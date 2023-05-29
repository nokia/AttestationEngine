# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import secrets
import json
from flask import Blueprint, render_template, flash, redirect

import a10.structures.constants
import a10.structures.identity

import a10.asvr.hashes

hashes_blueprint = Blueprint(
    "hashes", __name__, static_folder="../static", template_folder="../templates/"
)

secret = secrets.token_urlsafe(64)
hashes_blueprint.secret_key = secret


@hashes_blueprint.route("/hashes", methods=["GET"])
def hashes():
    hs = a10.asvr.hashes.getHashesFull()
    hs_sorted = sorted(hs, key=lambda i: (i["hash"]))
    return render_template("hashes.html", hs=hs_sorted)
