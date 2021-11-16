# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import secrets
import json
from flask import Blueprint, render_template, flash, redirect, request

import a10.structures.constants
import a10.structures.identity

import a10.asvr.elements
import a10.asvr.results
import a10.asvr.policies
import a10.asvr.types

from . import formatting

elements_blueprint = Blueprint(
    "elements", __name__, static_folder="../static", template_folder="../templates/"
)

secret = secrets.token_urlsafe(64)
elements_blueprint.secret_key = secret


@elements_blueprint.route("/elements", methods=["GET"])
def elements():
    lrs = 5  # default number of latest results if nothing else is specified

    if "lrs" in request.args:
        lrs = int(request.args["lrs"])

    es = a10.asvr.elements.getElementsFull()

    for e in es:
        res = a10.asvr.results.getLatestResults(e["itemid"], lrs)
        resultsummary = []
        for r in res:
            summarystr = {
                "verifiedAt": formatting.futc(r["verifiedAt"]),
                "pid": r["policyID"],
                "pname": a10.asvr.policies.getPolicy(r["policyID"]).msg()["name"],
                "res": r["result"],
                "rul": r["ruleName"],
                "rid": r["itemid"],
            }

            resultsummary.append(summarystr)

        e["summary"] = resultsummary

    es_sorted = sorted(es, key=lambda i: (i["name"]))

    ts = a10.asvr.types.getTypes()

    return render_template("elements.html", elements=es_sorted, ts=ts)


@elements_blueprint.route("/element/<item_id>", methods=["GET"])
def element(item_id):
    lrs = 50  # default number of latest results if nothing else is specified

    if "lrs" in request.args:
        lrs = int(request.args["lrs"])

    e = a10.asvr.elements.getElement(item_id)
    evs = a10.asvr.expectedvalues.getExpectedValuesForElement(item_id)
    for i in evs:
        p = a10.asvr.policies.getPolicy(i["policyID"])
        if p.rc() == a10.structures.constants.SUCCESS:
            i["policyname"] = p.msg()["name"]
        else:
            i["policyname"] = "POLICY DELETED"

    resultsummary = []
    res = a10.asvr.results.getLatestResults(item_id, lrs)

    for r in res:
        resultsummary.append(
            {
                "verifiedAt": formatting.futc(r["verifiedAt"]),
                "pid": r["policyID"],
                "pname": a10.asvr.policies.getPolicy(r["policyID"]).msg()["name"],
                "res": r["result"],
                "rul": r["ruleName"],
                "msg": r["message"],
                "rid": r["itemid"],
            }
        )

    pp = json.dumps(e.msg(), sort_keys=True, indent=4)
    return render_template("element.html", e=e.msg(), evs=evs, rs=resultsummary, pp=pp)
