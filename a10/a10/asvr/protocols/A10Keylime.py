#Copyright 2021 Nokia, Thore Sommer
#Licensed under the BSD 3-Clause Clear License.
#SPDX-License-Identifier: BSD-3-Clear


import random
import subprocess
import tempfile
import zlib

import requests
import string
import base64

import yaml

import a10.asvr.protocols.A10ProtocolBase

import a10.structures.constants
import a10.structures.returncode


class A10Keylime(a10.asvr.protocols.A10ProtocolBase.A10ProtocolBase):
    NAME = "A10Keylime"
    KEYLIME_VERSION = "v1.0"

    def __init__(self, endpoint, policyintent, policyparameters, callparameters):
        super().__init__(endpoint, policyintent, policyparameters, callparameters)

    def contact_keylime(self, mask, nonce):
        # partial is set to 1 because we do not use the NK
        # vmask and mask are set to the same value because deep quotes are currently not supported
        keylime_url = f"{self.endpoint}/{self.KEYLIME_VERSION}/quotes/integrity/?nonce={nonce}&partial=1&mask={mask}&vmask={mask}"

        try:
            req = requests.get(keylime_url)
        except requests.exceptions.ConnectionError as e:
            return None, a10.structures.returncode.ReturnCode(
                a10.structures.constants.PROTOCOLNETWORKFAILURE,
                {"message": "Network failure " + str(e)},
            )
        if req.status_code != 200:
            return None, a10.structures.returncode.ReturnCode(
                a10.structures.constants.PROTOCOLNETWORKFAILURE,
                {"message": f"Agent returned {req.status_code}"},
            )
        #
        #  Reformat it into the form that A10 expects
        #    - which is like what TPM2_Quote returns in JSON format
        #
        data = req.json().get("results", None)

        if "quote" not in data:
            return None, a10.structures.returncode.ReturnCode(
                a10.structures.constants.PROTOCOLNETWORKFAILURE,
                {"message": f"No quote in agent response"},
            )

        #  Start parsing the quote data
        quote = data["quote"]
        if quote[0] != "r":
            return None, a10.structures.returncode.ReturnCode(
                a10.structures.constants.PROTOCOLNETWORKFAILURE,
                {"message": f"Quote data is actually not a quote"},
            )

        quote = quote[1:]

        quote_parts = quote.split(":")
        if len(quote_parts) != 3:
            return None, a10.structures.returncode.ReturnCode(
                a10.structures.constants.PROTOCOLNETWORKFAILURE,
                {"message": f"Quote should be composed of three parts"},
            )

        quote_message = zlib.decompress(base64.b64decode(quote_parts[0]))
        quote_signature = zlib.decompress(base64.b64decode(quote_parts[1]))
        quote_pcrvalues = zlib.decompress(base64.b64decode(quote_parts[2]))

        with tempfile.NamedTemporaryFile(prefix="a10_") as qmf:
            qmf.write(quote_message)
            qmf.seek(0)
            qm = subprocess.check_output(["tpm2_print", "-t", "TPMS_ATTEST", qmf.name])

        quote_data = convert_keys_to_str(yaml.safe_load(qm))
        eventlog = None
        if "mb_measurement_list" in data:
            eventlog = base64.b64decode(data["mb_measurement_list"])
        out = {
            "quote": quote_data,
            "eventlog": eventlog,
            "ima": data.get("ima_measurement_list")
        }
        return out, None

    def handle_quote(self, nonce):
        # TODO: add error handling
        hash_alg = self.policyparameters["hashalg"]
        pcrselection = self.policyparameters["pcrselection"]
        pcr_hash_alg, mask = pcr_selection_to_mask(pcrselection)
        assert pcr_hash_alg == hash_alg, "Hash algorithms do not match"

        data, err = self.contact_keylime(mask, nonce)
        if err:
            return err
        out_data = {"payload": {"quote": data["quote"]}}

        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.PROTOCOLSUCCESS, (out_data, None)
        )

    def handle_eventlog(self, nonce):
        # Mask must include PCR 0 for the agent to send the eventlog
        data, err = self.contact_keylime("0x1", nonce)
        if err:
            return err
        eventlog = data["eventlog"]
        if not eventlog:
            return a10.structures.returncode.ReturnCode(
                a10.structures.constants.PROTOCOLEXECUTIONFAILURE, f"UEFI Eventlog is missing from Keylime agent"
            )

        eventlog_enc = base64.b85encode(eventlog).decode("utf-8")
        out_data = {"payload": {
            "encoding": "base85/utf-8",
            "eventlog": eventlog_enc,
            "size": len(eventlog),
            "sizeencoded": len(eventlog_enc)
        }}

        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.PROTOCOLSUCCESS, (out_data, None)
        )

    def handle_ima_log(self, nonce):
        # Mask must include PCR 10 for the agent to send the IMA log
        data, err = self.contact_keylime("0x400", nonce)
        if err:
            return err
        ima_log = data["ima"]
        if not ima_log:
            return a10.structures.returncode.ReturnCode(
                a10.structures.constants.PROTOCOLEXECUTIONFAILURE, f"IMA log is missing from Keylime agent"
            )

        # TODO: in what format this should be?
        out_data = {"payload": {
            "size": len(ima_log),
            "logfile": "/sys/kernel/security/ima/ascii_runtime_measurements",
            "imalog": ima_log
        }}
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.PROTOCOLSUCCESS, (out_data, None)
        )

    def exec(self):
        nonce = ''.join(random.choice(string.ascii_letters) for _ in range(20))

        if self.policyintent == "tpm2/quote":
            return self.handle_quote(nonce)
        elif self.policyintent == "uefi/eventlog":
            return self.handle_eventlog(nonce)
        elif self.policyintent == "ima/ascii":
            return self.handle_ima_log(nonce)
        else:
            return a10.structures.returncode.ReturnCode(
                a10.structures.constants.PROTOCOLEXECUTIONFAILURE, f"Keylime support {self.policyintent}"
            )


def pcr_selection_to_mask(pcr_selection):
    hash_alg, pcrs = pcr_selection.split(":")
    pcrs = [int(pcr) for pcr in pcrs.split(",") if pcr != ""]
    mask = 0
    for i in set(pcrs):
        mask = mask | (1 << i)
    return hash_alg, hex(mask)


def convert_keys_to_str(item):
    if isinstance(item, dict):
        new_item = {}
        for key, val in item.items():
            new_item[str(key)] = convert_keys_to_str(val)
        return new_item
    return item