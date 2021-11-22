# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import datetime

import a10.utils.constants
from a10.rules import baserule


# Individual Rules


class ClaimTimeliness(baserule.BaseRule):
    def __init__(self, cid, ps):
        super().__init__(cid, ps)
        self.description = "Claim Timeliness"

    def apply(self):
        margin = float(self.parameters["margin"])

        requested = float(self.claim["header"]["as_requested"])
        received = float(self.claim["header"]["as_received"])

        print("HERE2")
        print(requested, received)

        difference = received - requested
        print(difference)
        trusted = difference < margin
        print(trusted)

        additional = [{"timeDifference": str(difference), "margin": str(margin)}]

        if trusted == True:
            return self.returnMessage(
                a10.utils.constants.VERIFYSUCCEED, "Timing within margin", additional
            )
        else:
            return self.returnMessage(
                a10.utils.constants.VERIFYFAIL, "Timing outside of margin", additional
            )


class ClaimTPMTimeliness(baserule.BaseRule):
    def __init__(self, cid, ps):
        super().__init__(cid, ps)
        self.description = "ClaimTPMTimeliness"

    def apply(self):
        # Applies the rule

        margin = float(self.parameters["margin"])

        received = self.claim["header"]["ta_received"]
        completed = self.claim["header"]["ta_complete"]

        print("HERE2")
        # print(requested, completed)   # TODO: print(received, completed) ?

        difference = completed - received
        print(difference)
        trusted = difference < margin
        print(trusted)

        additional = [{"timeDifference": str(difference), "margin": str(margin)}]

        if trusted == True:
            return self.returnMessage(
                a10.utils.constants.VERIFYSUCCEED, "Timing within margin", additional
            )
        else:
            return self.returnMessage(
                a10.utils.constants.VERIFYFAIL, "Timing outside of margin", additional
            )


class ClaimClockIntegrity(baserule.BaseRule):
    def __init__(self, cid, ps):
        super().__init__(cid, ps)
        self.description = "ClaimClockIntegrity"

    def apply(self):
        # Applies the rule

        as_requested = self.claim["header"]["as_requested"]
        as_received = self.claim["header"]["as_received"]
        ta_received = self.claim["header"]["ta_received"]
        ta_complete = self.claim["header"]["ta_complete"]

        print("CLOCK**************")
        print(as_requested, as_received, ta_received, ta_complete)

        as_requested_t = datetime.datetime.strptime(
            as_requested, "%Y-%m-%d %H:%M:%S.%f"
        )
        as_received_t = datetime.datetime.strptime(as_received, "%Y-%m-%d %H:%M:%S.%f")
        ta_received_t = datetime.datetime.strptime(ta_received, "%Y-%m-%d %H:%M:%S.%f")
        ta_complete_t = datetime.datetime.strptime(ta_complete, "%Y-%m-%d %H:%M:%S.%f")

        trusted = False

        if as_requested_t < ta_received_t < ta_complete_t < as_received_t:
            trusted = True

        additional = [
            {
                "as_requested": str(as_requested_t),
                "ta_received": str(ta_received_t),
                "ta_complete": str(ta_complete_t),
                "as_received": str(as_received_t),
                "d1": str((ta_received_t - as_requested_t).total_seconds()),
                "d2": str((ta_complete_t - ta_received_t).total_seconds()),
                "d3": str((as_received_t - ta_complete_t).total_seconds()),
            }
        ]

        print("\n***********************\nADDITIONAL=", additional)

        # Returns a 2-tuple
        #  0 - The result
        #  1 - A description structure as a python dict
        if trusted == True:
            return self.returnMessage(
                a10.utils.constants.VERIFYSUCCEED, "Timing within margin", additional
            )
        else:
            return self.returnMessage(
                a10.utils.constants.VERIFYFAIL, "Timing outside of margin", additional
            )
