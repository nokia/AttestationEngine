#Copyright 2021 Nokia
#Licensed under the BSD 3-Clause Clear License.
#SPDX-License-Identifier: BSD-3-Clear

#import json
#import requests
import subprocess
#import string
import datetime
import yaml
from urllib.parse import urlparse

import a10.asvr.protocols.A10ProtocolBase

import a10.structures.constants
import a10.structures.returncode


class A10tpm2send(a10.asvr.protocols.A10ProtocolBase.A10ProtocolBase):
    NAME = "A10TPMSENDSSL"

    def __init__(self, endpoint, policyintent, policyparameters, callparameters):
        super().__init__(endpoint, policyintent, policyparameters, callparameters)

    def exec(self):

      
        transientdata = {}

        print(
            "Calling protocol A10TPMSENDSSL ",
            self.endpoint,
            self.policyintent,
            self.policyparameters,
            self.callparameters,
        )
        
        rc = None
        
        if self.policyintent=="tpm2/pcrs":
            rc = self.tpm2pcrs()
        else:
            rc = a10.structures.returncode.ReturnCode(
                a10.structures.constants.PROTOCOLFAIL, ({"msg":"unsupported intent -> "+self.policyintent},transientdata)
            )
       
        return rc
      
    #
    # Utility functions
    #
      
    def getTCTI(self):
        #
        # The TCTI is created from the element description
        # and contains the key and the username and the endpoint IP address
        # which are then passed to ssh in the form
        #
        # cmd:ssh <username>@<ip> -i <key> tpm2_send
        #
        # For example "cmd:ssh pi@10.144.176.152 -i /var/a10keystore/lassa tpm2_send"
        #
        key = self.callparameters["a10_tpm_send_ssl"]["key"]
        username = self.callparameters["a10_tpm_send_ssl"]["username"]
        ip = urlparse(self.endpoint).netloc

        tcti = "cmd:ssh "+username+"@"+ip+" -i "+key+" tpm2_send"
        print("CONSTRUCTED TCTI IS ",tcti)

        return tcti

    #
    # These functions implement the specific commands by calling out to tpm2_tools installed locally
    #
    # I really should add the Claim structure to the generic set of things instead of being buried away in nut10 somewhere...future TODO
    #
    # Each will return a ReturnCode complete with a Claim structure, if successful
    #  
      
    def tpm2pcrs(self):
        claim = { "header": {
                "ta_received": str(datetime.datetime.now(datetime.timezone.utc))
             },
             "payload":{},
             "signature":{}
           }

        # Now create the command

        cmd = 'tpm2_pcrread'

        try:
            cmdwtcti = cmd.split()+["-T",self.getTCTI()]
            print("Trying ",cmdwtcti)
            #out = subprocess.check_output(
            #    cmdwtcti, stderr=subprocess.STDOUT, shell=True, timeout=3,
            #    universal_newlines=True)
            out = subprocess.check_output(cmdwtcti, stderr=subprocess.STDOUT, timeout=3) 
        except subprocess.CalledProcessError as exc:
            #print("Status : FAIL", exc.returncode, exc.out)
            claim['payload']={"msg":"Command failed to execute","out":exc.out,"process_return_code":exc.retyrbcide}
        else:
            claim['payload']['pcrs']=yaml.load(out, Loader=yaml.BaseLoader)
            #print("Output: \n{}\n".format(out))


        #claim['payload']=out

        # and return

        claim["header"]["ta_complete"] = str(datetime.datetime.now(datetime.timezone.utc))

        return a10.structures.returncode.ReturnCode(
                a10.structures.constants.PROTOCOLSUCCESS, (claim,{})
            )    
