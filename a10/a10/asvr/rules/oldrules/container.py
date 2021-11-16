# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import a10.utils.constants
from a10.rules import baserule


class ImageVerification(baserule.BaseRule):
    def __init__(self, cid, ps):
        super().__init__(cid, ps)
        self.description = "ContainerImageVerification"

    def apply(self):
        manifest_result = ContainerImageQuoteAttestedValue(
            self.claimID, self.parameters
        ).apply()

        trusted = manifest_result["result"]
        msg = manifest_result["message"]
        subresults = []

        if trusted:
            return self.returnMessage(
                a10.utils.constants.VERIFYSUCCEED, msg, subresults
            )
        else:
            return self.returnMessage(a10.utils.constants.VERIFYFAIL, msg, subresults)


class InstanceVerification(baserule.BaseRule):
    def __init__(self, cid, ps):
        super().__init__(cid, ps)
        self.description = "ContainerInstanceVerification"

    def apply(self):
        manifest_result = ContainerInstanceQuoteAttestedValue(
            self.claimID, self.parameters
        ).apply()

        trusted = manifest_result["result"]
        msg = manifest_result["message"]
        subresults = []

        if trusted:
            return self.returnMessage(
                a10.utils.constants.VERIFYSUCCEED, msg, subresults
            )
        else:
            return self.returnMessage(a10.utils.constants.VERIFYFAIL, msg, subresults)


class ContainerImageQuoteAttestedValue(baserule.BaseRule):
    def __init__(self, cid, ps):
        super().__init__(cid, ps)
        self.description = "Check Container Image Signature Is Correct"

    def apply(self):
        self.setExpectedValue()

        known_good_value = self.ev["evs"]["imageManifestDigest"]
        claimed_attested_value = self.claim["payload"]["quote"]["attested"]["quote"][
            "imageManifestDigest"
        ]
        trusted = claimed_attested_value == known_good_value

        if trusted:
            return self.returnMessage(
                a10.utils.constants.VERIFYSUCCEED,
                "attested value == known good value",
                [],
            )
        else:
            msg = (
                "Incorrect attested value, got "
                + claimed_attested_value
                + " was expecting "
                + known_good_value
            )
            return self.returnMessage(a10.utils.constants.VERIFYFAIL, msg, [])


class ContainerInstanceQuoteAttestedValue(baserule.BaseRule):
    def __init__(self, cid, ps):
        super().__init__(cid, ps)
        self.description = "Check Container Writable Snapshot Digest Is Correct"

    def apply(self):
        self.setExpectedValue()

        known_good_value = self.ev["evs"]["copyOnWriteSnapshotDigest"]
        claimed_attested_value = self.claim["payload"]["quote"]["attested"]["quote"][
            "copyOnWriteSnapshotDigest"
        ]
        trusted = claimed_attested_value == known_good_value

        if trusted:
            return self.returnMessage(
                a10.utils.constants.VERIFYSUCCEED,
                "attested value == known good value",
                [],
            )
        else:
            msg = (
                "Incorrect attested value, got "
                + claimed_attested_value
                + " was expecting "
                + known_good_value
            )
            return self.returnMessage(a10.utils.constants.VERIFYFAIL, msg, [])
