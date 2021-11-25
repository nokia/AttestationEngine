# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import a10.structures.constants
import a10.structures.returncode

from . import baserule


# PCR Rules


class PCRsAllUnassigned(baserule.BaseRule):
    NAME = "tpm2rules/PCRsAllUnassigned"
    DESCRIPTION = "TPM2 Check all PCRS for given bank to be unassigned"

    def __init__(self, cid, ps):
        super().__init__(cid, ps)

    def apply(self):
        try:
            bank = str(self.parameters["bank"])
        except KeyError:
            return self.returnMessage(
                a10.structures.constants.VERIFYERROR,
                "Missing bank parameter. Was expecting sha1, sha256, sha384 or sha512",
                [],
            )

        if bank not in ["sha1", "sha256", "sha384", "sha512", "test"]:
            return self.returnMessage(
                a10.structures.constants.VERIFYERROR,
                "Unknown PCR bank, got "
                + bank
                + " was expecting sha1, sha256, sha384 or sha512",
                [],
            )

        try:
            pcrs = self.claim["payload"]["payload"]["pcrs"][bank]
        except KeyError:
            return self.returnMessage(
                a10.structures.constants.VERIFYERROR,
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
        if pcrentry != zeros:
            trusted = False
            assigned = assigned + "23 "

        if trusted == True:
            return self.returnMessage(
                a10.structures.constants.VERIFYSUCCEED, "All PCRs unassigned", []
            )
        else:
            return self.returnMessage(a10.structures.constants.VERIFYFAIL, assigned, [])


class TPM2FirmwareVersion(baserule.BaseRule):
    NAME = "tpm2rules/TPM2FirmwareVersion"
    DESCRIPTION = "TPM2 Check Firmware Version for Given Device"

    def __init__(self, cid, ps):
        super().__init__(cid, ps)

    def apply(self):
        sev = self.setExpectedValue()

        if sev == False:
            msg = "Missing expected value"
            return self.returnMessage(a10.structures.constants.VERIFYNORESULT, msg, [])

        try:
            knownGoodValue = self.ev["evs"]["firmwareVersion"]
            # Type cast to str because the IBM TPM Simualtor reports its vesion number as hex but without any of the A..F being present
            # This means that python pyyaml which interprets tpm2_quote's result via tpm2_print guesses that firmware is an Int64
            # In all other calses pyyaml interprets this as a string
            claimedAttestedValue = str(
                self.claim["payload"]["payload"]["quote"]["firmwareVersion"]
            )
        except Exception as e:
            msg = "An error occured " + str(e)
            return self.returnMessage(a10.structures.constants.VERIFYERROR, msg, [])

        trusted = claimedAttestedValue == knownGoodValue

        if trusted == True:
            return self.returnMessage(
                a10.structures.constants.VERIFYSUCCEED,
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
            return self.returnMessage(a10.structures.constants.VERIFYFAIL, msg, [])


#
# Individual Quote Rules
#


class TPM2QuoteMagicNumber(baserule.BaseRule):
    def __init__(self, cid, ps):
        super().__init__(cid, ps)
        self.description = "TPM2 Check TPMS_ATTEST Magic Number Correct"

    def apply(self):
        try:
            trusted = self.claim["payload"]["payload"]["quote"]["magic"] == "ff544347"
        except KeyError:
            return self.returnMessage(
                a10.structures.constants.VERIFYERROR,
                "Missing magic number. Does this contain a TPMS_ATTEST structure?",
                [],
            )

        if trusted == True:
            return self.returnMessage(
                a10.structures.constants.VERIFYSUCCEED, "magic == ff544347", []
            )
        else:
            return self.returnMessage(
                a10.structures.constants.VERIFYFAIL, "Incorred magic number", []
            )


class TPM2QuoteType(baserule.BaseRule):
    def __init__(self, cid, ps):
        super().__init__(cid, ps)
        self.description = "TPM2 Check TPMS_ATTEST Type Correct"

    def apply(self):
        try:
            trusted = self.claim["payload"]["payload"]["quote"]["type"] == 8018
        except KeyError:
            return self.returnMessage(
                a10.structures.constants.VERIFYERROR,
                "Missing type. Does this contain a TPMS_ATTEST structure?",
                [],
            )

        if trusted == True:
            return self.returnMessage(
                a10.structures.constants.VERIFYSUCCEED, "type == 801", []
            )
        else:
            return self.returnMessage(
                a10.structures.constants.VERIFYFAIL, "Incorred TPMS_ATTEST type", []
            )


class TPM2QuoteAttestedValue(baserule.BaseRule):
    NAME = "tpm2rules/TPM2QuoteAttestedValue"
    DESCRIPTION = "TPM2 Check TPMS_ATTEST Magic Number Correct"

    def __init__(self, cid, ps):
        super().__init__(cid, ps)

    def apply(self):
        sev = self.setExpectedValue()

        if sev == False:
            msg = "Missing expected value"
            return self.returnMessage(a10.structures.constants.VERIFYNORESULT, msg, [])

        try:
            knownGoodValue = self.ev["evs"]["pcrDigest"]
        except KeyError:
            return self.returnMessage(
                a10.structures.constants.VERIFYERROR,
                "Missing pcrDigest value in expected value. ",
                [],
            )

        try:
            claimedAttestedValue = self.claim["payload"]["payload"]["quote"][
                "attested"
            ]["quote"]["pcrDigest"]
        except KeyError:
            return self.returnMessage(
                a10.structures.constants.VERIFYERROR,
                "Missing pcrDigest in quote. Is this a valid TPMS_ATTEST structure?",
                [],
            )

        trusted = claimedAttestedValue == knownGoodValue

        if trusted == True:
            return self.returnMessage(
                a10.structures.constants.VERIFYSUCCEED,
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
            return self.returnMessage(a10.structures.constants.VERIFYFAIL, msg, [])


#
# Clock Stuff
#


class TPM2Safe(baserule.BaseRule):
    def __init__(self, cid, ps):
        super().__init__(cid, ps)
        self.description = "TPM2 Check Safe == 1"

    def apply(self):
        try:
            trusted = (
                self.claim["payload"]["payload"]["quote"]["clockInfo"]["safe"] == 1
            )
        except KeyError:
            return self.returnMessage(
                a10.structures.constants.VERIFYERROR,
                "Missing safe value in quote. Is this a valid TPMS_ATTEST structure?",
                [],
            )

        if trusted == True:
            return self.returnMessage(
                a10.structures.constants.VERIFYSUCCEED, "Safe", []
            )
        else:
            return self.returnMessage(a10.structures.constants.VERIFYFAIL, "Unsafe", [])


#
# Bigger, Complex and Structured Rules
#


class TPM2QuoteStandardVerify(baserule.BaseRule):
    NAME = "tpm2rules/TPM2QuoteStandardVerify"
    DESCRIPTION = "TPM2 Check the quote for its overall integrity, including type, magic number, safe, attestedValue and firmware"

    def __init__(self, cid, ps):
        super().__init__(cid, ps)

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

        # Check if anything failed = NORESULT
        # Usually a missing expected value by applying the a wrong policy

        if (
            (magicRule_result["result"] == a10.structures.constants.VERIFYNORESULT)
            or (quoteType_result["result"] == a10.structures.constants.VERIFYNORESULT)
            or (safe_result["result"] == a10.structures.constants.VERIFYNORESULT)
            or (av_result["result"] == a10.structures.constants.VERIFYNORESULT)
            or (fw_result["result"] == a10.structures.constants.VERIFYNORESULT)
        ):
            return self.returnMessage(
                a10.structures.constants.VERIFYNORESULT,
                "Subrule failed - see additional section",
                subresults,
            )

        # Check if anything failed = ERROR
        # For some other reason

        if (
            (magicRule_result["result"] == a10.structures.constants.VERIFYERROR)
            or (quoteType_result["result"] == a10.structures.constants.VERIFYERROR)
            or (safe_result["result"] == a10.structures.constants.VERIFYERROR)
            or (av_result["result"] == a10.structures.constants.VERIFYERROR)
            or (fw_result["result"] == a10.structures.constants.VERIFYERROR)
        ):
            return self.returnMessage(
                a10.structures.constants.VERIFYERROR,
                "Subrule failed - see additional section",
                subresults,
            )

        # Ok, now check if everything went well or not, ie: trusted yay or nay!

        trusted = (
            (magicRule_result["result"] == a10.structures.constants.VERIFYSUCCEED)
            and (quoteType_result["result"] == a10.structures.constants.VERIFYSUCCEED)
            and (safe_result["result"] == a10.structures.constants.VERIFYSUCCEED)
            and (av_result["result"] == a10.structures.constants.VERIFYSUCCEED)
            and (fw_result["result"] == a10.structures.constants.VERIFYSUCCEED)
        )

        msg = (
            "Additional contains "
            + str(len(subresults))
            + " items. Expected value in subrule - see additional section"
        )

        if trusted == True:
            return self.returnMessage(
                a10.structures.constants.VERIFYSUCCEED, msg, subresults
            )
        else:
            return self.returnMessage(
                a10.structures.constants.VERIFYFAIL, msg, subresults
            )
