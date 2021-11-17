# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import ast
import json
import secrets

import a10.structures.constants
import a10.structures.identity
from a10.asvr import rules, attestation, elements, policies, expectedvalues
from a10.asvr.rules import rule_dispatcher
from flask import Blueprint, request, render_template, flash, redirect, Markup


attestation_blueprint = Blueprint(
    "attestation", __name__, static_folder="../static", template_folder="../templates/"
)

secret = secrets.token_urlsafe(64)
attestation_blueprint.secret_key = secret


@attestation_blueprint.route("/attestverify/<itemid>", methods=["GET"])
def attestverify_get(itemid):
    e = elements.getElement(itemid)
    ps = policies.getPoliciesFull()
    rs = rule_dispatcher.getRegisteredRules()
    pp = json.dumps(e.msg(), sort_keys=True, indent=4)
    return render_template("attest.html", e=e.msg(), pp=pp, ps=ps, rs=rs)


@attestation_blueprint.route("/attestverify", methods=["POST"])
def attestverify_post():
    av = request.form["av"]  # "av"=attest and verify,  "aonly"=attest only
    i = request.form["i"]
    p = request.form["p"]
    r = request.form["r"]
    cp = ast.literal_eval(request.form["cp"])  # needed to make sure that cp is a DICT
    rp = ast.literal_eval(request.form["rp"])  # needed to make sure that rp is a DICT

    # so try to get a claim
    cres = attestation.attest(i, p, cp)
    if cres.rc() != a10.structures.constants.SUCCESS:
        flash(
            "Error obtaining claim: " + str(cres.msg()) + " " + str(cres.rc()), "danger"
        )
        return redirect("/attestverify/" + i)
    else:
        message = Markup(
            "Claim <a href=/claim/"
            + str(cres.msg())
            + ">"
            + str(cres.msg())
            + "</a> successfully obtained"
        )
        flash(message, "success")
        if av == "aonly":
            flash(
                "NB: request was for attestation only (no verification applied)", "info"
            )
            return redirect("/claim/" + cres.msg())
        else:
            rule = (r, rp)
            v = attestation.verify(cres.msg(), rule)
            if v.rc() != a10.structures.constants.RESULTSUCCESSFUL:
                flash("Error applying the verification rule: " + r, "danger")
                return redirect("/claim/" + cres.msg())
            else:
                message = Markup(
                    "Result <a href=/result/"
                    + str(v.msg())
                    + ">"
                    + str(v.msg())
                    + "</a> successfully obtained"
                )
                flash(message, "success")
                # flash("Result " + str(v.msg()) + " successfully obtained", "success")
                return redirect("/result/" + v.msg())


@attestation_blueprint.route("/attestverifyall/<itemid>", methods=["GET"])
def attestverifyall_get(itemid):
    e = elements.getElement(itemid)
    evs = expectedvalues.getExpectedValuesForElement(itemid)
    for i in evs:
        p = a10.asvr.policies.getPolicy(i["policyID"])
        if p.rc() == a10.structures.constants.SUCCESS:
            i["policyname"] = p.msg()["name"]
        else:
            i["policyname"] = "POLICY DELETED"
    rs = rule_dispatcher.getRegisteredRules()

    return render_template("attestall.html", e=e.msg(), evs=evs, rs=rs, lenevs=len(evs))


@attestation_blueprint.route("/attestverifyall", methods=["POST"])
def attestverifyall_post():
    f = request.form

    # print(f)

    eid = f["elementid"]
    lev = int(f["lenevs"])

    print("Form is ", eid, lev)

    attreqs = []

    for i in range(1, lev + 1):
        print("Dealing with item ", i)
        prefix = str(i) + "__"
        attreq = {
            "policyid": f[prefix + "policyid"],
            "rule": f[prefix + "rule"],
            "cp": ast.literal_eval(f[prefix + "cp"]),
            "rp": ast.literal_eval(f[prefix + "rp"]),
            "op": f[prefix + "op"],
        }
        # print("****** ",type(ast.literal_eval(f[prefix+"cp"])))
        attreqs.append(attreq)

    print("lenattreqs is ", len(attreqs))
    # call attest

    sessionstring = a10.structures.identity.generateID()
    flash(
        "Attesting " + str(lev) + " elements with session string " + sessionstring,
        "primary",
    )

    for a in attreqs:
        print("Attesting ", a["policyid"], a["op"])

        if a["op"] == "d":
            flash(
                "NB: attest/verify for " + a["policyid"] + " was not required",
                "secondary",
            )
        else:
            a["cp"]["attestAllSessionString"] = sessionstring
            cres = attestation.attest(eid, a["policyid"], a["cp"])
            if cres.rc() != a10.structures.constants.SUCCESS:
                flash(
                    "Error obtaining claim: " + str(cres.msg()) + " " + str(cres.rc()),
                    "danger",
                )
            else:
                message = Markup(
                    "Claim <a href=/claim/"
                    + str(cres.msg())
                    + ">"
                    + str(cres.msg())
                    + "</a> successfully obtained"
                )
                flash(message, "success")
                if a["op"] == "a":
                    flash(
                        "NB: claim "
                        + str(cres.msg())
                        + " was for attestation only (no verification applied)",
                        "info",
                    )
                else:
                    a["rp"]["attestAllSessionString"] = sessionstring
                    rule = (a["rule"], a["rp"])
                    v = attestation.verify(cres.msg(), rule)
                    if v.rc() != a10.structures.constants.RESULTSUCCESSFUL:
                        flash("Error applying the verification rule: " + r, "danger")
                    else:
                        message = Markup(
                            "Result <a href=/result/"
                            + str(v.msg())
                            + ">"
                            + str(v.msg())
                            + "</a> successfully obtained"
                        )
                        flash(message, "success")

    return redirect("/results")
