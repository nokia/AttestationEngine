# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import secrets
import json
from flask import Blueprint, render_template, flash, redirect

import a10.structures.constants
import a10.structures.identity

import a10.asvr.protocols.protocol_dispatcher

protocols_blueprint = Blueprint(
    "protocols", __name__, static_folder="../static", template_folder="../templates/"
)

secret = secrets.token_urlsafe(64)
protocols_blueprint.secret_key = secret


@protocols_blueprint.route("/protocols", methods=["GET"])
def protocols():
    ps = list(a10.asvr.protocols.protocol_dispatcher.getRegisteredProtocols())

    return render_template("/informationpages/protocols.html", ps=ps)
