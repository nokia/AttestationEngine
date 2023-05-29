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

    #
    # THINKLMI INTERFACE
    #
    THINKLMIDIR="/sys/class/firmware-attributes/thinklmi/attributes"
    thinklmi = { "out":{}, "error":"0", "dir":THINKLMIDIR }

    #Check it exists

    fwdirexists = os.path.isdir(THINKLMIDIR)
    if fwdirexists == False:
        print("Attributes DO NOT exist. Exiting")
        thnklmi["error"]=1
    else:
        print("Running the intenral thinklmi reader")
        commandsRun.append("thinklmi::INTERNAL")
        for d in list(os.walk(THINKLMIDIR)):
            attdir = d[0]
   # in this directory are three files
   #   current_values, display_name and possible_values
   # we now open each to get their contents and add this to a list
            try:
                cvf = open(attdir+"/current_value","r")
                cvr = cvf.read()
                cvf.close()

                dvf = open(attdir+"/display_name","r")
                dvr = dvf.read()
                dvf.close()     

                pvf = open(attdir+"/possible_values","r")
                pvr = pvf.read()
                pvf.close()     

                s = { "c": cvr.rstrip('\r\n'), 
                      "p" : pvr.rstrip('\r\n').split(",") }
                thinklmi[dvr.rstrip('\r\n')] = s 
                print(" -- thinklmi, got ",dvr)
            except Exception as e:
                print("Exception ",str(e))

    c.addPayloadItem("thinklmi", thinklmi )
    #
    # END OF THINKLMI INTERFACE
    #


    #
    # Finished and generate the rest of the claim
    #

    c.addPayloadItem("commandsRun", commandsRun )

    c.addHeaderItem("ta_complete", str(datetime.datetime.now(datetime.timezone.utc)))
    
    rc = c.getClaim()

    return jsonify(rc), 200
