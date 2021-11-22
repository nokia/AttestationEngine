# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause


class Result:
    def __init__(self, res, msg, add, eid, pid, cid, vat, par, rn, ev):
        self.s = {}
        self.s["result"] = res
        self.s["message"] = msg
        self.s["additional"] = add
        self.s["elementID"] = eid
        self.s["policyID"] = pid
        self.s["claimID"] = cid
        self.s["verifiedAt"] = vat
        self.s["ruleParameters"] = par
        self.s["ruleName"] = rn
        self.s["ev"] = ev

    def asDict(self):
        return self.s
