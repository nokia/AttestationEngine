# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import json
import requests

import a10.asvr.protocols.A10ProtocolBase

import a10.structures.constants
import a10.structures.returncode


class A10HttpRest(a10.asvr.protocols.A10ProtocolBase.A10ProtocolBase):
    NAME = "A10HTTPREST"

    def __init__(self, endpoint, policyintent, policyparameters, additionalparameters):
        super().__init__(endpoint, policyintent, policyparameters, additionalparameters)

    def exec(self):
        print(
            "Calling protocol A10HTTPREST ",
            self.endpoint,
            self.policyintent,
            self.policyparameters,
            self.additionalparameters,
        )
        print(
            "      + ---------------Types ",
            type(self.endpoint),
            type(self.policyintent),
            type(self.policyparameters),
            type(self.additionalparameters),
        )

        elementURL = self.endpoint + "/" + self.policyintent
        callbody = {
            "policyparameters": self.policyparameters,
            "callparameters": self.additionalparameters,
        }
        jsondata = json.dumps(callbody, ensure_ascii=False)

        # note, we use POST because the body contains data, which is not part of the GET standard
        try:
            r = requests.post(
                url=elementURL,
                json=jsondata,
                headers={"Content-type": "application/json", "Accept": "text/plain"},
                timeout=20,
            )
        except requests.exceptions.ConnectionError as e:
            return a10.structures.returncode.ReturnCode(
                a10.structures.constants.PROTOCOLNETWORKFAILURE,
                {"message": "Network failure " + str(e)},
            )

        # This is already in JSON so ok
        # print("RETURNING ",r,r.text,r.status_code)

        # r.text is JSON, so we need to convert (load) it into a python dictionary if htings went well
        if r.status_code == 200:
            return a10.structures.returncode.ReturnCode(
                a10.structures.constants.PROTOCOLSUCCESS, json.loads(r.text)
            )

        else:
            return a10.structures.returncode.ReturnCode(
                a10.structures.constants.PROTOCOLEXECUTIONFAILURE,
                {"message": "http failure", "return code": r.status_code},
            )
