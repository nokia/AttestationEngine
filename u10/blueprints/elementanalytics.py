# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import os
import secrets
import uuid
import string
import random

from flask import Blueprint, send_from_directory, render_template, request

import a10.structures.constants
import a10.structures.identity

import a10.asvr.db.core
import a10.asvr.db.configuration
import a10.asvr.analytics.elementanalytics

elementanalytics_blueprint = Blueprint(
    "elementanalytics",
    __name__,
    static_folder="../static",
    template_folder="../templates/",
)

secret = secrets.token_urlsafe(64)
elementanalytics_blueprint.secret_key = secret


@elementanalytics_blueprint.route("/element/graph/<item_id>", methods=["GET"])
def eapage(item_id):
    lrs = 250  # default number of latest results if nothing else is specified

    if "lrs" in request.args:
        lrs = int(request.args["lrs"])

    # First get the element
    e = a10.asvr.elements.getElement(item_id).msg()

    # Then get the expected values which show which policies the element is associated with
    evs = a10.asvr.expectedvalues.getExpectedValuesForElement(item_id)

    ps = []
    for i in evs:
        p = a10.asvr.policies.getPolicy(i["policyID"])
        if p.rc() == a10.structures.constants.SUCCESS:
            ps.append(p.msg())

    # For each policy, get the Results Count for that element

    ds = []
    for p in ps:
        entry = {
            "elementID": item_id,
            "elementname": e["name"],
            "policyID": p["itemid"],
            "policyname": p["name"],
        }

        counts = a10.asvr.analytics.elementanalytics.getResultCountsByPolicy(
            item_id, p["itemid"], 250
        )
        # zips are clever!
        clabels, cvalues = zip(*counts.items())
        entry["counts"] = counts
        entry["clabels"] = list(clabels)
        entry["cvalues"] = list(cvalues)
        # This generates a random variable name for javascript between 10 and 15 characters
        entry["vname"] = "".join(
            random.choice(string.ascii_letters) for x in range(random.randint(10, 15))
        )
        ds.append(entry)

    print(ds)

    return render_template("elementanalytics.html", ds=ds, ename=e["name"])
