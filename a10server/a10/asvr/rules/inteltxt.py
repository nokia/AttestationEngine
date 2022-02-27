# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import a10.structures.constants
import a10.structures.returncode

from . import baserule


class MeasuredBootLaunch(baserule.BaseRule):
    NAME = "inteltxt/MeassuredBootLaunch"
    DESCRIPTION = "Success if measured boot launch status is the given value - defaults to True"

    def __init__(self, cid, ps):
        super().__init__(cid, ps)

    def apply(self):

        bootvalue = "TRUE"

        try:
            b = str(self.parameters["expect"])
            if b=="FALSE":
                bootvalue="FALSE"

        except KeyError:
            bootvalue = "TRUE"

        #print(self.claim)

        try:
            txtlog = str(
                self.claim["payload"]["payload"]["stat"]
            )
        except Exception as e:
            return self.returnMessage(
            a10.structures.constants.VERIFYNORESULT,
            "Exception getting TXT status"+str(e),
            [],
            )            

        f = txtlog.find("TXT measured launch: "+bootvalue)

        if f==-1:
            return self.returnMessage(
            a10.structures.constants.VERIFYFAIL,
            "TXT status not matching expected "+bootvalue,
            [],
            )
        else:
            return self.returnMessage(
            a10.structures.constants.VERIFYSUCCEED,
            "TXT Measured boot launch as expected "+bootvalue,
            [],
        )
