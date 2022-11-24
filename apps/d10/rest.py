#Copyright 2021 Nokia
#Licensed under the BSD 3-Clause Clear License.
#SPDX-License-Identifier: BSD-3-Clear

from flask import Flask, request, send_from_directory, jsonify, Blueprint, render_template
import secrets

import d10api
import d10db


rest_blueprint = Blueprint(
    "rest", __name__, static_folder="../static", template_folder="../templates/",url_prefix='/rest'
)

secret = secrets.token_urlsafe(64)
rest_blueprint.secret_key = secret

#
# Att Calls
#
@rest_blueprint.route("/atts", methods=["GET"])
def getatts():
    return d10db.getatts()

@rest_blueprint.route("/att/<name>", methods=["GET"])
def getattbyname(name):
    att = d10db.getatt(name)
    return jsonify(att), 200

@rest_blueprint.route("/att", methods=["POST"])
def addatt():
    print("add att ",request.json)
    content=request.json
    
    name = content["name"]
    att = content["att"]

    r = d10db.addatt(name,att)

    if r==True:
        return jsonify({"name":name,"msg":"success"}), 201
    else:
        return jsonify({"name":name,"msg":"database add failed"}), 400


#
# Eva
#
@rest_blueprint.route("/evas", methods=["GET"])
def getevas():
    return d10db.getevas()

@rest_blueprint.route("/eva/<name>", methods=["GET"])
def getevabyname(name):
    eva = d10db.getatt(name)
    return jsonify(eva), 200

@rest_blueprint.route("/eva", methods=["POST"])
def addeva():
    content=request.json
    
    name = content["name"]
    eva = content["eva"]

    r = d10db.addeva(name,eva)

    if r==True:
        return jsonify({"name":name,"msg":"success"}), 201
    else:
        return jsonify({"name":name,"msg":"database add failed"}), 400

#
# Exec Call
#

@rest_blueprint.route("/exec", methods=["GET"])
def exec():
    None
    #content=request.json

    #The post must contain
    # att name
    # eva name

    #att = content["att"]s
    #eva = content["eva"]
    #ept = content.get('endpoint', None)

    # go get att and eva from the database as attc and evac

    #report = d10api.executeAtteststion(att,eva,ept)

    #return jsonify({"report":report}), 400
