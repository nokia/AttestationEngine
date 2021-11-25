# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import a10.structures.constants
import a10.structures.identity
import a10.structures.returncode
import a10.asvr.db.core
import a10.asvr.db.announce


def addHash(h):
    """Adds a hash to the list of hashes

	:param str h: A hash structure - which must not already exist
	:return: SUCCESS or ADDITEMFAIL
	:rtype: ReturnCode
	"""

    try:
        # Force an exception if any of the following fields are missing
        tmp = h["hash"]
        tmp = h["type"]
        tmp = h["short"]
        tmp = h["long"]
    except KeyError as err:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.MISSINGFIELDS, "Missing fields " + (str(err))
        )
    except Exception as err:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.GENERALERROR, "General error " + (str(err))
        )

    if getHash(h["hash"]).rc() == a10.structures.constants.SUCCESS:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.ADDITEMFAIL,
            "Hash " + h["hash"] + " already exists",
        )

    r = a10.asvr.db.core.addHash(h)
    if r == True:
        a10.asvr.db.announce.announceItemManagement(
            "add", {"type": "hash", "itemid": h}
        )
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.SUCCESS, "Hash " + h["hash"] + " added"
        )
    else:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.ADDITEMFAIL,
            "Hash not added to database - database error?",
        )


def getHash(h):
    e = a10.asvr.db.core.getHash(h)
    if e == None:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.ITEMDOESNOTEXIST, h
        )
    else:
        return a10.structures.returncode.ReturnCode(a10.structures.constants.SUCCESS, e)


def getHashes():
    hs = a10.asvr.db.core.getHashes()
    return hs


def getHashesFull():
    hs = a10.asvr.db.core.getHashesFull()
    return hs
