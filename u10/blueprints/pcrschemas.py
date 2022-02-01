# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import secrets
import json
from flask import Blueprint, render_template, flash, redirect

import a10.structures.constants
import a10.structures.identity

import a10.asvr.pcrschemas

pcrschemas_blueprint = Blueprint(
    "pcrschemas", __name__, static_folder="../static", template_folder="../templates/"
)

secret = secrets.token_urlsafe(64)
pcrschemas_blueprint.secret_key = secret


@pcrschemas_blueprint.route("/pcrschemas", methods=["GET"])
def pcrschemas():
    hs = a10.asvr.pcrschemas.getPCRSchemasFull()
    hs_sorted = sorted(hs, key=lambda i: (i["name"]))
    return render_template("pcrschemas.html", pcrs=hs_sorted)
