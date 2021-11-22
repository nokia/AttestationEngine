#!/usr/bin/python3
# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause


import sys
import yaml
import json
import requests
import tempfile
import base64
import subprocess

enrollmentserver = sys.argv[1]
jsonfile = open(sys.argv[2])
jsondata = json.loads(jsonfile.read())
jsonfile.close()

ekpub = jsondata["tpm2"]["tpm0"]["ekpem"]
akname = jsondata["tpm2"]["tpm0"]["akname"]
akhandle = jsondata["tpm2"]["tpm0"]["akhandle"]
ekhandle = jsondata["tpm2"]["tpm0"]["ekhandle"]

initialrequestbody = {"ekpub": ekpub, "akname": akname}

r = requests.post(enrollmentserver + "/enrol/credentialcheck", json=initialrequestbody)

# print("Credential Check ",r, r.text )

if r.status_code != 200:
    print("Call to credential check on enrollment server failed ", r.text, r.json())
    sys.exit(1)

# print("Continuing")

sessionid = r.json()["session"]
credential = r.json()["credential"]

#
# Do the activatecredential stuff here
#


sfile = tempfile.NamedTemporaryFile(delete=False)

incredf = tempfile.NamedTemporaryFile(delete=False)
incredf.write(bytes(base64.b64decode(credential)))
incredf.seek(0)

ocredf = tempfile.NamedTemporaryFile(delete=False)

# This should be rewritten using the proper python libraries
# but as you can see there is absolutely no error checking here
# if any of these fail then the whole thing fails...hard!
# Of course that is hardly ever going to happen in production....hahahahhaha

try:
    cmd = "tpm2_startauthsession --policy-session -S " + sfile.name
    out = subprocess.run(cmd.split())

    cmd = "tpm2_policysecret -S " + sfile.name + " -c e"
    out = subprocess.run(cmd.split())

    cmd = (
        "tpm2_activatecredential -c "
        + akhandle
        + " -C "
        + ekhandle
        + " -i "
        + incredf.name
        + " -o "
        + ocredf.name
        + " -P session:"
        + sfile.name
    )
    out = subprocess.run(cmd.split())

    cmd = "tpm2_flushcontext " + sfile.name
    out = subprocess.run(cmd.split())
except:
    print("Failed to run a tpm command ", out)
    sys.exit(1)

sfile.close()
incredf.close()

ocredf.seek(0)
revealedsecret = ocredf.read().decode("utf-8")
print("REVEALED SECRET IS ", revealedsecret)
ocredf.close()

#
# Now request enrollment
#

enrollbody = {"secret": revealedsecret, "element": jsondata}

r = requests.post(enrollmentserver + "/enrol/element/" + sessionid, json=enrollbody)

if r.status_code != 201:
    print("Call to element enrollment on enrollment server failed ", r.text)
    sys.exit(1)
else:
    print("Element with id ", r.text, " created")
