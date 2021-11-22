# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import uuid


def generateID():
    """Returns a UUID4 to be used as an identifier.

	:return: a UUID4
	:rtype: str
	"""

    return str(uuid.uuid4())
