# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import a10.utils.constants
from a10.rules import baserule


# PCR Rules


class PCRsAllUnassigned(baserule.BaseRule):
    NAME = "tpm2rules/PCRsAllUnassigned"

    def __init__(self, cid, ps):
        super().__init__(cid, ps)
        self.description = "TPM2 Check all PCRS for given bank to be unassigned"

    def apply(self):
        try:
            print("Attempting bank ", self.parameters)

            bank = str(self.parameters["bank"])

        except KeyError:
            return self.returnMessage(
                a10.utils.constants.VERIFYERROR,
                "Missing bank parameter. Was expecting sha1, sha256, sha384 or sha512",
                [],
            )

        print("Got bank ", bank)

        if bank not in ["sha1", "sha256", "sha384", "sha512", "test"]:
            return self.returnMessage(
                a10.utils.constants.VERIFYERROR,
                "Unknown PCR bank, got "
                + bank
                + " was expecting sha1, sha256, sha384 or sha512",
                [],
            )

        try:
            pcrs = self.claim["payload"]["pcrs"][bank]

        except KeyError:
            return self.returnMessage(
                a10.utils.constants.VERIFYERROR,
                "PCR bank " + bank + " not supported on this TPM",
                [],
            )

        assigned = "Assigned: "
        trusted = True

        # SHA1 banks
        zeros = "0x0000000000000000000000000000000000000000"
        ffs = "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"

        if bank == "sha256":
            zeros = "0x0000000000000000000000000000000000000000000000000000000000000000"
            ffs = "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"
        elif bank == "sha384":
            zeros = "0x000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
            ffs = "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"
        elif bank == "sha512":
            zeros = "0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
            ffs = "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"
        else:
            pass  # because we already set sha1 above as the default

        for p in range(0, 17):  # PCRs 0,1,2...16  all should be 0x00:
            pcrentry = str(pcrs[str(p)])
            print("PCR ", p, " is ", pcrentry, pcrentry != zeros)

            if pcrentry != zeros:
                trusted = False
                assigned = assigned + str(p) + " "

        for p in range(17, 23):  # PCRs 17..22  all should be 0xFF:
            pcrentry = str(pcrs[str(p)])
            if pcrentry != ffs:
                trusted = False
                assigned = assigned + str(p) + " "

        # Finally PCR 23 should be 0x00
        pcrentry = str(pcrs["23"])
        print("PCR 23 is ", pcrentry, pcrentry != zeros)
        if pcrentry != zeros:
            trusted = False
            assigned = assigned + "23 "

        if trusted == True:
            return self.returnMessage(
                a10.utils.constants.VERIFYSUCCEED, "All PCRs unassigned", []
            )
        else:
            return self.returnMessage(a10.utils.constants.VERIFYFAIL, assigned, [])


# Individual Quote Rules


class TPM2QuoteMagicNumber(baserule.BaseRule):
    def __init__(self, cid, ps):
        super().__init__(cid, ps)
        self.description = "TPM2 Check TPMS_ATTEST Magic Number Correct"

    def apply(self):
        trusted = self.claim["payload"]["quote"]["magic"] == "ff544347"

        if trusted == True:
            return self.returnMessage(
                a10.utils.constants.VERIFYSUCCEED, "magic == ff544347", []
            )
        else:
            return self.returnMessage(
                a10.utils.constants.VERIFYFAIL, "Incorred magic number", []
            )


class TPM2QuoteType(baserule.BaseRule):
    def __init__(self, cid, ps):
        super().__init__(cid, ps)
        self.description = "TPM2 Check TPMS_ATTEST Type Correct"

    def apply(self):
        trusted = self.claim["payload"]["quote"]["type"] == 8018

        if trusted == True:
            return self.returnMessage(
                a10.utils.constants.VERIFYSUCCEED, "type == 801", []
            )
        else:
            return self.returnMessage(
                a10.utils.constants.VERIFYFAIL, "Incorred TPMS_ATTEST type", []
            )


class TPM2QuoteAttestedValue(baserule.BaseRule):
    def __init__(self, cid, ps):
        super().__init__(cid, ps)
        self.description = "TPM2 Check TPMS_ATTEST Magic Number Correct"

    def apply(self):
        self.setExpectedValue()

        knownGoodValue = self.ev["evs"]["pcrDigest"]
        claimedAttestedValue = self.claim["payload"]["quote"]["attested"]["quote"][
            "pcrDigest"
        ]
        trusted = claimedAttestedValue == knownGoodValue

        if trusted == True:
            return self.returnMessage(
                a10.utils.constants.VERIFYSUCCEED,
                "attested value == known good value",
                [],
            )
        else:
            msg = (
                "Incorrect attested value, got "
                + claimedAttestedValue
                + " was expecting "
                + knownGoodValue
            )
            return self.returnMessage(a10.utils.constants.VERIFYFAIL, msg, [])


class TPM2FirmwareVersion(baserule.BaseRule):
    def __init__(self, cid, ps):
        super().__init__(cid, ps)
        self.description = "TPM2 Check Firmware Version for Given Device"

    def apply(self):
        self.setExpectedValue()

        try:
            knownGoodValue = self.ev["evs"]["firmwareVersion"]
            # Type cast to str because the IBM TPM Simualtor reports its vesion number as hex but without any of the A..F being present
            # This means that python pyyaml which interprets tpm2_quote's result via tpm2_print guesses that firmware is an Int64
            # In all other calses pyyaml interprets this as a string
            claimedAttestedValue = str(
                self.claim["payload"]["quote"]["firmwareVersion"]
            )
        except Exception as e:
            msg = "An error occured " + str(e)
            return self.returnMessage(a10.utils.constants.VERIFYERROR, msg, [])

        trusted = claimedAttestedValue == knownGoodValue

        if trusted == True:
            return self.returnMessage(
                a10.utils.constants.VERIFYSUCCEED,
                "attested value == known good value",
                [],
            )
        else:
            msg = (
                "Incorrect firmware version, got "
                + claimedAttestedValue
                + " was expecting "
                + knownGoodValue
            )
            return self.returnMessage(a10.utils.constants.VERIFYFAIL, msg, [])


# Clock Stuff


class TPM2Safe(baserule.BaseRule):
    def __init__(self, cid, ps):
        super().__init__(cid, ps)
        self.description = "TPM2 Check Safe == 1"

    def apply(self):
        trusted = self.claim["payload"]["quote"]["clockInfo"]["safe"] == 1

        if trusted == True:
            return self.returnMessage(a10.utils.constants.VERIFYSUCCEED, "Safe", [])
        else:
            return self.returnMessage(a10.utils.constants.VERIFYFAIL, "Unsafe", [])


# Signature


class TPM2QuoteSignatureVerify(baserule.BaseRule):
    def __init__(self, cid, ps):
        super().__init__(cid, ps)
        self.description = "TPM2QuoteSignatureVerify"

    def apply(self):
        trusted = True

        if trusted == True:
            return self.returnMessage(
                a10.utils.constants.VERIFYSUCCEED,
                "Quote matches Signature and signed by AK",
                [],
            )
        else:
            return self.returnMessage(
                a10.utils.constants.VERIFYFAIL, "Quote does not match Signature", []
            )

        # Combinations


class TPM2QuoteStandardVerify(baserule.BaseRule):
    def __init__(self, cid, ps):
        super().__init__(cid, ps)
        self.description = "TTPM2QuoteStandardVerify"

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


class TPM2QuoteStandardVerifyWithSignatureCheck(baserule.BaseRule):
    def __init__(self, cid, ps):
        super().__init__(cid, ps)
        self.description = "TPM2QuoteStandardVerifyWithSignatureCheck"

    def apply(self):
        standardverify_results = TPM2QuoteStandardVerify(
            self.claimID, self.parameters
        ).apply()
        sigantureverify_result = TPM2QuoteSignatureVerify(
            self.claimID, self.parameters
        ).apply()

        subresults = []
        subresults.append(sigantureverify_result)
        # subresults.append(av_result) # TODO: `av_result` is not defined at this point and will fail

        trusted = standardverify_results["result"] & sigantureverify_result["result"]

        msg = "Additional contains " + str(len(subresults)) + " items"

        if trusted == True:
            return self.returnMessage(
                a10.utils.constants.VERIFYSUCCEED, msg, subresults
            )
        else:
            return self.returnMessage(a10.utils.constants.VERIFYFAIL, msg, subresults)
