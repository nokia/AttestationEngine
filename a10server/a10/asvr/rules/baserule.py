# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import a10.structures.constants
import a10.structures.identity
import a10.structures.timestamps
import a10.structures.returncode

from a10.asvr import claims, expectedvalues


class BaseRule:
    NAME = "<<abstract>>baserule.BaseRule"
    DESCRIPTION = "Abstract Class Base Rule - not to be used for anything."

    def __init__(self, cid, ps):
        # cid is the claim ID
        # ps are additional parameters
        # ps is the set of additional parameters as a python dict

        self.claimID = cid
        self.claim = claims.getClaim(self.claimID).msg()
        self.parameters = ps
        self.ruleClassName = type(self).__name__
        self.ruleName = self.NAME
        self.ev = {}

    def apply(self):
        # In subclasses this is overridden to actually apply the rule. It must end with a return.self.returnMessage(...) call

        return self.returnMessage(
            a10.utils.constants.VERIFYSUCCEED,
            "Base Rule - Always returns VERIFYSUCCEED",
            [{"nothing": "to add"}, {"nothing again": "to add either"}],
        )

    def returnMessage(self, res, msg, add):
        return {
            "result": res,
            "message": msg,
            "description": self.DESCRIPTION,
            "claimID": self.claimID,
            "ruleClassName": self.ruleClassName,
            "ruleName": self.ruleName,
            "additional": add,
            "ruleParameters": self.parameters,
            "ev": self.ev,
        }

    def setExpectedValue(self):
        print("self.claim ", self.claim)
        eid = self.claim["header"]["element"]["itemid"]
        pid = self.claim["header"]["policy"]["itemid"]
        e = expectedvalues.getExpectedValueForElementAndPolicy(eid, pid)
        if e.rc() == a10.structures.constants.SUCCESS:
            self.ev = e.msg()
            return True
        else:
            return False
