# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import a10.structures.timestamps
import a10.asvr.results

from collections import Counter


def getResultCounts(e, n=250):
    """Returns the counts of result codes for a given element.


	"""
    rs = a10.asvr.results.getLatestResults(e, n)
    res = [x["result"] for x in rs]
    return Counter(res)


def getResultCountsByPolicy(e, p, n=250):
    """
	Returns the counts of result codes for a given element and a given policy


	"""
    rs = a10.asvr.results.getLatestResultsForElementAndPolicy(e, p, n)
    res = [x["result"] for x in rs]
    return Counter(res)
