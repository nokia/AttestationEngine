# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import a10.asvr.protocols.A10ProtocolBase
import a10.structures.constants
import a10.structures.returncode


class Lannion(a10.asvr.protocols.A10ProtocolBase.A10ProtocolBase):
    NAME = "Lannion"

    def __init__(self, endpoint, policyintent, policyparameters, callparameters):
        super().__init__(endpoint, policyintent, policyparameters, callparameters)

    def exec(self):
        # print("Calling protocol A10HTTPREST ", self.endpoint, self.policyintent, self.policyparameters,
        #      self.additionalparameters)
        # print("      + ---------------Types ", type(self.endpoint), type(self.policyintent),
        #      type(self.policyparameters), type(self.additionalparameters))

        elementURL = self.endpoint + "/" + self.policyintent
        callbody = {
            "policyparameters": self.policyparameters,
            "callparameters": self.callparameters,
        }

        return_data = {
            "Greeting": "Croeso!",
            "endpoint": self.endpoint,
            "policyintent": self.policyintent,
            "policyparameters": self.policyparameters,
            "callparameters": self.callparameters,
        }

        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.SUCCESS, return_data
        )
