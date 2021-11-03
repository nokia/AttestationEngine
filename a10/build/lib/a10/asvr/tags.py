# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import a10.structures.constants
import a10.structures.identity
import a10.structures.returncode
import a10.asvr.db.core
import a10.asvr.db.announce

import a10.asvr.elements

def getTags():
	"""Gets a list of all currently used tags 

	:return: list of tags
	:rtype: List

	"""

	ts = []
	es = a10.asvr.elements.getElementsFull()

	for e in es:
		ts.append(e['tags'])

	return set(ts)