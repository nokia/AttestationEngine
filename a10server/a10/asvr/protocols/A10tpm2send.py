#Copyright 2021 Nokia
#Licensed under the BSD 3-Clause Clear License.
#SPDX-License-Identifier: BSD-3-Clear

import subprocess
import datetime
import yaml
import binascii
import tempfile
import string

from tpm2_pytss import *
from tpm2_pytss.utils import *

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

        #print(
        #    "Calling protocol A10TPMSENDSSL ",
        #    "endpoint ",self.endpoint,"\n",
        #    "intent ",self.policyintent,"\n",
        #    "polparams ",self.policyparameters,"\n",
        #    "cpsparams ",self.callparameters,"\n"
        #)
        
        rc = None
        
        if self.policyintent=="tpm2/pcrs":
            rc = self.tpm2pcrs()
        elif self.policyintent=="tpm2/quote":
            rc = self.tpm2quote()
        elif self.policyintent=="tpm2/credentialcheck":
            rc = self.tpm2credentialcheck()
        elif self.policyintent=="sys/info":
            rc = self.sysinfo()
        else:
            print(" Intent not understood")
            rc = a10.structures.returncode.ReturnCode(
                a10.structures.constants.UNSUPPORTEDPROTOCOLINTENT, {"msg":"unsupported intent -> "+self.policyintent,"transientdata":transientdata}
            )
            print(" rc object ",rc)
       
        return rc
      
    #
    # Utility functions
    #

    def getTimeout(self):
        timeout = 20
        print("Timeout ",self.callparameters.get("a10_tpm_send_ssl"))
        if  self.callparameters.get("a10_tpm_send_ssl").get("timeout")!=None:
            timeout = self.callparameters.get("a10_tpm_send_ssl").get("timeout")
        return int(timeout)
      
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
        ip = urlparse(self.endpoint).hostname   # we need just the IP address, ssh does the rest

        tcti = "cmd:ssh "+username+"@"+ip+" -i "+key+" tpm2_send"
        print("CONSTRUCTED TCTI IS ",tcti)

        return tcti

    def getSSH(self):
        #
        # This is the structure used for the SSH command
        #    
        # eg: ssh <username>@<ip| -i <key>
        #
        key = self.callparameters["a10_tpm_send_ssl"]["key"]
        username = self.callparameters["a10_tpm_send_ssl"]["username"]
        ip = urlparse(self.endpoint).hostname   # we need just the IP address, ssh does the rest

        ssh = "ssh "+username+"@"+ip+" -i "+key+" "
        print("CONSTRUCTED SSH IS ",ssh)
        
        return ssh
    
    #
    # These functions implement the specific commands by calling out to tpm2_tools installed locally
    #
    # I really should add the Claim structure to the generic set of things instead of being buried away in nut10 somewhere...future TODO
    #
    # Each will return a ReturnCode complete with a Claim structure, if successful
    #  
      
    def processQuote(self,h):
        j = {}
        print("Quote part is ",h["quoted"])
        with tempfile.NamedTemporaryFile() as tf:
            b = binascii.a2b_hex(h["quoted"])
            tf.write(b)
            cmd = ["tpm2_print","-t","TPMS_ATTEST",tf.name]
            tf.seek(0)
            p = subprocess.Popen(cmd, stdin=subprocess.PIPE, stdout=subprocess.PIPE) 
            o = p.communicate(input=b)[0]
            j = yaml.load(o, Loader=yaml.BaseLoader)
        print("processed as ",type(j),j)
        return j

    def tpm2quote(self):
        claim = { "header": {
                "ta_received": str(datetime.datetime.now(datetime.timezone.utc)),
                "ssl_tpm2send_timeout":str(self.getTimeout())
             },
             "payload":{},
             "signature":{}
           }       

        print("\nquote policy parameters ",self.policyparameters)

        #resolve ak
        ak_to_use = "0x810100AA"

        cmd = "tpm2_quote -c "+ak_to_use+" -l "+self.policyparameters["pcrselection"]

        #TODO: this is a bit odd because the protocol returns always success...fix later

        #TODO: tpm2_quote returns a raw structure as YAML, this needs to be processed into a fuller description
        # that tpm2_print returns
        #Maybe use pytss?

        try:
            cmdwtcti = cmd.split()+["-T",self.getTCTI()]

            print("Trying ",cmdwtcti)

            out = subprocess.check_output(cmdwtcti, stderr=subprocess.STDOUT, timeout=self.getTimeout()) 
            print("OUT=",out)
        except subprocess.CalledProcessError as exc:
            print("Status : FAIL", exc)
            claim['payload']={"msg":"Command failed to execute","exc":str(exc)}
        except subprocess.TimeoutExpired as exc:
            claim['payload']={"msg":"Connection and/or processing timedout after "+str(self.getTimeout())+"seconds"} 
        else:
            quoteyaml = yaml.load(out, Loader=yaml.BaseLoader)
            claim['payload']['hexquote']=quoteyaml
            claim['payload']['quote']=self.processQuote(quoteyaml)


        # and return

        claim["header"]["ta_complete"] = str(datetime.datetime.now(datetime.timezone.utc))

        return a10.structures.returncode.ReturnCode(
                a10.structures.constants.PROTOCOLSUCCESS, {"claim":claim,"transientdata":{}}
            )    




    def tpm2pcrs(self):
        claim = { "header": {
                "ta_received": str(datetime.datetime.now(datetime.timezone.utc)),
                "ssl_tpm2send_timeout":str(self.getTimeout())
             },
             "payload":{},
             "signature":{}
           }

        # Now create the command

        cmd = 'tpm2_pcrread'

        #TODO: this is a bit odd because the protocol returns always success...fix later
        #TODO: remember what the above statement actually meant and whether I fixed it
        #TODO: also this comment got cut and pasted elsewhere, so .... no idea

        try:
            cmdwtcti = cmd.split()+["-T",self.getTCTI()]
            print("Trying ",cmdwtcti)

            out = subprocess.check_output(cmdwtcti, stderr=subprocess.STDOUT, timeout=self.getTimeout()) 
        except subprocess.CalledProcessError as exc:
            print("Status : FAIL", exc)
            claim['payload']={"msg":"Command failed to execute","exc":str(exc)}
        except subprocess.TimeoutExpired as exc:
            claim['payload']={"msg":"Connection and/or processing timedout after "+str(self.getTimeout())+"seconds"} 
        else:
            claim['payload']['pcrs']=yaml.load(out, Loader=yaml.BaseLoader)

        # and return

        claim["header"]["ta_complete"] = str(datetime.datetime.now(datetime.timezone.utc))

        return a10.structures.returncode.ReturnCode(
                a10.structures.constants.PROTOCOLSUCCESS, {"claim":claim,"transientdata":{}}
            )    

    def sysinfo(self):
        claim = { "header": {
                "ta_received": str(datetime.datetime.now(datetime.timezone.utc)),
                "ssl_tpm2send_timeout":str(self.getTimeout())
             },
             "payload":{},
             "signature":{}
           }

        # Now create the command

        cmd = 'uname -a'

        #TODO: this is a bit odd because the protocol returns always success...fix later
        #TODO: remember what the above statement actually meant and whether I fixed it
        #TODO: also this comment got cut and pasted elsewhere, so .... no idea

        try:
            cmdwtcti = (self.getSSH()+cmd).split()
            print("Trying ",cmdwtcti)

            out = subprocess.check_output(cmdwtcti, stderr=subprocess.STDOUT, timeout=self.getTimeout()) 

        except subprocess.CalledProcessError as exc:
            print("Status : FAIL", exc)
            claim['payload']={"msg":"Command failed to execute","exc":str(exc)}
        except subprocess.TimeoutExpired as exc:
            claim['payload']={"msg":"Connection and/or processing timedout after "+str(self.getTimeout())+"seconds"} 
        else:
            claim['payload']['systeminfo']={'uname':out.decode("utf-8")}
            print("PAYLOAD=",claim['payload'])
            

        # and return

        claim["header"]["ta_complete"] = str(datetime.datetime.now(datetime.timezone.utc))

        return a10.structures.returncode.ReturnCode(
                a10.structures.constants.PROTOCOLSUCCESS, {"claim":claim,"transientdata":{}}
            )    
    

    def tpm2credentialcheck(self):
        claim = { "header": {
                "ta_received": str(datetime.datetime.now(datetime.timezone.utc)),
                "ssl_tpm2send_timeout":str(self.getTimeout())
             },
             "payload":{},
             "signature":{}
           }    
        transientdata={}

        # now do the make credential bit locally
        # this is identical to the code in A10HttpRest.py    

        #cred,secret = self.makecredential()
        #if cred==None:
        #    return a10.structures.returncode.ReturnCode(
        #            a10.structures.constants.PROTOCOLEXECUTIONFAILURE, {"msg": "Makecredential failed","transientdata":transientdata}
        #        )

        # now call activecredential
        # see the code in nut10 for example

        #ekpub = self.callparameters["ekpub"]
        #akname = self.callparameters["akname"]

        ak_to_use = "0x810100aa"
        try:
            ak_to_use = self.callparameters["ak"]
        except KeyError:
            print("AK Missing , using  default of 0x810100aa")

        ek_to_use = "0x810100ee"
        try:
            ek_to_use = self.callparameters["ek"]
        except KeyError:
            print("EK Missing , using  default of 0x810100ee")


        remote_tpm = ESAPI(tcti=self.getTCTI())

        #print("Just a random number from remote :",remote_tpm.get_random(6))

#
# We'll ask the remote tpm what its AK and EK are
#
# This kind of information in stored in our attestation server elements table
#


        ek_handle =  remote_tpm.tr_from_tpmpublic(int(ek_to_use,16))
        ak_handle =  remote_tpm.tr_from_tpmpublic(int(ak_to_use,16))

        print("Call params ",self.callparameters)

        pem = self.callparameters["ekpub"]

        ek_pub = TPM2B_PUBLIC.from_pem(pem.encode('ascii'),
                 objectAttributes=TPMA_OBJECT.DECRYPT,
                 symmetric=TPMT_SYM_DEF_OBJECT(
                                algorithm=TPM2_ALG.AES,
                                keyBits=TPMU_SYM_KEY_BITS(aes=128),
                                mode=TPMU_SYM_MODE(aes=TPM2_ALG.CFB),
                                ),
                    scheme=  TPMT_ASYM_SCHEME()          

                 )

        # this is a bit horrible but
        rek_pub, rek_name, rek_qname = remote_tpm.read_public(ek_handle)
        ek_pub=rek_pub
        samepem = rek_pub.to_pem()==bytes(pem,'utf-8')
        print("PEMs:\n",samepem)

        if samepem==False:
            return a10.structures.returncode.ReturnCode(
                a10.structures.constants.PROTOCOLFAIL, {"msg":"pems are not the same","transientdata":{}}
            ) 

        ak_name_str = bytes(self.callparameters["akname"],'utf-8')
        ak_name = TPM2B_NAME(ak_name_str)

        #same as the above but core to check the same names is not written yet
        rak_pub, rak_name, rak_qname = remote_tpm.read_public(ak_handle)
        ak_name=rak_name
        print("AKNAMES ",rak_name.__str__() == ak_name.__str__())

#
# On the LOCAL machine, we want to make a credential.
#
# There are two options, comment one block out depending upon the actions required.
#


# This is out secret that we want to encrypt

        alphabet = string.ascii_letters + string.digits
        credential = "".join(secrets.choice(alphabet) for i in range(30))

        transientdata["secret"]=credential

        print("The credential secret string is ",credential)

        #make credential
        #this is from pytss.utils
        credentialBlob, secret = make_credential(ek_pub, bytes(credential,'utf-8'), ak_name) 
        print("Credential types :",type(credentialBlob),type(secret))


        sym = TPMT_SYM_DEF(algorithm=TPM2_ALG.NULL)

        authsession = remote_tpm.start_auth_session(
            tpm_key=ek_handle,
            bind=ESYS_TR.NONE,
            session_type=TPM2_SE.POLICY,
            symmetric=sym,
            auth_hash=TPM2_ALG.SHA256,
        )

        print("authorisation session :",type(authsession),authsession)

        print("\nPolicy Secret")
        nonce = remote_tpm.trsess_get_nonce_tpm(authsession)
        expiration = -(10 * 365 * 24 * 60 * 60)
        timeout, policyTicket = remote_tpm.policy_secret(
            ESYS_TR.ENDORSEMENT, authsession, nonce, b"", b"", expiration
        )

        print("Policy ticket: ",type(policyTicket))

        print("\nActivating credential")
        certInfo = remote_tpm.activate_credential( ak_handle, ek_handle, credentialBlob, secret, session2=authsession)
        print("Returned secret is ",certInfo,type(certInfo))


        remote_tpm.close()

        #claim["transientdata"]=transientdata
        claim["header"]["ta_complete"] = str(datetime.datetime.now(datetime.timezone.utc))
        claim["payload"]["secret"] = bytearray.fromhex(certInfo.__str__()).decode()


        return a10.structures.returncode.ReturnCode(
                a10.structures.constants.PROTOCOLSUCCESS, {"claim":claim,"transientdata":transientdata}
            )                  
















    def makecredential(self):
        print("\nmakecredential")

        try:
            ekpub = self.callparameters["ekpub"]
            akname = self.callparameters["akname"]
        except:
            print("missing ekpub and/or akname ")
            return None

        # a bit of housekeeping
        # store ek in temporary file
        ektf = tempfile.NamedTemporaryFile()

        ektf.write(bytes(ekpub, "utf-8"))
        ektf.seek(0)

        # generate secret
        # This must be a maximum of 32 bytes for makecredential - it is possible that your TPM might vary, but 32 seems to be usual

        print("Secret is ", secret)

        secf = tempfile.NamedTemporaryFile()
        secf.write(bytes(secret, "ascii"))
        secf.seek(0)

        # temporary file for credential
        credf = tempfile.NamedTemporaryFile()

        # makecredential
        #
        # assuming no local TPM with -T none --- might change this one day
        # given that the tools need to be available .... or pytss.
        # OK, maybe not such a bad thing that the AE runs on a device with a TPM or at least the tools
        # Will be necessary for using tpm2_send in the other protocol
        #
        try:
            out=""
            cmd = (
                "tpm2_makecredential -T none"
                + " -s "
                + secf.name
                + " -u "
                + ektf.name
                + " -n "
                + akname
                + " -G rsa -o "
                + credf.name
            )
            out = subprocess.check_output(cmd.split())
        except:
            print("tpm2_makecredential failed "+out)
            return None

        # read the credential
        credf.seek(0)
        cred = base64.b64encode(credf.read()).decode("utf-8")

        # cleanup
        ektf.close()
        secf.close()
        credf.close()

        # return
        return cred,secret        