# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import datetime
import os
import sys

import argparse

from a10.asvr import (
    elements,
    policies,
    attestation,
    claims,
    expectedvalues,
    results,
    types,
)
from a10.structures import constants
from a10.asvr.db import announce
import a10.asvr.rules.rule_dispatcher

#from bson.objectid import ObjectId
from flask import Flask, request, send_from_directory, jsonify
#from flask.json import JSONEncoder

from blueprints.v2 import v2_blueprint
from blueprints.dbimportexport import dbimportexport_blueprint


a10rest = Flask(__name__)

#
# All of the original v1 interface (non-JSON compliant) was deprecated as of 15 November 2022
# Use the v2 interface only.
#

a10rest.register_blueprint(v2_blueprint)
a10rest.register_blueprint(dbimportexport_blueprint)


#
# Keeping this just in case
# 

@a10rest.route("/")
def hello():
    return "Hello from A10REST. v1 interface now deprecated, use v2"

################################################################################################
#
# Main
#
################################################################################################

def main_debug(cert, key, config_filename="a10rest.conf"):
    a10rest.config.from_pyfile(config_filename)
    if cert and key:
        print("running in secure mode")        
        a10rest.run(
            debug=a10rest.config["FLASKDEBUG"],
            threaded=a10rest.config["FLASKTHREADED"],
            host=a10rest.config["DEFAULTHOST"],
            port=a10rest.config["DEFAULTPORT"],
            ssl_context=(cert, key),
        )
    else:
        print("running in insecure mode")        
        a10rest.run(
            debug=a10rest.config["FLASKDEBUG"],
            threaded=a10rest.config["FLASKTHREADED"],
            host=a10rest.config["DEFAULTHOST"],
            port=a10rest.config["DEFAULTPORT"],
        )

def main_production(cert, key, config_filename="a10rest.conf", t=16):
   from waitress import serve
   a10rest.config.from_pyfile(config_filename)
   serve(a10rest, host=a10rest.config["DEFAULTHOST"], port=a10rest.config["DEFAULTPORT"], threads=t)


ap = argparse.ArgumentParser(description='A10REST Nokia Attestation Server REST API')
ap.add_argument('-p', '--production', help="Run the REST server in production mode (Waitress instread of Flask Debug)",  action='store_true')
args = ap.parse_args()


if __name__ == "__main__":
    print("A10REST Starting")
    if args.production==True:
        print("Running in Production Mode")
        main_production("", "")
    else:
        print("Running in Debug Mode")        
        main_debug("","")