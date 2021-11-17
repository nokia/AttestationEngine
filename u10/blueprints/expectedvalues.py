# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import secrets
import json
from flask import Blueprint, render_template, flash, redirect

import a10.structures.constants
import a10.structures.identity

import a10.asvr.expectedvalues

expectedvalues_blueprint = Blueprint(
    "expectedvalues",
    __name__,
    static_folder="../static",
    template_folder="../templates/",
)

secret = secrets.token_urlsafe(64)
expectedvalues_blueprint.secret_key = secret


@expectedvalues_blueprint.route("/expectedvalues", methods=["GET"])
def expectedvalues():
    evs = a10.asvr.expectedvalues.getExpectedValuesFull()

    for ev in evs:

        e = a10.asvr.elements.getElement(ev["elementID"])
        if e.rc() == a10.structures.constants.SUCCESS:
            ev["elementname"] = e.msg()["name"]
        else:
            ev["elementname"] = "ELEMENT DELETED"

        p = a10.asvr.policies.getPolicy(ev["policyID"])
        if p.rc() == a10.structures.constants.SUCCESS:
            ev["policyname"] = p.msg()["name"]
        else:
            ev["policyname"] = "ELEMENT DELETED"

    evs_sorted = sorted(evs, key=lambda i: (i["name"]))

    return render_template("expectedvalues.html", expectedValues=evs_sorted)


@expectedvalues_blueprint.route("/expectedvalue/<item_id>", methods=["GET"])
def expectedvalue(item_id):
    ev = a10.asvr.expectedvalues.getExpectedValue(item_id)

    pp = json.dumps(ev.msg(), sort_keys=True, indent=4)
    return render_template("expectedvalue.html", e=ev.msg(), pp=pp)
