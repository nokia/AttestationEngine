#Copyright 2021 Nokia
#Licensed under the BSD 3-Clause Clear License.
#SPDX-License-Identifier: BSD-3-Clear

import json
import requests
import subprocess
import tempfile
import secrets
import string
import base64

import a10.asvr.protocols.A10ProtocolBase

import a10.structures.constants
import a10.structures.returncode


class A10HttpRest(a10.asvr.protocols.A10ProtocolBase.A10ProtocolBase):
    NAME = "A10HTTPREST"

    def __init__(self, endpoint, policyintent, policyparameters, callparameters):
        super().__init__(endpoint, policyintent, policyparameters, callparameters)

    def exec(self):
        print("1¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤")

        # see the makecredential example for how to use this.
        # basically to store data that shouldn't be transmitted to the element
        # but needs to be persisted, eg: makecredetial's secret

        transientdata = {}

        # print(
        #     "Calling protocol A10HTTPREST ",
        #     self.endpoint,
        #     self.policyintent,
        #     self.policyparameters,
        #     self.callparameters,
        # )
        # print(
        #     "      + ---------------Types ",
        #     type(self.endpoint),
        #     type(self.policyintent),
        #     type(self.policyparameters),
        #     type(self.callparameters),
        # )

        #
        # Some intents require additional processing
        #

        if self.policyintent=="tpm2/credentialcheck":
            c = self.makecredential()
            if c==None:
                return a10.structures.returncode.ReturnCode(
                    a10.structures.constants.PROTOCOLEXECUTIONFAILURE, {"msg": "Makecredential failed","transientdata":transientdata}
                )
            cred,secret = self.makecredential()
            self.callparameters["credential"] = cred
            transientdata["secret"]=secret

        #
        # Ok, now go on with the calling
        #

        elementURL = self.endpoint + "/" + self.policyintent
        callbody = {
            "policyparameters": self.policyparameters,
            "callparameters": self.callparameters,
        }
        jsondata = json.dumps(callbody, ensure_ascii=False)

        # note, we use POST because the body contains data, which is not part of the GET standard
        try:
            r = requests.post(
                url=elementURL,
                json=jsondata,
                headers={"Content-type": "application/json", "Accept": "text/plain"},
                timeout=30,
            )
        except requests.exceptions.ConnectionError as e:
            return a10.structures.returncode.ReturnCode(
                a10.structures.constants.PROTOCOLNETWORKFAILURE,
                {"msg": "Network failure " + str(e),"transientdata":transientdata},
            )

        # This is already in JSON so ok
        # print("RETURNING ",r,r.text,r.status_code)

        # r.text is JSON but encoded as a strong, 
        #  so we need to convert (load) it into a python dictionary if things went well

        j = json.loads(r.text)
        print("2¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤")
        #
        # Note we return a tuple of the data back from the element and the transient data
        #
        if r.status_code == 200:
            return a10.structures.returncode.ReturnCode(
                a10.structures.constants.PROTOCOLSUCCESS, {"claim":json.loads(r.text),"transientdata":transientdata}
            )
        else:
            return a10.structures.returncode.ReturnCode(
                a10.structures.constants.PROTOCOLEXECUTIONFAILURE, {"msg":json.loads(r.text),"transientdata":transientdata}
            )

    def makecredential(self):
        print("\nmakecredential")

        try:
            ekpub = self.callparameters["ekpub"]
            akname = self.callparameters["akname"]
        except:
            print("missing ekpub and/or akname ")
            return None

        # a bit of housekeeping
        # store ek in temporary file
        ektf = tempfile.NamedTemporaryFile()

        ektf.write(bytes(ekpub, "utf-8"))
        ektf.seek(0)

        # generate secret
        # This must be a maximum of 32 bytes for makecredential - it is possible that your TPM might vary, but 32 seems to be usual
        alphabet = string.ascii_letters + string.digits
        secret = "".join(secrets.choice(alphabet) for i in range(30))
        print("Secret is ", secret)

        secf = tempfile.NamedTemporaryFile()
        secf.write(bytes(secret, "ascii"))
        secf.seek(0)

        # temporary file for credential
        credf = tempfile.NamedTemporaryFile()

        # makecredential
        #
        # assuming no local TPM with -T none --- might change this one day
        # given that the tools need to be available .... or pytss.
        # OK, maybe not such a bad thing that the AE runs on a device with a TPM or at least the tools
        # Will be necessary for using tpm2_send in the other protocol
        #
        try:
            out=""
            cmd = (
                "tpm2_makecredential -T none"
                + " -s "
                + secf.name
                + " -u "
                + ektf.name
                + " -n "
                + akname
                + " -G rsa -o "
                + credf.name
            )
            out = subprocess.check_output(cmd.split())
        except:
            print("tpm2_makecredential failed "+out)
            return None

        # read the credential
        credf.seek(0)
        cred = base64.b64encode(credf.read()).decode("utf-8")

        # cleanup
        ektf.close()
        secf.close()
        credf.close()

        # return
        return cred,secret


