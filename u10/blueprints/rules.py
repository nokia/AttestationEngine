# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import secrets
import json
from flask import Blueprint, render_template, flash, redirect

import a10.structures.constants
import a10.structures.identity

import a10.asvr.rules.rule_dispatcher

rules_blueprint = Blueprint(
    "rules", __name__, static_folder="../static", template_folder="../templates/"
)

secret = secrets.token_urlsafe(64)
rules_blueprint.secret_key = secret


@rules_blueprint.route("/rules", methods=["GET"])
def results():
    rs = list(a10.asvr.rules.rule_dispatcher.getRegisteredRules())

    rsl = []
    for r in rs:
        rsl.append(
            {
                "name": r,
                "description": a10.asvr.rules.rule_dispatcher.getRuleDescription(
                    r
                ).msg(),
            }
        )

    return render_template("/informationpages/rules.html", rs=rsl)
