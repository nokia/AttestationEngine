# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import a10.structures.constants
import a10.structures.returncode

from . import baserule


class ValidUEFIEventLog(baserule.BaseRule):
    NAME = "uefi/ValidUEFIEventLog"
    DESCRIPTION = "Validates a given UEFI EventLog against something..."

    def __init__(self, cid, ps):
        super().__init__(cid, ps)

    def apply(self):
        return self.returnMessage(
            a10.structures.constants.VERIFYSUCCEED,
            "UEFI EVENTLOG Rule - Always returns VERIFYSUCCEED",
            [],
        )
