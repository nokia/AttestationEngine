#Copyright 2021 Nokia
#Licensed under the BSD 3-Clause Clear License.
#SPDX-License-Identifier: BSD-3-Clear

from flask import Blueprint, request, jsonify
import json
import datetime
import tempfile
import base64
import subprocess
from tpm import tpm
from claims import claimstructure

tpm2_endpoint = Blueprint("tpm2_endpoint", __name__)


@tpm2_endpoint.route("/pcrs", methods=["GET", "POST"])
def returnPCRREAD():
    tpmdevice = tpm.TPM()

    c = claimstructure.Claim()
    c.addHeaderItem("ta_received", str(datetime.datetime.now(datetime.timezone.utc)))
    q = tpmdevice.readPCRs()
    c.addPayloadItem("pcrs", q)
    c.addHeaderItem("ta_complete", str(datetime.datetime.now(datetime.timezone.utc)))
    c.addHeaderItem("ak_name", "whatever the AK name is here")
    c.sign()
    rc = c.getClaim()

    return jsonify(rc), 200





@tpm2_endpoint.route("/quote", methods=["POST"])
def returnTPMSATTEST():
    tpmdevice = tpm.TPM()

    # This is how it works

    # 1. take the policy and extract the PCRs
    # print("Now in TA")
    # print(request.json)
    body = json.loads(request.json)
    print("\n*********************\nReceived body is", body)

    # 2. deal with any additional information, eg: nonce etc from the additional parameters

    pcrselection = body["policyparameters"]["pcrselection"]
    hashalg = body["policyparameters"]["hashalg"]
    callparameters = body["callparameters"]

    # print("Policy & Parameters",pcrselection,"\n",hashalg,"\n",parameters,"\n")

    # 3. call tpm2_quote accordingly

    # 3.1 decided which AK to use locally, or if supplied in the parameters

    # We need to wrap this into an exception because the parameters["ak"] might not exist.
    # In which case we need to let the TA decided ... or use a default of 0x810100aa
    ak_to_use = "0x810100aa"
    try:
        ak_to_use = callparameters["ak"]
    except KeyError:
        print("AK Missing , using  default of 0x810100aa")

    # 3.1.1 build the initial claim structure

    c = claimstructure.Claim()
    c.addHeaderItem("ta_received", str(datetime.datetime.now(datetime.timezone.utc)))

    # 3.2 call quote
    q = tpmdevice.quote(ownak=ak_to_use, pcrs=pcrselection)

    # j = tpmdevice.tpms_attest_as_yaml(q[0])
    # print("AS YAML=",j)

    # 4. populate the claim with the quote and other header items

    c.addPayloadItem("quote", q)
    c.addHeaderItem("ta_complete", str(datetime.datetime.now(datetime.timezone.utc)))

    # 5. Signing ... this should be done by the TPM and use the same key (AK) as the quote
    #   also include the ak.name in the header for completeness

    c.addHeaderItem("ak_name", "whatever the AK name is here")
    c.sign()

    rc = c.getClaim()

    # print("Returned claim is ",rc)

    # 6. return the claim

    # In the case of success we return a claim and HTTP 200
    # In the case of failure we return a message and something that isn't HTTP
    # 20x

    return jsonify(rc), 200






@tpm2_endpoint.route("/credentialcheck", methods=["POST"])
def returnMAKEACTIVATECREDENTIAL():
    tpmdevice = tpm.TPM()

    c = claimstructure.Claim()
    c.addHeaderItem("ta_received", str(datetime.datetime.now(datetime.timezone.utc)))

    # This is how it works

    # 1. take the policy and extract the PCRs
    # print("Now in TA")
    # print(request.json)
    body = json.loads(request.json)
    print("\n*********************\nCredential Check\nReceived body is", body)    

    ekpub = body["callparameters"]["ekpub"]
    akname = body["callparameters"]["akname"]
    credential = body["callparameters"]["credential"]

    ak_to_use = "0x810100aa"
    try:
        ak_to_use = body["ak"]
    except KeyError:
        print("AK Missing , using  default of 0x810100aa")

    ek_to_use = "0x810100ee"
    try:
        ek_to_use = body["ek"]
    except KeyError:
        print("EK Missing , using  default of 0x810100ee")
            

    #print("I have ",ekpub,akname,ak_to_use,ek_to_use)

#
# Do the activatecredential stuff here
#


    sfile = tempfile.NamedTemporaryFile(delete=False)

    incredf = tempfile.NamedTemporaryFile(delete=False)
    incredf.write(bytes(base64.b64decode(credential)))
    incredf.seek(0)

    ocredf = tempfile.NamedTemporaryFile(delete=False)

# This should (WILL!!!) be rewritten using the proper python libraries
# but as you can see there is absolutely no error checking here
# if any of these fail then the whole thing fails...hard!
# Of course that is hardly ever going to happen in production....hahahahhaha

    out=None

    try:
        cmd = "tpm2_startauthsession --policy-session -S " + sfile.name
        out = subprocess.run(cmd.split())

        cmd = "tpm2_policysecret -S " + sfile.name + " -c e"
        out = subprocess.run(cmd.split())

        cmd = (
            "tpm2_activatecredential -c "
            + ak_to_use
            + " -C "
            + ek_to_use
            + " -i "
            + incredf.name
            + " -o "
            + ocredf.name
            + " -P session:"
            + sfile.name
        )
        out = subprocess.run(cmd.split())

        cmd = "tpm2_flushcontext " + sfile.name
        out = subprocess.run(cmd.split())
    except Exception as e:
        print("Failed to run a tpm command ", cmd, e)
        return jsonify({"msg":"error running TPM command: "+cmd+", error was "+str(e)}),500

    sfile.close()
    incredf.close()

    try:
        ocredf.seek(0)
        revealedsecret = ocredf.read().decode("utf-8")
        print("REVEALED SECRET IS ", revealedsecret)
     except Exception as e:
        print("Failed to read secret from activatecredential: ", cmd, e)
        return jsonify({"msg":"error running TPM command "+cmd+", error was "+str(e)}),500   

    ocredf.close()


    c.addPayloadItem("secret", revealedsecret)
    c.addHeaderItem("ta_processed", str(datetime.datetime.now(datetime.timezone.utc)))

    c.sign()
    rc = c.getClaim()


    return jsonify(rc), 200
