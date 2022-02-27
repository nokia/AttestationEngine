# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import a10.structures.constants
import a10.structures.identity
import a10.structures.returncode
import a10.asvr.db.core
import a10.asvr.db.announce


def addPolicy(e):
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

    i = a10.structures.identity.generateID()
    e["itemid"] = i
    r = a10.asvr.db.core.addPolicy(e)
    if r == True:
        a10.asvr.db.announce.announceItemManagement(
            "add", {"type": "policy", "itemid": i}
        )
        return a10.structures.returncode.ReturnCode(a10.structures.constants.SUCCESS, i)
    else:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.ADDITEMFAIL, "Policy not added to database"
        )


def getPolicy(i):
    e = a10.asvr.db.core.getPolicy(i)
    if e == None:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.ITEMDOESNOTEXIST, i
        )
    else:
        return a10.structures.returncode.ReturnCode(a10.structures.constants.SUCCESS, e)


def getPolicyByName(n):
    """Gets a single policy from the database by its name.

    :param str n: the policy's name
    :return: return code structure
    :rtype: ResultCode
    """

    e = a10.asvr.db.core.getPolicyByName(n)
    if e is None:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.ITEMDOESNOTEXIST, "Element does not exist"
        )
    else:
        return a10.structures.returncode.ReturnCode(a10.structures.constants.SUCCESS, e)


def getPolicies():
    ps = a10.asvr.db.core.getPolicies()
    return ps


def getPoliciesFull():
    ps = a10.asvr.db.core.getPoliciesFull()
    return ps


def deletePolicy(i):
    # itemid MUST be present

    r = a10.asvr.db.core.deletePolicy(i)

    if r == True:
        a10.asvr.db.announce.announceItemManagement(
            "delete", {"type": "policy", "itemid": i}
        )
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.SUCCESS, "Successfully deleted policy"
        )
    else:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.DELETEITEMFAIL, "Deletion failed."
        )


def updatePolicy(i):
    # itemid MUST be present
    #
    # Parameters: e - is a dictionary containing the element information
    #
    # The following fields must be present:   itemid
    #
    # The return is   msg, err
    # Success if err is None
    # Failure if err is True
    #
    #

    r = a10.asvr.db.core.updatePolicy(i)

    if r == True:
        a10.asvr.db.announce.announceItemManagement(
            "update", {"type": "policy", "itemid": i}
        )
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.SUCCESS, "Policy updated"
        )
    else:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.UPDATEITEMFAIL, "Element not modified"
        )
