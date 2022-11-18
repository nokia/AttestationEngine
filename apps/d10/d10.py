#Copyright 2021 Nokia
#Licensed under the BSD 3-Clause Clear License.
#SPDX-License-Identifier: BSD-3-Clear

import secrets
import argparse

from flask import Flask,render_template


d10 = Flask(__name__)

secret = secrets.token_urlsafe(64)
d10.secret_key = secret


#
# Handle errors, censorship and cups to tea
#

@d10.errorhandler(404)
def not_found(e):
    return render_template("home/404.html")


#
# Use this for development
#

def main_debug(cert, key, config_filename="d10.conf"):
    d10.config.from_pyfile(config_filename)
    if cert and key:
        print("running in secure mode")
        d10.run(
            debug=d10.config["FLASKDEBUG"],
            threaded=d10.config["FLASKTHREADED"],
            host=d10.config["DEFAULTHOST"],
            port=d10.config["DEFAULTPORT"],
            ssl_context=(cert, key),
        )
    else:
        print("running in insecure mode")
        d10.run(
            debug=d10.config["FLASKDEBUG"],
            threaded=d10.config["FLASKTHREADED"],
            host=d10.config["DEFAULTHOST"],
            port=d10.config["DEFAULTPORT"],
        )


#
# Use this in production
#
def main_production(cert, key, config_filename="d10.conf", t=16):
   from waitress import serve
   d10.config.from_pyfile(config_filename)
   serve(d10, host=d10.config["DEFAULTHOST"], port=d10.config["DEFAULTPORT"], threads=t)

ap = argparse.ArgumentParser(description='d10 Nokia Attestation Server DSL UI')
ap.add_argument('-p', '--production', help="Run the web server in production mode (Waitress instread of Flask Debug)",  action='store_true')
args = ap.parse_args()


if __name__ == "__main__":
    print("d10 Starting")
    if args.production==True:
        print("Running in Production Mode")
        main_production("", "")
    else:
        print("Running in Debug Mode")        
        main_debug("","")
