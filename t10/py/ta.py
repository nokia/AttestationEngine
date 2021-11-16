# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

from flask import Flask, request, jsonify

from endpoints.tpm2.tpm2_endpoint import tpm2_endpoint
from endpoints.notpm.notpm_endpoint import notpm_endpoint
from endpoints.uefi.uefi_endpoint import uefi_endpoint
from endpoints.pifake.pifake_endpoint import pifake_endpoint

import sys
import os

VERSION = "0.2"

ta = Flask(__name__)

ta.register_blueprint(tpm2_endpoint, url_prefix="/tpm2")
ta.register_blueprint(uefi_endpoint, url_prefix="/uefi")
ta.register_blueprint(pifake_endpoint, url_prefix="/pifake")
ta.register_blueprint(notpm_endpoint, url_prefix="/notpm")


def listroutes():
    print("Defined Endpoints")
    for rule in ta.url_map.iter_rules():
        print(rule)


@ta.route("/", methods=["GET"])
def status_homepage():
    services = [r.rule for r in ta.url_map.iter_rules()]

    rc = {
        "title": "T10 Trust Agent",
        "version": VERSION,
        "services": str(services),
        "platform": sys.platform,
        "os": os.name,
        "pid": os.getpid(),
    }

    return jsonify(rc), 200


def main(cert, key, config_filename="ta_config.cfg"):
    listroutes()
    ta.config.from_pyfile(config_filename)
    if cert and key:
        ta.run(
            debug=ta.config["FLASKDEBUG"],
            threaded=ta.config["FLASKTHREADED"],
            host=ta.config["DEFAULTHOST"],
            port=ta.config["DEFAULTPORT"],
            ssl_context=(cert, key),
        )
    else:
        ta.run(
            debug=ta.config["FLASKDEBUG"],
            threaded=ta.config["FLASKTHREADED"],
            host=ta.config["DEFAULTHOST"],
            port=ta.config["DEFAULTPORT"],
        )


if __name__ == "__main__":
    print("TA Starting")
    main("", "")
