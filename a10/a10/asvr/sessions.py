# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import a10.structures.constants
import a10.structures.identity
import a10.structures.returncode
import a10.structures.timestamps
import a10.asvr.db.core
import a10.asvr.db.announce


def openSession():
    """
    Opens a session and returns an itemid for that session
    """
    i = a10.structures.identity.generateID()

    # sessions have another property, closed, which if it exists contains the
    # timestamp of the session's closing

    s={ "itemid":i, "opened": a10.structures.timestamps.now(),
        "claims":[], 
        "results":[] }

    r = a10.asvr.db.core.openSession(s)

    if r == True:
        a10.asvr.db.announce.announceItemManagement(
            "add", {"type": "session", "itemid": i}
        )
        return a10.structures.returncode.ReturnCode(a10.structures.constants.SUCCESS, i)
    else:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.ADDITEMFAIL, "Session openening failed"
        )


def closeSession(sid):
    """
    s is a session itemid
    """

    s = getSession(sid)
    if s.rc() == a10.structures.constants.ITEMDOESNOTEXIST:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.ITEMDOESNOTEXIST, "Session does not exist"
        )

    ses = s.msg()

    try:
        x=ses["closed"]
    except KeyError:
        # Key Error hopefully
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.SESSIONALREADYCLOSED, "Session already closed"
        )


    ses["closed"] = a10.structures.timestamps.now()

    u = a10.asvr.db.core.closeSession(ses)
    
    if u == True:
        # need to check if this an archived element
        a10.asvr.db.announce.announceItemManagement(
            "update", {"type": "session", "itemid": ses["itemid"]}
        )
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.SUCCESS, "Session closed"
        )
    else:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.UPDATEITEMFAIL, "Session not modified"
        )


def getOpenSessions():
    """
    Returns all open sessions
    """
    ss = a10.asvr.db.core.getSessions(closed=False)
    return ss

def getClosedSessions():
    """
    Returns all open sessions
    """
    ss = a10.asvr.db.core.getSessions(closed=True)
    return ss


def getSession(s):
    """
    Returns all - by default - open sessions for a given element
    """
    e = a10.asvr.db.core.getSession(s)
    
    if e is None:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.ITEMDOESNOTEXIST, "Session does not exist"
        )
    else:
        return a10.structures.returncode.ReturnCode(a10.structures.constants.SUCCESS, e)

def associateClaim(s,c):
    """
    Associates a claim with an open session
    """
    pass

