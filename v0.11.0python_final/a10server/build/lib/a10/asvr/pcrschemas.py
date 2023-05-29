# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import a10.structures.constants
import a10.structures.identity
import a10.structures.returncode
import a10.asvr.db.core
import a10.asvr.db.announce


def addPCRSchema(h):
    """Adds a hash to the list of hashes

	:param str h: A hash structure - which must not already exist
	:return: SUCCESS or ADDITEMFAIL
	:rtype: ReturnCode
	"""

    try:
        # Force an exception if any of the following fields are missing
        tmp = h["name"]
        tmp = h["description"]
        tmp = h["pcrs"]
    except KeyError as err:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.MISSINGFIELDS, "Missing fields " + (str(err))
        )
    except Exception as err:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.GENERALERROR, "General error " + (str(err))
        )

    if getPCRSchema(h["name"]).rc() == a10.structures.constants.SUCCESS:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.ADDITEMFAIL,
            "PCR schema " + h["name"] + " already exists",
        )

    r = a10.asvr.db.core.addPCRSchema(h)
    if r == True:
        a10.asvr.db.announce.announceItemManagement(
            "add", {"type": "pcrschema", "itemid": h}
        )
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.SUCCESS, "PCR SChema " + h["name"] + " added"
        )
    else:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.ADDITEMFAIL,
            "PCR Schema not added to database - database error?",
        )


def getPCRSchema(h):
    e = a10.asvr.db.core.getPCRSchema(h)
    if e == None:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.ITEMDOESNOTEXIST, h
        )
    else:
        return a10.structures.returncode.ReturnCode(a10.structures.constants.SUCCESS, e)


def getPCRSchemas():
    hs = a10.asvr.db.core.getPCRSchemas()
    return hs


def getPCRSchemasFull():
    hs = a10.asvr.db.core.getPCRSchemasFull()
    return hs
