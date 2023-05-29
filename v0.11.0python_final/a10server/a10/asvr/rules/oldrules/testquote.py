# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

from a10.rules import baserule
import a10.utils.constants


class TestQuote(baserule.BaseRule):
    def __init__(self, cid, ps):
        super().__init__(cid, ps)
        self.description = "TestQuote Test Rule Rulez OK"

    def apply(self):
        # Applies the rule

        print("Checking Magic Number")

        mn = self.claim["quote"]["magic"]

        print("Magic Number is ", mn)
        print("Additional parameters are ", self.parameters)

        # Returns a 2-tuple
        #  0 - The result
        #  1 - A description structure as a python dict

        return self.returnMessage(
            a10.utils.constants.VERIFYSUCCEED,
            self.description + " Always returns true",
            [{"claimID": self.claimID}],
        )
