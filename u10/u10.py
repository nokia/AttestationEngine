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

# This function is unused but I'll leave it here for documentation and future purposes
# You can call this function from a template, eg: {{ resolveTheHash }} - use the name in the returned dict
# Historical fact: I had a use for this function, but didn't need it nor find a convenience place in the end :-)
# @u10.context_processor
# def resolveHash():
#    print("Calling context processor")
#    return dict(resolveTheHash='Shw Mae!')

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

def main(cert, key, config_filename="u10.conf"):
    u10.config.from_pyfile(config_filename)
    if cert and key:
        u10.run(
            debug=u10.config["FLASKDEBUG"],
            threaded=u10.config["FLASKTHREADED"],
            host=u10.config["DEFAULTHOST"],
            port=u10.config["DEFAULTPORT"],
            ssl_context=(cert, key),
        )
    else:
        print("running")
        u10.run(
            debug=u10.config["FLASKDEBUG"],
            threaded=u10.config["FLASKTHREADED"],
            host=u10.config["DEFAULTHOST"],
            port=u10.config["DEFAULTPORT"],
        )


#
# Use this in production
#
#def main(cert, key):
#   from waitress import serve
#   serve(u10, host="0.0.0.0", port=8540)


if __name__ == "__main__":
    print("U10 Starting")
    main("", "")
