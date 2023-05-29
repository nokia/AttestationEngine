# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

from flask import Blueprint, request, jsonify
import json
import datetime

# from tpm import tpm
from claims import claimstructure

notpm = Blueprint("notpm", __name__)


@notpm.route("/null", methods=["GET", "POST"])
def returnTPMSATTEST():
    # tpmdevice = tpm.TPM()

    # This is how it works

    # 1. take the policy and extract the PCRs
    print("Now in TA")
    body = json.loads(request.json)
    print("Received body is", body)

    # 2. deal with any additional information, eg: nonce etc from the additional parameters

    print("HERE WE GO!")

    # 3.1.1 build the initial claim structure

    c = claimstructure.Claim()
    c.addHeaderItem("ta_received", str(datetime.datetime.utcnow()))

    # 4. populate the claim with the quote and other header items

    c.addPayloadItem("notpm", "notpm")
    c.addHeaderItem("ta_complete", str(datetime.datetime.utcnow()))

    # 5. Signing ... this should be done by the TPM and use the same key (AK) as the quote
    #   also include the ak.name in the header for completeness

    c.addHeaderItem("ak_name", "whatever the AK name is here")
    c.sign()

    rc = c.getClaim()

    print("Returned claimed is ", rc)

    # 6. return the claim

    # In the case of success we return a claim and HTTP 200
    # In the case of failure we return a message and something that isn't HTTP
    # 20x

    return jsonify(rc), 200
