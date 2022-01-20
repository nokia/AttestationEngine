#Copyright 2021 Nokia
#Licensed under the BSD 3-Clause Clear License.
#SPDX-License-Identifier: BSD-3-Clear

from flask import Blueprint, jsonify
import json
import datetime
import base64
from claims import claimstructure
import os
import platform

sysinfo_endpoint = Blueprint("sysinfo_endpoint", __name__)


@sysinfo_endpoint.route("/info", methods=["GET", "POST"])
def returnSYSINFO():
    c = claimstructure.Claim()

    c.addHeaderItem("ta_received", str(datetime.datetime.now(datetime.timezone.utc)))
    
    si = { "os": os.name,
              "system": platform.system(),
              "release": platfor.release()
    }

    c.addPayloadItem("systeminfo", si )
 
    c.addHeaderItem("ta_complete", str(datetime.datetime.now(datetime.timezone.utc)))
    
    rc = c.getClaim()

    return jsonify(rc), 200
