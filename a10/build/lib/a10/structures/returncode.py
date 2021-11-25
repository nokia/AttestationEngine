# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause


class ReturnCode:
    """
	This is the return code structure used in A10 libraries
	
	"""

    def __init__(self, rc, msg):
        """
	   Initialises the ReturnCode object.
	   
	   :param int rc: The result code, found in constants
	   :param str msg: The human (maybe) readable message to be included
		"""

        self.resultcode = rc
        self.message = msg

    def rc(self):
        """
		Returns the result code
		
		:return: the result code
		:rtype: int
		"""

        return self.resultcode

    def msg(self):
        """
		Returns the message
		
		:return: the message
		:rtype: msg
		"""

        return self.message
