# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import json
import secrets

from flask import Blueprint, request, render_template, flash, redirect

import a10.structures.constants
import a10.structures.identity

import a10.asvr.elements
import a10.asvr.policies
import a10.asvr.expectedvalues


edit_blueprint = Blueprint(
    "edit", __name__, static_folder="../static", template_folder="../templates/"
)

secret = secrets.token_urlsafe(64)
edit_blueprint.secret_key = secret


@edit_blueprint.route("/edit/element", methods=["POST"])
def post_edited_element():
    j = json.loads(request.form["j"])
    i = request.form["i"]
    # print("J",type(j),j,"\n","I",i,"\n")
    if j["itemid"] != i:
        flash(
            "Attempt to modify itemid in raw JSON form ignored - do not try to do this again OK!",
            "warning",
        )
        j["itemid"] = i

    r = a10.asvr.elements.updateElement(j)
    # print("Return code ",r)
    if r.rc() != a10.structures.constants.SUCCESS:
        flash(
            "Database update error code " + str(r.rc()) + " " + str(r.msg()), "danger"
        )
    else:
        flash("This element has been updated successfully", "success")

    return redirect("/element/" + j["itemid"])


@edit_blueprint.route("/edit/element/<item_id>", methods=["GET"])
def edit_element(item_id):
    element_data = a10.asvr.elements.getElement(item_id)

    pp = json.dumps(element_data.msg(), sort_keys=True, indent=4)

    return render_template("editraw.html", t="element", e=element_data.msg(), pp=pp)


@edit_blueprint.route("/new/element", methods=["GET"])
def new_element():
    # print("new element")
    element_data = {
        "type": ["tpm2.0"],
        "name": "EDIT_THIS",
        "description": "EDIT_THIS_LONGER_DESCRIPTION",
        "endpoint": "http://127.0.0.1:8530",
        "protocol": "A10HTTPREST",
    }

    pp = json.dumps(element_data, sort_keys=True, indent=4)

    return render_template("newraw.html", t="element", e=element_data, pp=pp)


@edit_blueprint.route("/new/element", methods=["POST"])
def post_new_element():
    j = json.loads(request.form["j"])

    r = a10.asvr.elements.addElement(j)
    # print("Return code ",r)
    if r.rc() != a10.structures.constants.SUCCESS:
        flash(
            "Database update error code " + str(r.rc()) + " " + str(r.msg()), "danger"
        )
        return redirect("/elements")
    else:
        flash("This element has been created successfully", "success")
        return redirect("/elements")


@edit_blueprint.route("/delete/element/<item_id>", methods=["GET"])
def delete_element(item_id):
    print("Deleteing " + item_id)
    r = a10.asvr.elements.deleteElement(item_id)
    print(r.rc(), r.msg())
    flash("Deleted (element result: " + str(r.rc()) + " " + str(r.msg()), "info")
    return redirect("/elements")


@edit_blueprint.route("/edit/policy", methods=["POST"])
def post_policy():
    j = json.loads(request.form["j"])
    i = request.form["i"]
    if j["itemid"] != i:
        flash(
            "Attempt to modify itemid in raw JSON form ignored - do not try to do this again OK!",
            "warning",
        )
        j["itemid"] = i

    r = a10.asvr.policies.updatePolicy(j)
    # print("Return code ",r)
    if r.rc() != a10.structures.constants.SUCCESS:
        flash(
            "Database update error code " + str(r.rc()) + " " + str(r.msg()), "danger"
        )
    else:
        flash("This element has been updated successfully", "success")

    return redirect("/policy/" + j["itemid"])


@edit_blueprint.route("/edit/policy/<item_id>", methods=["GET"])
def edit_policy(item_id):
    # print("editing ",item_id)
    element_data = a10.asvr.policies.getPolicy(item_id).msg()
    # print("element data ",element_data)
    pp = json.dumps(element_data, sort_keys=True, indent=4)

    return render_template("editraw.html", t="policy", e=element_data, pp=pp)


@edit_blueprint.route("/new/policy", methods=["GET"])
def new_policy():
    # print("new element")
    element_data = {
        "intent": "tpm2/quote",
        "name": "EDIT_THIS",
        "description": "EDIT_THIS_LONGER_DESCRIPTION",
        "parameters": {"hashalg": "sha256", "pcrselection": "sha256:0,1,2,3,4,5,6,7"},
    }

    pp = json.dumps(element_data, sort_keys=True, indent=4)

    return render_template("newraw.html", t="policy", e=element_data, pp=pp)


@edit_blueprint.route("/new/policy", methods=["POST"])
def post_new_policy():
    j = json.loads(request.form["j"])

    r = a10.asvr.policies.addPolicy(j)
    # print("Return code ",r)
    if r.rc() != a10.structures.constants.SUCCESS:
        flash(
            "Database update error code " + str(r.rc()) + " " + str(r.msg()), "danger"
        )
        return redirect("/policies")
    else:
        flash("This policy has been created successfully", "success")
        # print("Here ",r.msg())
        return redirect("/policy/" + r.msg())


@edit_blueprint.route("/delete/policy/<item_id>", methods=["GET"])
def delete_policy(item_id):
    r = a10.asvr.policies.deletePolicy(item_id)
    flash("Deleted policy result: " + str(r.rc()) + " " + str(r.msg()), "info")
    return redirect("/policies")


@edit_blueprint.route("/edit/expectedvalue", methods=["POST"])
def post_expected_value():
    j = json.loads(request.form["j"])
    i = request.form["i"]
    # print("J",type(j),j,"\n","I",i,"\n")
    if j["itemid"] != i:
        flash(
            "Attempt to modify itemid in raw JSON form ignored - do not try to do this again OK!",
            "warning",
        )
        j["itemid"] = i

    r = a10.asvr.expectedvalues.updateExpectedValue(j)
    # print("Return code ",r)
    if r.rc() != a10.structures.constants.SUCCESS:
        flash(
            "Database update error code " + str(r.rc()) + " " + str(r.msg()), "danger"
        )
    else:
        flash("This element has been updated successfully", "success")

    return redirect("/expectedvalue/" + j["itemid"])


@edit_blueprint.route("/edit/expectedvalue/<item_id>", methods=["GET"])
def edit_expected_value(item_id):
    # print("editing ",itemid)
    element_data = a10.asvr.expectedvalues.getExpectedValue(item_id)

    if element_data.rc() != 0:
        flash(
            "Element does not exist " + itemid + " code is " + element_data.rc(),
            "danger",
        )
    else:
        pp = json.dumps(element_data.msg(), sort_keys=True, indent=4)
        return render_template(
            "editraw.html", t="expectedvalue", e=element_data.msg(), pp=pp
        )


@edit_blueprint.route("/new/expectedvalue", methods=["GET"])
def new_expected_value():
    # print("new element")
    element_data = {
        # "type": "ev type", # TODO: Should this be removed? It was dupplicated by the last row
        "name": "EDIT_THIS",
        "description": "EDIT_THIS_LONGER_DESCRIPTION",
        "evs": {},
        "elementID": "EDIT_THIS_ELEMENT_ID",
        "policyID": "EDIT_THIS_POLICY_ID",
        "type": "tpm2_attestedValuePCRdigest",
    }

    pp = json.dumps(element_data, sort_keys=True, indent=4)

    return render_template("newraw.html", t="expectedvalue", e=element_data, pp=pp)


@edit_blueprint.route("/new/expectedvalue", methods=["POST"])
def post_new_expected_value():
    j = json.loads(request.form["j"])

    r = a10.asvr.expectedvalues.addExpectedValue(j)
    # print("Return code ",r)
    if r.rc() != a10.structures.constants.SUCCESS:
        flash(
            "Database update error code " + str(r.rc()) + " " + str(r.msg()), "danger"
        )
        return redirect("/expectedvalues")
    else:
        flash("This expected value has been created successfully", "success")
        # print("Here ",r.msg())
        return redirect("/expectedvalue/" + r.msg())


@edit_blueprint.route("/delete/expectedvalue/<item_id>", methods=["GET"])
def delete_expected_value(item_id):
    r = a10.asvr.expectedvalues.deleteExpectedValue(item_id)
    flash("Deleted expectedvalue result: " + str(r.rc()) + " " + str(r.msg()), "info")
    return redirect("/expectedvalues")
