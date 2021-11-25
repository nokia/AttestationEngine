# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import a10.structures.constants
import a10.structures.identity
import a10.structures.returncode
import a10.asvr.db.core
import a10.asvr.db.announce


def addExpectedValue(e):
    """Adds an expected value structure

    :param ExpectedValue e: the expected value as a dict. Must not contain an itemid - as this will be overwritten with a generated ID.
    :return: returns Success and the generated itemid, or ADDITEMFAIL and a message
    :rtype: ReturnCode
    """

    i = a10.structures.identity.generateID()
    e["itemid"] = i
    r = a10.asvr.db.core.addExpectedValue(e)
    if r == True:
        a10.asvr.db.announce.announceItemManagement("add", {"type": "ev", "itemid": i})
        return a10.structures.returncode.ReturnCode(a10.structures.constants.SUCCESS, i)
    else:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.ADDITEMFAIL, "ExpectedValue not added to database"
        )


def getExpectedValue(i):
    """Returns an expected value structure

    :param itemid i: the itemID of the expected value
    :return: returns Success and a dictionary of the expected value, or ADDITEMFAIL and a message
    :rtype: ReturnCode
    """

    e = a10.asvr.db.core.getExpectedValue(i)
    if e == None:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.ITEMDOESNOTEXIST, i
        )
    else:
        return a10.structures.returncode.ReturnCode(a10.structures.constants.SUCCESS, e)


def getExpectedValuesFull():
    """Returns all the expected values in the database

    :return: list of expected values
    :rtype: list dict
    """

    evs = a10.asvr.db.core.getExpectedValuesFull()
    return evs


def getExpectedValuesForElement(e):
    """Returns all the expected values in the database which refer to a given element itemID

    :param itemid i: the itemID of the element
    :return: the expectedvalue
    :rtype: ReturnCode
    """

    evs = a10.asvr.db.core.getExpectedValuesForElement(e)
    return evs


def getExpectedValuesForPolicy(p):
    """Returns all the expected values in the database which refer to a given policy itemID

    :param itemid i: the itemID of the policy
    :return:  the expectedvalue
    :rtype: ReturncCode
    """

    evs = a10.asvr.db.core.getExpectedValuesForPolicy(p)
    return evs


def getExpectedValueForElementAndPolicy(e, p):
    """Returns an expected value structure

    :param itemid e: the itemID of the element
    :param itemid p: the itemID of the policy
    :return: returns Success and a dictionary of the expected value, or ADDITEMFAIL and a message
    :rtype: ReturnCode
    """

    ev = a10.asvr.db.core.getExpectedValueForElementAndPolicy(e, p)
    if ev == None:
        m = str({"elementID": e, "policyID": p})
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.ITEMDOESNOTEXIST, m
        )
    else:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.SUCCESS, ev
        )


def deleteExpectedValue(i):
    """Adds an expected value structure

    :param itemid i: an itemid of an expected value
    :return: returns Success and the generated itemid, or DELETEITEMFAIL and a message
    :rtype: ReturnCode
    """

    r = a10.asvr.db.core.deleteExpectedValue(i)

    if r == True:
        a10.asvr.db.announce.announceItemManagement(
            "delete", {"type": "ev", "itemid": i}
        )
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.SUCCESS, "Successfully deleted policy"
        )
    else:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.DELETEITEMFAIL, "Deletion failed."
        )


def updateExpectedValue(i):
    """Adds an expected value structure

    :param ExpectedValue e: the expected value as a dict. Must contain an itemid of the item to be updated.
    :return: returns Success and the generated itemid, or ADDITEMFAIL and a message
    :rtype: ReturnCode
    """
    r = a10.asvr.db.core.updateExpectedValue(i)

    if r == True:
        a10.asvr.db.announce.announceItemManagement(
            "update", {"type": "ev", "itemid": i}
        )
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.SUCCESS, "ExpectedValue updated"
        )
    else:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.UPDATEITEMFAIL, "Element not modified"
        )
