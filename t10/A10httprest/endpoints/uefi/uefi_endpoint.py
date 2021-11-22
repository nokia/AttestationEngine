# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

from flask import Blueprint, request, jsonify
import json
import datetime
from tpm import tpm
from claims import claimstructure

uefi_endpoint = Blueprint("uefi_endpoint", __name__)


@uefi_endpoint.route("/eventlog", methods=["GET", "POST"])
def returnEVENTLOGRREAD():
    tpmdevice = tpm.TPM()

    c = claimstructure.Claim()
    c.addHeaderItem("ta_received", str(datetime.datetime.now(datetime.timezone.utc)))
    q = tpmdevice.readEventLog()
    c.addPayloadItem("eventlog", q)
    c.addHeaderItem("ta_complete", str(datetime.datetime.now(datetime.timezone.utc)))
    c.addHeaderItem("ak_name", "whatever the AK name is here")
    c.sign()
    rc = c.getClaim()

    return jsonify(rc), 200
