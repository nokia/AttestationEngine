# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import a10.structures.constants
import a10.structures.identity
import a10.structures.returncode
import a10.structures.timestamps
import a10.asvr.db.core

def getLogEntriesForItemID(i):
    """ Returns the log entries with a field
         data.itemid == i

    :params str i: itemid
    :returns: list of log entries
    :rtype: list dict
    """
    es = a10.asvr.db.core.getLogEntriesForItemID(i)
    return es