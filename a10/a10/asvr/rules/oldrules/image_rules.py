# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import a10.utils.constants
from a10.rules import baserule
from a10.rules.tpm2rules import (
    TPM2Safe,
    TPM2QuoteMagicNumber,
    TPM2QuoteType,
    TPM2QuoteAttestedValue,
    TPM2FirmwareVersion,
)


class ImageStandardVerify(baserule.BaseRule):
    def __init__(self, cid, ps):
        super().__init__(cid, ps)
        self.description = "ImageStandardVerify"

    def apply(self):
        magicRule_result = TPM2QuoteMagicNumber(self.claimID, self.parameters).apply()
        quoteType_result = TPM2QuoteType(self.claimID, self.parameters).apply()
        safe_result = TPM2Safe(self.claimID, self.parameters).apply()
        av_result = TPM2QuoteAttestedValue(self.claimID, self.parameters).apply()
        fw_result = TPM2FirmwareVersion(self.claimID, self.parameters).apply()

        subresults = []
        subresults.append(magicRule_result)
        subresults.append(quoteType_result)
        subresults.append(safe_result)
        subresults.append(av_result)
        subresults.append(fw_result)

        trusted = (
            (magicRule_result["result"] == a10.utils.constants.VERIFYSUCCEED)
            and (quoteType_result["result"] == a10.utils.constants.VERIFYSUCCEED)
            and (safe_result["result"] == a10.utils.constants.VERIFYSUCCEED)
            and (av_result["result"] == a10.utils.constants.VERIFYSUCCEED)
            and (fw_result["result"] == a10.utils.constants.VERIFYSUCCEED)
        )

        msg = "Additional contains " + str(len(subresults)) + " items"

        if trusted == True:
            return self.returnMessage(
                a10.utils.constants.VERIFYSUCCEED, msg, subresults
            )
        else:
            return self.returnMessage(a10.utils.constants.VERIFYFAIL, msg, subresults)
