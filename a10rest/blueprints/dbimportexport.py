# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

#import datetime
#import os
#import sys
import secrets

import tempfile

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
from a10.structures import constants
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

@dbimportexport_blueprint.route("/")
def hello():
    return {"msg":"Hello from A10REST v2 DB API"},200

@dbimportexport_blueprint.route("/import")
def dbimport():
    return {"msg":"Hello from A10REST v2 DB API - IMPORT"},200

@dbimportexport_blueprint.route("/export")
def dbexport():
    f = tempfile.NamedTemporaryFile(delete=False)
    f.write(b"Croeso ffeil")
    print("temp file name is ",f.name)
    f.close()
    return send_file(f.name)
    #return {"msg":"Hello from A10REST v2 DB API - EXPORT "+f.name},200    