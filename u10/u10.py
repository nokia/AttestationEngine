#Copyright 2021 Nokia
#Licensed under the BSD 3-Clause Clear License.
#SPDX-License-Identifier: BSD-3-Clear

import secrets

from flask import Flask,render_template
from blueprints.edit import edit_blueprint
from blueprints.home import home_blueprint
from blueprints.elements import elements_blueprint
from blueprints.policies import policies_blueprint
from blueprints.expectedvalues import expectedvalues_blueprint
from blueprints.claims import claims_blueprint
from blueprints.results import results_blueprint
from blueprints.rules import rules_blueprint
from blueprints.protocols import protocols_blueprint
from blueprints.attestation import attestation_blueprint
from blueprints.hashes import hashes_blueprint
from blueprints.elementanalytics import elementanalytics_blueprint
from blueprints.log import log_blueprint
from blueprints.ping import ping_blueprint
from blueprints.qrcodes import qrcodes_blueprint
from blueprints.pcrschemas import pcrschemas_blueprint
from blueprints.sessions import sessions_blueprint

import argparse

import a10.structures.constants
import a10.asvr.hashes
import a10.asvr.pcrschemas

u10 = Flask(__name__)

secret = secrets.token_urlsafe(64)
u10.secret_key = secret

u10.register_blueprint(edit_blueprint)
u10.register_blueprint(home_blueprint)
u10.register_blueprint(elements_blueprint)
u10.register_blueprint(policies_blueprint)
u10.register_blueprint(expectedvalues_blueprint)
u10.register_blueprint(claims_blueprint)
u10.register_blueprint(results_blueprint)
u10.register_blueprint(rules_blueprint)
u10.register_blueprint(protocols_blueprint)
u10.register_blueprint(attestation_blueprint)
u10.register_blueprint(hashes_blueprint)
u10.register_blueprint(elementanalytics_blueprint)
u10.register_blueprint(log_blueprint)
u10.register_blueprint(ping_blueprint)
u10.register_blueprint(qrcodes_blueprint)
u10.register_blueprint(pcrschemas_blueprint)
u10.register_blueprint(sessions_blueprint)


#
# Context Processor Functions
#

@u10.context_processor
def resolveHash_processor():
    def resolveHash(h):
        #print("Calling context processor with ",h)
        r =  a10.asvr.hashes.getHash(h)
        if r.rc()==a10.structures.constants.SUCCESS:
            return a10.asvr.hashes.getHash(h).msg()
        else:
            return "-"
    return dict(resolveHash=resolveHash)

@u10.context_processor
def resolvePCRSchema_processor():
    def resolvePCRSchema(n):
        #print("Calling context processor with ",n,b,p)
        r =  a10.asvr.pcrschemas.getPCRSchema(n)
        print("pcrschema ",r.rc())
        if r.rc()==a10.structures.constants.SUCCESS:
            return r.msg()
        else:
            return "-"
    return dict(resolvePCRSchema=resolvePCRSchema)

#
# Handle errors, censorship and cups to tea
#

@u10.errorhandler(404)
def not_found(e):
    return render_template("home/404.html")

#
# These are useless but a bit of humour doesn't hurt
#

@u10.errorhandler(451)
def censored(e):
    return render_template("home/451.html")    

@u10.errorhandler(418)
def teapot(e):
    return render_template("home/418.html")    

#
# Use this for development
#

def main_debug(cert, key, config_filename="u10.conf"):
    u10.config.from_pyfile(config_filename)
    if cert and key:
        print("running in secure mode")
        u10.run(
            debug=u10.config["FLASKDEBUG"],
            threaded=u10.config["FLASKTHREADED"],
            host=u10.config["DEFAULTHOST"],
            port=u10.config["DEFAULTPORT"],
            ssl_context=(cert, key),
        )
    else:
        print("running in insecure mode")
        u10.run(
            debug=u10.config["FLASKDEBUG"],
            threaded=u10.config["FLASKTHREADED"],
            host=u10.config["DEFAULTHOST"],
            port=u10.config["DEFAULTPORT"],
        )


#
# Use this in production
#
def main_production(cert, key, config_filename="u10.conf", t=16):
   from waitress import serve
   u10.config.from_pyfile(config_filename)
   serve(u10, host=u10.config["DEFAULTHOST"], port=u10.config["DEFAULTPORT"], threads=t)

ap = argparse.ArgumentParser(description='U10 Nokia Attestation Server UI')
ap.add_argument('-p', '--production', help="Run the web server in production mode (Waitress instread of Flask Debug)",  action='store_true')
args = ap.parse_args()


if __name__ == "__main__":
    print("U10 Starting")
    if args.production==True:
        print("Running in Production Mode")
        main_production("", "")
    else:
        print("Running in Debug Mode")        
        main_debug("","")
