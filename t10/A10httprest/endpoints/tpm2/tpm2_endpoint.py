# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

from flask import Blueprint, request, jsonify
import json
import datetime
from claims import claimstructure

tpm2_endpoint = Blueprint("tpm2_endpoint", __name__)


@tpm2_endpoint.route("/pcrs", methods=["GET", "POST"])
def returnPCRREAD():
    c = claimstructure.Claim()
    c.addHeaderItem("ta_received", str(datetime.datetime.now(datetime.timezone.utc)))
    q = tpmdevice.readPCRs()
    c.addPayloadItem("pcrs", q)
    c.addHeaderItem("ta_complete", str(datetime.datetime.now(datetime.timezone.utc)))
    c.addHeaderItem("ak_name", "whatever the AK name is here")
    c.sign()
    rc = c.getClaim()

    return jsonify(rc), 200


# @tpm2_endpoint.route('/quote', methods=['POST'])
# def returnTPMSATTEST():
#     tpmdevice = tpm.TPM()

#     # This is how it works

#     # 1. take the policy and extract the PCRs
#     #print("Now in TA")
#     #print(request.json)
#     body = json.loads(request.json)
#     print("\n*********************\nReceived body is",body)

#     # 2. deal with any additional information, eg: nonce etc from the additional parameters

#     pcrselection   = body['policyparameters']['pcrselection']
#     hashalg   = body['policyparameters']['hashalg']
#     callparameters = body['callparameters']

#     #print("Policy & Parameters",pcrselection,"\n",hashalg,"\n",parameters,"\n")

#     # 3. call tpm2_quote accordingly

#     # 3.1 decided which AK to use locally, or if supplied in the parameters

#     #We need to wrap this into an exception because the parameters["ak"] might not exist.
#     #In which case we need to let the TA decided ... or use a default of 0x810100aa
#     ak_to_use="0x810100aa"
#     try:
#        ak_to_use=callparameters["ak"]
#     except KeyError:
#     	print("AK Missing , using  default of 0x810100aa")


#     # 3.1.1 build the initial claim structure

#     c = claimstructure.Claim()
#     c.addHeaderItem("ta_received",str(datetime.datetime.now(datetime.timezone.utc)))

#     # 3.2 call quote
#     q = tpmdevice.quote(ownak=ak_to_use,pcrs=pcrselection)


#     #j = tpmdevice.tpms_attest_as_yaml(q[0])
#     #print("AS YAML=",j)

#     # 4. populate the claim with the quote and other header items

#     c.addPayloadItem("quote",q)
#     c.addHeaderItem("ta_complete",str(datetime.datetime.now(datetime.timezone.utc)))

#     # 5. Signing ... this should be done by the TPM and use the same key (AK) as the quote
#     #   also include the ak.name in the header for completeness

#     c.addHeaderItem("ak_name","whatever the AK name is here")
#     c.sign()

#     rc = c.getClaim()

#     #print("Returned claim is ",rc)

#     # 6. return the claim

#     # In the case of success we return a claim and HTTP 200
#     # In the case of failure we return a message and something that isn't HTTP
#     # 20x

#     return jsonify(rc), 200
