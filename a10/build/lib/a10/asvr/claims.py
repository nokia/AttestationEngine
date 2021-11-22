# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import a10.asvr.db.core
import a10.structures.identity
import a10.structures.constants
import a10.structures.returncode
import a10.asvr.db.announce


def addClaim(e):
    """
    Adds a claim to the database. The following fields MUST be present:  payload, header.as_requested, header.as_received,
    header.element, header.policy

    In the future we will add header.ta_received, header.ta_complete

    :params dict e: a dictionary with claim informatino
    :return: the itemid of the claim on success
    :rtype: ReturnCode
    """

    try:
        # Force an exception if any of the following fields are missing
        tmp = e["header"]["as_requested"]
        tmp = e["header"]["as_received"]
        # tmp = e["header"]["ta_received"]
        # tmp = e["header"]["ta_complete"]
        tmp = e["header"]["element"]
        tmp = e["header"]["policy"]
        tmp = e["payload"]
    except KeyError as err:
        a10.announce.announceClaim(
            "error", {"msg": "Missing fields", "content": (str(err)), "itemid": "X"}
        )
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.MISSINGFIELDS, "Missing fields " + (str(err))
        )
    except Exception as err:
        a10.announce.announceClaim(
            "error", {"msg": "General error:", "content": (str(err)), "itemid": "X"}
        )
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.GENERALERROR, "General error " + (str(err))
        )

    i = a10.structures.identity.generateID()
    e["itemid"] = i

    r = a10.asvr.db.core.addClaim(e)

    if r == True:
        a10.asvr.db.announce.announceClaim("add", {"type": "claim", "itemid": i})
        return a10.structures.returncode.ReturnCode(a10.structures.constants.SUCCESS, i)
    else:
        a10.asvr.db.announce.announceClaim(
            "add", {"msg": "Claim not added to database", "itemid": i}
        )
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.ADDITEMFAIL, "Claim not added to database"
        )


def getClaim(i):
    """
    Gets a claim from the database with the given itemid

    :params str i: an itemID of a claim
    :return: the claim as a dictionary
    :rtype: ReturnCode
    """

    e = a10.asvr.db.core.getClaim(i)
    if e == None:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.ITEMDOESNOTEXIST, "Element does not exist"
        )
    else:
        return a10.structures.returncode.ReturnCode(a10.structures.constants.SUCCESS, e)


def getClaims(n):
    """
    Gets a list of claim itemids from the database 

    :return: the claim itemids as a list
    :rtype: list str
    """

    cs = a10.asvr.db.core.getClaims(n)
    return cs


def getClaimsFull(n=50):
    """
    Gets a list of claims from the database 

    :params int n: limit to n results
    :return: list of claim structures as a dict
    :rtype: list dict
    """

    cs = a10.asvr.db.core.getClaimsFull(n)
    return cs


def getAssociatedResults(i):
    """
    Gets a list of results associated with the given claim id from the database 

    :params int n: limit to n results
    :return: list of result structures as a dict
    :rtype: list dict
    """

    rs = a10.asvr.db.core.getAssociatedResults(i)
    return rs
