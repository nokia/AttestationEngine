#Copyright 2021 Nokia
#Licensed under the BSD 3-Clause Clear License.
#SPDX-License-Identifier: BSD-3-Clear

import json
import requests
import subprocess
import tempfile
import secrets
import string
import base64

import a10.asvr.protocols.A10ProtocolBase

import a10.structures.constants
import a10.structures.returncode


class A10Keylime(a10.asvr.protocols.A10ProtocolBase.A10ProtocolBase):
    NAME = "A10Keylime"

    def __init__(self, endpoint, policyintent, policyparameters, callparameters):
        super().__init__(endpoint, policyintent, policyparameters, callparameters)

    def exec(self):

    
        #
        # We only support quoting
        #

        if self.policyintent!="tpm2/quote":
            return a10.structures.returncode.ReturnCode(
                    a10.structures.constants.PROTOCOLEXECUTIONFAILURE, "Keylime supports tpm2/quote only"
                )
            

        #
        # Ok, now go on with the calling
        #

        returndata = ( {"msg":"nothing"},{} )


        #
        #  Call Keylime trust agent here
        #

        #
        #  Reformat it into the form that A10 expects
        #    - which is like what TPM2_Quote returns in JSON format
        #


        #
        # Return
        #
    
        return a10.structures.returncode.ReturnCode(
                a10.structures.constants.PROTOCOLSUCCESS, returndata
            )
        