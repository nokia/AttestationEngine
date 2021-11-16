# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

from flask import Blueprint, request, jsonify
import json
import datetime
from claims import claimstructure

pifake_endpoint = Blueprint("pifake_endpoint", __name__)


@pifake_endpoint.route("/log", methods=["GET", "POST"])
def returnfakemeasuredbootlog():

    c = claimstructure.Claim()
    c.addHeaderItem("ta_received", str(datetime.datetime.now(datetime.timezone.utc)))

    log_content = "-empty-"

    with open("/var/log/measuredBootLog", "r") as f:
        log_content = f.read()

    c.addPayloadItem("measuredbootlog", log_content)
    c.addHeaderItem("ta_complete", str(datetime.datetime.now(datetime.timezone.utc)))
    c.addHeaderItem("ak_name", "whatever the AK name is here")
    c.sign()
    rc = c.getClaim()

    return jsonify(rc), 200
