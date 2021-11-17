# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import a10.utils.constants
from a10.asvr import claims
from a10.rules import baserule


# Individual Rules


class TestRule(baserule.BaseRule):
    def __init__(self, cid, ps):
        super().__init__(cid, ps)
        self.description = "A10 Arduino TestRule"

    def apply(self):
        # Applies the rule

        claim = claims.getClaimByID(self.claimID)[0]

        # print("Returned claim is ",claim,"\n\n")

        # print("Payload is ",claim['payload'],"\n\n")
        # print("Payload is ",claim['payload']['sn'],"\n\n")

        trusted = claim["payload"]["sn"] == "42"

        if trusted == True:
            return self.returnMessage(a10.utils.constants.VERIFYSUCCEED, "sn == 42", [])
        else:
            return self.returnMessage(
                a10.utils.constants.VERIFYFAIL, "Incorrect SN", []
            )
