# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import secrets
import json
from flask import Blueprint, render_template, flash, redirect

import a10.structures.constants
import a10.structures.identity

import a10.asvr.policies

policies_blueprint = Blueprint(
    "policies", __name__, static_folder="../static", template_folder="../templates/"
)

secret = secrets.token_urlsafe(64)
policies_blueprint.secret_key = secret


@policies_blueprint.route("/policies", methods=["GET"])
def policies():
    ps = a10.asvr.policies.getPoliciesFull()
    ps_sorted = sorted(ps, key=lambda i: (i["name"]))

    return render_template("policies.html", policies=ps_sorted)


@policies_blueprint.route("/policy/<item_id>", methods=["GET"])
def policy(item_id):
    p = a10.asvr.policies.getPolicy(item_id)
    evs = a10.asvr.expectedvalues.getExpectedValuesForPolicy(item_id)
    for i in evs:
        e = a10.asvr.elements.getElement(i["elementID"])
        if e.rc() == a10.structures.constants.SUCCESS:
            i["elementname"] = e.msg()["name"]
        else:
            i["elementname"] = "ELEMENT DELETED"
    pp = json.dumps(p.msg(), sort_keys=True, indent=4)
    return render_template("policy.html", pol=p.msg(), evs=evs, pp=pp)
