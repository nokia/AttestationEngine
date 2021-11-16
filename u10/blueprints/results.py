# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import secrets
import json
from flask import Blueprint, render_template, flash, redirect

import a10.structures.constants
import a10.structures.identity

import a10.asvr.results
import a10.asvr.policies
import a10.asvr.elements

from . import formatting


results_blueprint = Blueprint(
    "results", __name__, static_folder="../static", template_folder="../templates/"
)

secret = secrets.token_urlsafe(64)
results_blueprint.secret_key = secret


@results_blueprint.route("/results", methods=["GET"])
def results():
    rs = a10.asvr.results.getResultsFull(500)

    for r in rs:
        r["verifiedAtUTC"] = formatting.futc(r["verifiedAt"])

        e = a10.asvr.elements.getElement(r["elementID"])
        if e.rc() == a10.structures.constants.SUCCESS:
            r["elementname"] = e.msg()["name"]
        else:
            r["elementname"] = "ELEMENT DELETED"

        p = a10.asvr.policies.getPolicy(r["policyID"])
        if p.rc() == a10.structures.constants.SUCCESS:
            r["policyname"] = p.msg()["name"]
        else:
            r["policyname"] = "ELEMENT DELETED"

    return render_template("results.html", results=rs)


@results_blueprint.route("/result/<item_id>", methods=["GET"])
def result(item_id):
    r = a10.asvr.results.getResult(item_id)

    rm = r.msg()
    rm["verifiedAtUTC"] = formatting.futc(rm["verifiedAt"])
    e = a10.asvr.elements.getElement(rm["elementID"])
    if e.rc() == a10.structures.constants.SUCCESS:
        rm["elementname"] = e.msg()["name"]
    else:
        rm["elementname"] = "ELEMENT DELETED"
    rm["policyname"] = a10.asvr.policies.getPolicy(rm["policyID"]).msg()["name"]

    pp = json.dumps(r.msg(), sort_keys=True, indent=4)
    return render_template("result.html", r=rm, pp=pp)
