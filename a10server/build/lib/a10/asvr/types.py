# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import a10.structures.constants
import a10.structures.identity
import a10.structures.returncode
import a10.asvr.db.core
import a10.asvr.db.announce

import a10.asvr.elements


def getTypes():
    """Gets a list of all currently used types 

    :return: set of types
    :rtype: Set

    """

    ts = set()
    es = a10.asvr.elements.getElementsFull()

    for e in es:
        #print("type=", e["type"],type(e["type"]))
        # ugly - we keep everything as a flat list
        for t in e["type"]:
            ts.add(t)
        #print("ts  =", ts)
        # otherwise the set(List) breaks
        # warned you!

    return ts
