# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import secrets
import json
from flask import Blueprint, render_template, flash, redirect, request

import a10.structures.constants
import a10.structures.identity
import a10.structures.timestamps

import a10.asvr.db.announce

from . import formatting


log_blueprint = Blueprint(
    "log", __name__, static_folder="../static", template_folder="../templates/"
)

secret = secrets.token_urlsafe(64)
log_blueprint.secret_key = secret


@log_blueprint.route("/log", methods=["GET"])
def getlog():
    lrs = 250  # default number of latest results if nothing else is specified

    if "lrs" in request.args:
        lrs = int(request.args["lrs"])
        if lrs < 1:
            lrs = 1

    ls = a10.asvr.db.announce.getLatestLogEntries(lrs)
    for l in ls:
        l["tUTC"] = formatting.futc(l["t"])

    ts = a10.structures.timestamps.now()
    lc = a10.asvr.db.announce.getLogEntryCount()
    if lrs > lc:
        lrs = lc

    return render_template(
        "log.html", ls=ls, lrs=lrs, lc=lc, lt=ts, ltutc=formatting.futc(ts)
    )
