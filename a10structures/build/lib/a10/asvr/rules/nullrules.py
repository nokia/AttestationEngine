# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import a10.structures.constants

from a10.asvr.rules import baserule


# Individual Rules


class AlwaysSuccess(baserule.BaseRule):
    NAME = "nullrules/AlwaysSuccess"
    DESCRIPTION = "Always success null rule. This return always returns SUCCESS"

    def __init__(self, cid, ps):
        super().__init__(cid, ps)
        self.description = (
            "Always success null rule. This return always returns SUCCESS"
        )

    def apply(self):
        return self.returnMessage(
            a10.structures.constants.VERIFYSUCCEED,
            "Null Rule - Always returns VERIFYSUCCEED",
            [{"nothing": "to add"}, {"nothing again": "to add either"}],
        )


class AlwaysFail(baserule.BaseRule):
    NAME = "nullrules/AlwaysFail"
    DESCRIPTION = "Always fail null rule. This return always returns FAIL"

    def __init__(self, cid, ps):
        super().__init__(cid, ps)
        self.description = "Always fail null rule. This return always returns FAIL"

    def apply(self):
        return self.returnMessage(
            a10.structures.constants.VERIFYFAIL,
            "Null Rule - Always returns VERIFYFAIL",
            [{"nothing": "to add"}, {"nothing again": "to add either"}],
        )


class AlwaysError(baserule.BaseRule):
    NAME = "nullrules/AlwaysError"
    DESCRIPTION = "Always error null rule. This return always returns ERROR"

    def __init__(self, cid, ps):
        super().__init__(cid, ps)
        self.description = "Always error null rule. This return always returns ERROR"

    def apply(self):
        return self.returnMessage(
            a10.structures.constants.VERIFYERROR,
            "Null Rule - Always returns VERIFYERROR",
            [{"nothing": "to add"}, {"nothing again": "to add either"}],
        )


class AlwaysNoResult(baserule.BaseRule):
    NAME = "nullrules/AlwaysNoResult"
    DESCRIPTION = "Always no result null rule. This return always returns NORESULT"

    def __init__(self, cid, ps):
        super().__init__(cid, ps)
        self.description = (
            "Always no result null rule. This return always returns NORESULT"
        )

    def apply(self):
        return self.returnMessage(
            a10.structures.constants.VERIFYNORESULT,
            "Null Rule - Always returns VERIFYNORESULT",
            [{"nothing": "to add"}, {"nothing again": "to add either"}],
        )
