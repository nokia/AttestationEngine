#Copyright 2021 Nokia
#Licensed under the BSD 3-Clause Clear License.
#SPDX-License-Identifier: BSD-3-Clear

from flask import Blueprint, jsonify
import json
import datetime
import subprocess
from claims import claimstructure

inteltxt_endpoint = Blueprint("inteltxt_endpoint", __name__)


@inteltxt_endpoint.route("/stat", methods=["GET", "POST"])
def returnTXTSTAT():
    c = claimstructure.Claim()

    c.addHeaderItem("ta_received", str(datetime.datetime.now(datetime.timezone.utc)))
    
    try:
        cmd = ["txt-stat"]
        out = subprocess.check_output(cmd)
        print("TXTSTAT=",out)
        
        c.addPayloadItem("encoding", "utf-8")
        c.addPayloadItem("stat", out.decode('utf-8'))
        c.addPayloadItem("size",len(out))

    except Exception as e:
        c.addPayloadItem("error", str(e))

    c.addHeaderItem("ta_complete", str(datetime.datetime.now(datetime.timezone.utc)))
    
    rc = c.getClaim()

    return jsonify(rc), 200
