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
import subprocess

sysinfo_endpoint = Blueprint("sysinfo_endpoint", __name__)

@sysinfo_endpoint.route("/info", methods=["GET", "POST"])
def returnSYSINFO():
    c = claimstructure.Claim()

    c.addHeaderItem("ta_received", str(datetime.datetime.now(datetime.timezone.utc)))
    
    si = { "os": os.name,
              "system": platform.system(),
              "release": platform.release(),
              "version": platform.version(),
              "machine": platform.machine(),
              "processor": platform.processor(),
              "uname": platform.uname(),
              "node": platform.node()

    }

    c.addPayloadItem("systeminfo", si )
 
    c.addHeaderItem("ta_complete", str(datetime.datetime.now(datetime.timezone.utc)))
    
    rc = c.getClaim()

    return jsonify(rc), 200





@sysinfo_endpoint.route("/firmwareinfo", methods=["GET", "POST"])
def returnFIRMWAREINFO():
    c = claimstructure.Claim()

    c.addHeaderItem("ta_received", str(datetime.datetime.now(datetime.timezone.utc)))

    commandsRun = []

    #
    # DMIDECODE
    #
    # REPEAT THIS PATTERN FOR EACH FIRMWARE DIAGNOSTICS TOOL
    dmidecode = { "out":"", "error":"0" }
    try:
        cmd = ["dmidecode"]
        out = subprocess.check_output(cmd)
        print("DMIDECODE=",out)
        dmidecode["out"] = out.decode('utf-8')
        commandsRun.append("dmidecode")

    except Exception as e:
        dmidecode["out"] = str(e)
        dmidecode["error"] = "1"

    c.addPayloadItem("dmidecode", dmidecode )
    #
    # END OF DMIDECODE
    #

    c.addPayloadItem("commandsRun", commandsRun )

    c.addHeaderItem("ta_complete", str(datetime.datetime.now(datetime.timezone.utc)))
    
    rc = c.getClaim()

    return jsonify(rc), 200
