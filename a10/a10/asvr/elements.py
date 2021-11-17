# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import a10.structures.constants
import a10.structures.identity
import a10.structures.returncode
import a10.asvr.db.core
import a10.asvr.db.announce

import pymongo


def addElement(e):
    """Adds an element of basetype Element to the attestation database.

	Unless there is some database failure this function will always add a new element with a new itemid
	if the syntax check is successful.

	The syntax check involves ensuring that the necessary base fields have been set.

	:param dict e: The element parameter
	:return: return code structure
	:rtype: ResultCode with either the itemID as a string in case of success or a string with an error message otherwise

	"""

    i = a10.structures.identity.generateID()
    e["itemid"] = i
    r = a10.asvr.db.core.addElement(e)
    if r == True:
        a10.asvr.db.announce.announceItemManagement(
            "add", {"type": "element", "itemid": i}
        )
        return a10.structures.returncode.ReturnCode(a10.structures.constants.SUCCESS, i)
    else:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.ADDITEMFAIL, "Element not added to database"
        )

    # This is what should be written
    # if e.syntaxCheck()==True:
    # 	i = a10.structures.identity.generateID()
    # 	e.setItemID(i)
    # 	r = a10.asvr.db.core.addElement(e,i)
    # 	if r==True:
    # 		return a10.asvr.structures.ReturnCode( a10.structures.constants.SUCCESS, i )
    # 	else:
    # 		return a10.asvr.structures.ReturnCode( a10.structures.constants.ADDITEMFAIL, "Item not added to database" )
    # else:
    # 	return a10.asvr.structures.ReturnCode( a10.structures.constants.SYNTAXERROR, "Failed Syntax Check "+e.syntaxCheckOutput() )


def getElement(i):
    """Gets a single element from the database by its itemID.

	:param str i: the element ID, usually a UUID as a string or whatever is defined in a10.structures.identity
	:return: return code structure
	:rtype: ResultCode
	"""

    e = a10.asvr.db.core.getElement(i)
    if e is None:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.ITEMDOESNOTEXIST, "Element does not exist"
        )
    else:
        return a10.structures.returncode.ReturnCode(a10.structures.constants.SUCCESS, e)


def getElementByName(n):
    """Gets a single element from the database by its name.

	:param str n: the element's name
	:return: return code structure
	:rtype: ResultCode
	"""

    e = a10.asvr.db.core.getElementByName(n)
    if e is None:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.ITEMDOESNOTEXIST, "Element does not exist"
        )
    else:
        return a10.structures.returncode.ReturnCode(a10.structures.constants.SUCCESS, e)


def getElements():
    """Returns a list of elementIDs

    :return: a list of elementIDs
    :rtype: str list
	"""
    es = a10.asvr.db.core.getElements()
    return es


def getElementsFull():
    """Returns a list of everything in the database.

    :return: a list of elementIDs
    :rtype: str list
	"""
    es = a10.asvr.db.core.getElementsFull()
    return es


def updateElement(e):
    """Modifies a given element. The element *must* contain a valid itemid

	:param Element e: The element parameter
	:return: return code structure
	:rtype: ResultCode with either the itemID as a string in case of success or a string with an error message otherwise

	"""
    r = a10.asvr.db.core.updateElement(e)
    if r == True:
        a10.asvr.db.announce.announceItemManagement(
            "update", {"type": "element", "itemid": e["itemid"]}
        )
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.SUCCESS, "Element updated"
        )
    else:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.UPDATEITEMFAIL, "Element not modified"
        )


def deleteElement(i):
    """Deletes a single element from the database by its itemID.

	:param str i: the element ID, usually a UUID as a string or whatever is defined in a10.structures.identity
	:return: return code structure
	:rtype: ResultCode
	"""
    e = a10.asvr.db.core.deleteElement(i)
    if e is True:
        a10.asvr.db.announce.announceItemManagement(
            "delete", {"type": "element", "itemid": i}
        )
        return a10.structures.returncode.ReturnCode(a10.structures.constants.SUCCESS, e)
    else:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.DELETEITEMFAIL, "Deletion failed."
        )
