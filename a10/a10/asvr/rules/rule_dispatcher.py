# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

# from a10.asvr.protocols import A10HttpRest, A10ContainerImage, A10ArduinoUSB
# from a10.asvr.protocols import A10DummyProtocol

import a10.asvr.rules.nullrules
import a10.asvr.rules.tpm2rules
import a10.asvr.rules.uefi

import a10.structures.constants
import a10.structures.returncode

RULEREGISTER = {
    a10.asvr.rules.nullrules.AlwaysSuccess.NAME: (
        a10.asvr.rules.nullrules.AlwaysSuccess,
        a10.asvr.rules.nullrules.AlwaysSuccess.DESCRIPTION,
    ),
    a10.asvr.rules.nullrules.AlwaysFail.NAME: (
        a10.asvr.rules.nullrules.AlwaysFail,
        a10.asvr.rules.nullrules.AlwaysFail.DESCRIPTION,
    ),
    a10.asvr.rules.nullrules.AlwaysError.NAME: (
        a10.asvr.rules.nullrules.AlwaysError,
        a10.asvr.rules.nullrules.AlwaysError.DESCRIPTION,
    ),
    a10.asvr.rules.nullrules.AlwaysNoResult.NAME: (
        a10.asvr.rules.nullrules.AlwaysNoResult,
        a10.asvr.rules.nullrules.AlwaysNoResult.DESCRIPTION,
    ),
    a10.asvr.rules.tpm2rules.PCRsAllUnassigned.NAME: (
        a10.asvr.rules.tpm2rules.PCRsAllUnassigned,
        a10.asvr.rules.tpm2rules.PCRsAllUnassigned.DESCRIPTION,
    ),
    a10.asvr.rules.tpm2rules.TPM2FirmwareVersion.NAME: (
        a10.asvr.rules.tpm2rules.TPM2FirmwareVersion,
        a10.asvr.rules.tpm2rules.TPM2FirmwareVersion.DESCRIPTION,
    ),
    a10.asvr.rules.tpm2rules.TPM2QuoteAttestedValue.NAME: (
        a10.asvr.rules.tpm2rules.TPM2QuoteAttestedValue,
        a10.asvr.rules.tpm2rules.TPM2QuoteAttestedValue.DESCRIPTION,
    ),
    a10.asvr.rules.tpm2rules.TPM2QuoteStandardVerify.NAME: (
        a10.asvr.rules.tpm2rules.TPM2QuoteStandardVerify,
        a10.asvr.rules.tpm2rules.TPM2QuoteStandardVerify.DESCRIPTION,
    ),
    a10.asvr.rules.uefi.ValidUEFIEventLog.NAME: (
        a10.asvr.rules.uefi.ValidUEFIEventLog,
        a10.asvr.rules.uefi.ValidUEFIEventLog.DESCRIPTION,
    ),
}


def getRegisteredRules():
    """
	Returns all the registered rules
	
	:returns: a list of all registered names
	:rtype: list
	"""

    return RULEREGISTER.keys()


def getRuleDescription(n):
    try:
        print("Getting handler")
        p = RULEREGISTER[n][1]
        print("Found handler ", n)
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.RULESUCCESS, p
        )
    except KeyError as err:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.UNKNOWNRULE, "Unregistered Protocol " + (str(err))
        )
    except Exception as err:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.GENERALERROR, "General error " + (str(err))
        )


def getRuleHandler(n):
    """
	Returns all the registered rules
	
	:returns: the class of the rule handler if successful otherwise an errorcode of UNREGISTEREDPROTOCOL.
	:rtype: ResultCode
	"""
    try:
        print("Getting handler")
        p = RULEREGISTER[n][0]
        print("Found handler ", n)
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.RULESUCCESS, p
        )
    except KeyError as err:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.UNKNOWNRULE, "Unregistered Protocol " + (str(err))
        )
    except Exception as err:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.GENERALERROR, "General error " + (str(err))
        )
