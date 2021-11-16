# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import json

import requests

import a10.utils.constants as a10_constants
from a10.asvr.protocols.A10ProtocolBase import A10ProtocolBase


class A10ContainerIntents:
    IMAGE = "IMAGE"
    CONTAINER = "CONTAINER"


class A10Container(A10ProtocolBase):
    NAME = "A10CONTAINERIMAGE"  # This name will be stored in the DB

    def __init__(self, endpoint, policyintent, policyparameters):
        super().__init__(endpoint, policyintent, policyparameters, None)

    def exec(self):
        print(
            f"Calling protocol A10Container {self.endpoint} with intent {self.policyintent}"
        )

        if self.policyintent == A10ContainerIntents.IMAGE:
            """
            For the container image:
              Call the TA on the master node which will:
                  - call rekor to get the pub key cert of the image signer
                  - call cosign and verify the image signature
                  - return a claim (manifest digest) in form of a jws signed with the AK of the master node
            """
            # element_url = f"{self.endpoint}/attest/{self.policyintent}"
        elif self.policyintent == A10ContainerIntents.CONTAINER:
            """
            For the container instance:
              Call the TA on the running node which will:
                  - inspect the internal structure of the container which is running inside containerd
                  - return a claim (hash of the writable snapshot) in form of a jws signed with the AK of the running node
            """
        else:
            raise Exception("Policy intent not recognized")

        element_url = f"{self.endpoint}/attest/{self.policyintent}"

        call_body = {
            "policyparameters": self.policyparameters,
            "callparameters": self.additionalparameters,
        }
        json_data = json.dumps(call_body, ensure_ascii=False)

        response = requests.post(
            url=element_url,
            json=json_data,
            headers={"Content-type": "application/json", "Accept": "text/plain"},
            timeout=20,
        )

        if response.status_code == 200:
            return a10_constants.PROTOCOLSUCCESS, json.loads(response.text)
        else:
            return (
                a10_constants.PROTOCOLEXECUTIONFAILURE,
                {"message": "http failure", "return code": response.status_code},
            )
