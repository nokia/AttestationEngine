# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import a10.asvr.protocols.A10ProtocolBase
import a10.structures.constants
import a10.structures.returncode


class A10DummyProtocol(a10.asvr.protocols.A10ProtocolBase.A10ProtocolBase):
    NAME = "A10DUMMYPROTOCOL"

    def __init__(self, endpoint, policyintent, policyparameters, additionalparameters):
        super().__init__(endpoint, policyintent, policyparameters, additionalparameters)

    def exec(self):
        # print("Calling protocol A10HTTPREST ", self.endpoint, self.policyintent, self.policyparameters,
        #      self.additionalparameters)
        # print("      + ---------------Types ", type(self.endpoint), type(self.policyintent),
        #      type(self.policyparameters), type(self.additionalparameters))

        elementURL = self.endpoint + "/" + self.policyintent
        callbody = {
            "policyparameters": self.policyparameters,
            "callparameters": self.additionalparameters,
        }

        return_data = {
            "Greeting": "Croeso!",
            "endpoint": self.endpoint,
            "policyintent": self.policyintent,
            "policyparameters": self.policyparameters,
            "additionalparameters": self.additionalparameters,
        }

        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.SUCCESS, return_data
        )
