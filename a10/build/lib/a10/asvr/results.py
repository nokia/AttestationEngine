# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import a10.asvr.db.core
import a10.structures.identity
import a10.structures.constants
import a10.structures.returncode
import a10.asvr.db.announce


def addResult(e):
    #
    # Parameters: e - is a dictionary containing the policy information
    #
    # The following fields must be present:  type, name
    #
    # The return is   msg, err
    # Success if err is None
    # Failure if err is True
    #
    #

    try:
        # Force an exception if any of the following fields are missing
        tmp = e["verifiedAt"]
        tmp = e["claimID"]
        tmp = e["elementID"]
        tmp = e["policyID"]
        tmp = e["result"]
        tmp = e["message"]
        tmp = e["additional"]
        tmp = e["ruleParameters"]
        tmp = e["ev"]
    except KeyError as err:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.MISSINGFIELDS, "Missing fields " + (str(err))
        )
    except Exception as err:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.GENERALERROR, "General error " + (str(err))
        )

    i = a10.structures.identity.generateID()
    e["itemid"] = i

    r = a10.asvr.db.core.addResult(e)

    if r == True:
        a10.asvr.db.announce.announceResult(
            "add", {"type": "result", "itemid": i, "result": e["result"]}
        )
        return a10.structures.returncode.ReturnCode(a10.structures.constants.SUCCESS, i)
    else:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.ADDITEMFAIL, "Result not added to database"
        )


def getResult(i):
    e = a10.asvr.db.core.getResult(i)
    if e == None:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.ITEMDOESNOTEXIST, "Element does not exist"
        )
    else:
        return a10.structures.returncode.ReturnCode(a10.structures.constants.SUCCESS, e)


def getResults(n):
    rs = list(a10.asvr.db.core.getResults(n))
    return rs


def getResultsFull(n):
    rs = list(a10.asvr.db.core.getResultsFull(n))
    return rs


def getLatestResults(e, n=10):
    """
	Returns the latest n results for an element sorted by the verifiedAt property of the results

	:params str e: the element id
	:params int n: the number of elements, defaults to 10
	:return: the set of results
	:rtype: list dict
	"""
    rs = a10.asvr.db.core.getLatestResults(e, n)
    return rs


def getLatestResultsForElementAndPolicy(e, p, n=10):
    """
	Returns the latest n results for an element sorted by the verifiedAt property of the results

	:params str e: the element id
	:params str p: the policy id	
	:params int n: the number of elements, defaults to 10
	:return: the set of results
	:rtype: list dict
	"""
    rs = a10.asvr.db.core.getLatestResultsForElementAndPolicy(e, p, n)
    return rs
