#Copyright 2021 Nokia
#Licensed under the BSD 3-Clause Clear License.
#SPDX-License-Identifier: BSD-3-Clear

from flask import Blueprint, jsonify
import json
import datetime
import base64
from claims import claimstructure

ima_endpoint = Blueprint("ima_endpoint", __name__)


@ima_endpoint.route("/measurements", methods=["GET", "POST"])
def returnIMALOG():
    c = claimstructure.Claim()

    c.addHeaderItem("ta_received", str(datetime.datetime.now(datetime.timezone.utc)))
    
    try:
        f = open("/sys/kernel/security/ima/ascii_runtime_measurements","r")
        imalog = f.read()
        #eventlog_enc = base64.b64encode(eventlog).decode("utf-8")   
        #c.addPayloadItem("encoding", "base64/utf-8")
        #c.addPayloadItem("eventlog", eventlog_enc)
        c.addPayloadItem("size",len(imalog))
        c.addPayloadItem("logfile","/sys/kernel/security/ima/ascii_runtime_measurements")
        c.addPayloadItem("imalog",imalog)
        #c.addPayloadItem("sizeencoded",len(eventlog_enc))
        f.close()
    except Exception as e:
        c.addPayloadItem("error", str(e))

    c.addHeaderItem("ta_complete", str(datetime.datetime.now(datetime.timezone.utc)))
    
    rc = c.getClaim()

    return jsonify(rc), 200
