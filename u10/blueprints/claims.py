# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import secrets
import json
from flask import Blueprint, render_template, flash, redirect

import a10.structures.constants
import a10.structures.identity

import a10.asvr.claims

from . import formatting

claims_blueprint = Blueprint(
    "claims", __name__, static_folder="../static", template_folder="../templates/"
)

secret = secrets.token_urlsafe(64)
claims_blueprint.secret_key = secret


@claims_blueprint.route("/claims", methods=["GET"])
def claims():
    cs = a10.asvr.claims.getClaimsFull(500)

    for c in cs:
        c["receivedUTC"] = formatting.futc(c["header"]["as_received"])
        c["requestedUTC"] = formatting.futc(c["header"]["as_requested"])
        c["timedifference"] = round(
            float(c["header"]["as_received"]) - float(c["header"]["as_requested"]), 4
        )

    return render_template("claims.html", claims=cs)


@claims_blueprint.route("/claim/<item_id>", methods=["GET"])
def claim(item_id):
    c = a10.asvr.claims.getClaim(item_id).msg()
    pp = json.dumps(c, sort_keys=True, indent=4)

    c["receivedUTC"] = formatting.futc(c["header"]["as_received"])
    c["requestedUTC"] = formatting.futc(c["header"]["as_requested"])
    c["timedifference"] = round(
        float(c["header"]["as_received"]) - float(c["header"]["as_requested"]), 4
    )

    rs = a10.asvr.claims.getAssociatedResults(item_id)
    for r in rs:
        r["difference"] = round(
            float(r["verifiedAt"]) - float(c["header"]["as_received"]), 4
        )
        r["verifiedAtUTC"] = formatting.futc(r["verifiedAt"])

    return render_template("claim.html", cla=c, pp=pp, rs=rs)
