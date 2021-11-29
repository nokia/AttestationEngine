#Copyright 2021 Nokia
#Licensed under the BSD 3-Clause Clear License.
#SPDX-License-Identifier: BSD-3-Clear

from flask import Blueprint, jsonify
import json
import datetime
import base64
from claims import claimstructure

uefi_endpoint = Blueprint("uefi_endpoint", __name__)


@uefi_endpoint.route("/eventlog", methods=["GET", "POST"])
def returnEVENTLOGRREAD():
    c = claimstructure.Claim()

    c.addHeaderItem("ta_received", str(datetime.datetime.now(datetime.timezone.utc)))
    
    try:
        f = open("/sys/kernel/security/tpm0/binary_bios_measurements","rb")
        eventlog = f.read()
        eventlog_enc = base64.b85encode(eventlog).decode("utf-8")   
        c.addPayloadItem("encoding", "base85/utf-8")
        c.addPayloadItem("eventlog", eventlog_enc)
        c.addPayloadItem("size",len(eventlog))
        c.addPayloadItem("sizeencoded",len(eventlog_enc))
        f.close()
    except Exception as e:
        c.addPayloadItem("error", str(e))

    c.addHeaderItem("ta_complete", str(datetime.datetime.now(datetime.timezone.utc)))
    
    rc = c.getClaim()

    print("\nRC",type(eventlog_enc))

    return jsonify(rc), 200
