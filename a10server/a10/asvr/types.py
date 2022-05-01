# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import a10.asvr.elements


def getTypes():
    """Gets a list of all currently used types 

    :return: set of types
    :rtype: Set

    """

    ts = set()
    es = a10.asvr.elements.getElementsFull()

    for e in es:
        for t in e["type"]:
            ts.add(t)

    return ts
