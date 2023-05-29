# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

from flask import Blueprint, request, jsonify
import json
import datetime

testquote_endpoint = Blueprint("testquote_endpoint", __name__)


@testquote_endpoint.route("/", methods=["GET", "POST"])
def returnTestQuote():
    # POST allows a body with JSON...
    # GET according to the standard should ignore any body, however we leave both here just in case...GET is more natural but I'm not
    # encoding everything into the URL...too much...POST is fine
    #
    # in the body we should have a policy object structure in JSON form
    # {
    #'policyType' : 'tpm20_tpms_attest',
    #'description' : 'x86 SRTM',
    #'pcrs' : 'sha256:0,1,2,3,4,5,6,7'
    # }

    policy = json.loads(request.json)

    # we should validate the syntax here, but well...later
    # A full tpm2_tpms_attest structure would contain which AK to use and
    # other stuff, here we leave it to the local implementation to decide

    # TEST CLAIM
    quote = {
        "magic": "acbd",
        "type": "acbd",
        "qualifiedSigner": "acbd",
        "extraData": "acbd",
        "resetCount": "acbd",
        "restartCount": "acbd",
        "safe": "acbd",
        "attested": "acbd",
        "firmwareVersion": "acbd",
        "signature": "acbd",
    }

    claimHeader = {
        "claimType": "tpm20_tpms_attest",
        "claimCreated": str(datetime.datetime.now()),
        "signingKeyName": "1234",
    }

    claimSignature = {"hash": "0x1234", "signed": "0x1234"}

    claim = {
        "header": claimHeader,
        "payload": {"quote": quote, "requestedPCRs": policy["pcrs"]},
        "signature": claimSignature,
    }

    returncode = 200

    # In the case of success we return a claim and HTTP 200
    # In the case of failure we return a message and something that isn't HTTP
    # 20x

    return jsonify(claim), returncode
