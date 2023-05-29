# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import tempfile
import secrets
import string
import base64

# import subprocess
import uuid
import requests
from flask import Flask, request, jsonify
import sys
from tpm2_pytss import *

eapp = Flask(__name__)

enrollmentdb = {}
aerestendpoint = sys.argv[1]

#
# Home
#


@eapp.route("/")
def hello():
    return "Hello from Enrollment Engine. Using: " + aerestendpoint, 200


#
# Enrollment Functions
#


@eapp.route("/enrol/credentialcheck", methods=["POST"])
def credentialcheck():
    content = request.json

    ekpub = content["ekpub"]
    akname = content["akname"]

    print(str(akname))

    #print("\ntype ",type(ekpub),ekpub.encode('ascii'))
    #print("akn  ",type(akname),akname.encode('ascii'))

    # generate secret
    # This must be a maximum of 32 bytes for makecredential - it is possible that your TPM might vary, but 32 seems to be usual
    alphabet = string.ascii_letters + string.digits
    secret = "".join(secrets.choice(alphabet) for i in range(16))
    #print("Secret is ", secret)

    # makecredential
    with ESAPI("mssim:host=localhost,port=2321") as e:
        pub = TPM2B_PUBLIC.from_pem(ekpub.encode('ascii'))
        h =  e.load_external(None, pub)
        print( e.tr_get_name(h) )
        print( pub.get_name() )
        cred,csec = e.make_credential(h, TPM2B_DIGEST(secret), TPM2B_NAME(akname))


    # if that worked then create a session ID and add that with the secret to the enrollmentdb
    sessionid = str(uuid.uuid4())
    enrollmentdb[sessionid] = secret

    # read the credential
    cred = base64.b64encode(bincred).decode("utf-8")

    # send response
    res = {"session": sessionid, "credential": cred}
    print("RES=", res)
    return res, 200


#
# EnrolProcess
#


@eapp.route("/enrol/element/<sessionid>", methods=["POST"])
def enrolelement(sessionid):
    # First check if there is a valid session id
    sessionsecret = ""
    try:
        sessionsecret = enrollmentdb[sessionid]
    except:
        return "Invalid Session", 422

    content = request.json
    esecret = content["secret"]
    eelement = content["element"]

    # test if the given secret is the same as the expected secret
    if esecret != sessionsecret:
        return "Incorrect secret", 400

    # Now call the attestation engine API to add an element
    r = requests.post(aerestendpoint + "/element", json=eelement)
    print("RETURN=", r.status_code, r.text, r.json)

    if r.status_code == 201:
        return r.text, 201
    else:
        return "Communication with AE failed " + r.text + "," + str(r.json) + ".", 503


#
# Main
#


def main(cert, key, config_filename="eapp.conf"):
    eapp.config.from_pyfile(config_filename)
    if cert and key:
        eapp.run(
            debug=eapp.config["FLASKDEBUG"],
            threaded=eapp.config["FLASKTHREADED"],
            host=eapp.config["DEFAULTHOST"],
            port=eapp.config["DEFAULTPORT"],
            ssl_context=(cert, key),
        )
    else:
        eapp.run(
            debug=eapp.config["FLASKDEBUG"],
            threaded=eapp.config["FLASKTHREADED"],
            host=eapp.config["DEFAULTHOST"],
            port=eapp.config["DEFAULTPORT"],
        )


if __name__ == "__main__":
    print("EAPP Starting")
    main("", "")
