# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

#import datetime
#import os
#import sys
import secrets

import tempfile

import json


from a10.asvr import (
    elements,                                                                                                                                                                                                                                                                                                                                                                                                                       
    policies,
    attestation,
    claims,
    expectedvalues,
    results,
    types,
    hashes,
    pcrschemas,
    sessions,
    logread
)

from a10.structures import constants, timestamps
from a10.asvr.db import announce
import a10.asvr.rules.rule_dispatcher

from flask import Blueprint, jsonify, request, send_file

dbimportexport_blueprint = Blueprint(
    "dbimportexport", __name__, static_folder="../static", template_folder="../templates/",url_prefix='/dbimportexport'
)

secret = secrets.token_urlsafe(64)
dbimportexport_blueprint.secret_key = secret


#
# Home
#


@dbimportexport_blueprint.route("/", methods=["GET"])
def hello():
    return {"msg":"Hello from A10REST v2 DB API"},200

@dbimportexport_blueprint.route("/export")
def dbexport():
    archive = {}
    f = tempfile.NamedTemporaryFile(delete=False)
    
    print("Setting metadata")
    archive["timestamp"]=                                               str(a10.structures.timestamps.now())


    print("Getting elements")
    es  = elements.getElementsFull(archived=False)
    esa = elements.getElementsFull(archived=True)
    archive["elements"] = es 
    archive["elementsArchived"] = esa

    print("Getting policies")
    ps = policies.getPoliciesFull()
    archive["policies"] = ps 
    
    print("Getting expected values")
    evs = expectedvalues.getExpectedValuesFull()
    archive["expectedValues"] = evs 

    print("Getting claims")
    cs = claims.getClaimsFull()
    archive["claims"] = cs

    print("Getting results")
    rs = results.getResultsFull()
    archive["results"] = rs


    print("Getting sessions")
    so = sessions.getOpenSessions()
    sc = sessions.getClosedSessions()

    archive["sessionsopen"] = so
    archive["sessionsclosed"] = sc

    print("Getting log")
    ls = logread.getLogFull()
    archive["log"]=ls

    print("Getting ancillary structures")
    pcrs = pcrschemas.getPCRSchemasFull()
    hs = hashes.getHashesFull()
    archive["pcrschemas"] = pcrs
    archive["hashes"] = hs



    f.write( json.dumps(archive).encode() )
    print("temp file name is ",f.name)
    f.close()
    return send_file(f.name)




@dbimportexport_blueprint.route("/import", methods=["POST"])
def dbimport():
    content = request.json
    print("Content timestamp is ",content["timestamp"])

    return {"msg":"Hello from A10REST v2 DB API - IMPORT"},200
