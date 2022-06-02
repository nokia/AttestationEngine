# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import datetime
import os
import sys

import argparse

from a10.asvr import (
    elements,
    policies,
    attestation,
    claims,
    expectedvalues,
    results,
    types,
)
from a10.structures import constants
from a10.asvr.db import announce
import a10.asvr.rules.rule_dispatcher

from bson.objectid import ObjectId
from flask import Flask, request, send_from_directory, jsonify
from flask.json import JSONEncoder

from blueprints.v2 import v2_blueprint

a10rest = Flask(__name__)

a10rest.register_blueprint(v2_blueprint)


#
# This was added for the mobile app...not sure what it does
#

# class A10JSONEncoder(JSONEncoder):
#     def default(self,obj):
#         if isinstance(obj,ObjectId):
#             return str(obj)
#         return JSONEncoder.default(self, obj)
# a10rest.json_encoder = A10JSONEncoder

#
# Home
#


@a10rest.route("/")
def hello():
    return "Hello from A10REST"


#
# ELEMENTS (ALL OF THEM)
#


@a10rest.route("/elements", methods=["GET"])
def getelements():
    """
    Gets a list of all elements
    ---
    get:
       responses:
         - 200:
            content:
                application/json:
                    schema: list itemid
    """

    archived=False
    if "archived" in request.args:
        if int(request.args["archived"])==1:
            archived=True


    es = [x["itemid"] for x in elements.getElements(archived=archived)]
    return str(es), 200


@a10rest.route("/elements/types", methods=["GET"])
def getTypes():
    ts = str(types.getTypes())
    return ts, 200

@a10rest.route("/elements/type/<elementtype>", methods=["GET"])
def getElementsByType(elementtype):
    archived=False
    if "archived" in request.args:
        if int(request.args["archived"])==1:
            archived=True

    es = [x["itemid"] for x in elements.getElementsByType(elementtype,archived=archived)]
    return str(es), 200

#
# ELEMENT (SINGULAR)
#


@a10rest.route("/element/<itemid>", methods=["GET"])
def getelement(itemid):
    """
    Gets the details of a specific element
    ---
    get:
       parameters:
         - in: itemid
           schema: Item ID
       responses:
         - 404
         - 200:
            content:
                application/json:
                    schema: Element
    """
    elem = elements.getElement(itemid)

    if elem.rc() != constants.SUCCESS:
        return elem.msg(), 404
    else:
        return elem.msg(), 200



@a10rest.route("/element/name/<elementname>", methods=["GET"])
def getelementbyname(elementname):
    elem = elements.getElementByName(elementname)

    if elem.rc() != constants.SUCCESS:
        return elem.msg(), 404
    else:
        return elem.msg(), 200


@a10rest.route("/element", methods=["POST"])
def addElement():
    content = request.json
    print("content", content)

    e = elements.addElement(content)

    if e.rc() != constants.SUCCESS:
        return e.msg(), 400
    else:
        return e.msg(), 201


@a10rest.route("/element", methods=["DELETE"])
def deleteElement():
    itemid = request.args.get("itemid")
    print("itemid", itemid)
    e = elements.deleteElement(itemid)

    if e.rc() != constants.SUCCESS:
        return e.msg(), 404
    else:
        return e.msg(), 200


@a10rest.route("/element", methods=["PUT"])
def updateElement():
    content = request.json
    print("content", content)

    e = elements.updateElement(content)

    if e.rc() != constants.SUCCESS:
        return e.msg(), 400
    else:
        return e.msg(), 200


#
# POLICIES
#
@a10rest.route("/policies", methods=["GET"])
def getpolicies():

    ps = [x["itemid"] for x in policies.getPolicies()]
    print("ES is ", ps)
    return str(ps), 200


@a10rest.route("/policy/<itemid>", methods=["GET"])
def getpolicy(itemid):

    print("itemid", itemid)
    e = policies.getPolicy(itemid)

    if e.rc() != constants.SUCCESS:
        return e.msg(), 404
    else:
        return e.msg(), 200




@a10rest.route("/policy/name/<policyname>", methods=["GET"])
def getpolicybyname(policyname):
   
    pol = policies.getPolicyByName(policyname)

    if pol.rc() != constants.SUCCESS:
        return pol.msg(), 404
    else:
        return pol.msg(), 200



@a10rest.route("/policy", methods=["POST"])
def addPolicy():
    content = request.json
    print("content", content)

    e = policies.addPolicy(content)

    if e.rc() != constants.SUCCESS:
        return e.msg(), 400
    else:
        return e.msg(), 201


@a10rest.route("/policy", methods=["DELETE"])
def deletePolicy():
    itemid = request.args.get("itemid")
    print("itemid", itemid)
    e = policies.deletePolicy(itemid)

    if e.rc() != constants.SUCCESS:
        return e.msg(), 404
    else:
        return e.msg(), 200


@a10rest.route("/policy", methods=["PUT"])
def updatePolicy():
    content = request.json
    print("content", content)

    e = policies.updatePolicy(content)

    if e.rc() != constants.SUCCESS:
        return e.msg(), 400
    else:
        return e.msg(), 200


#
# EXPECTED VALUES
#


@a10rest.route("/expectedvalue/<itemid>", methods=["GET"])
def getEV(itemid):
    print("itemid", itemid)
    e = expectedValues.getExpectedValue(itemid)

    if e.rc() != constants.SUCCESS:
        return e.msg(), 404
    else:
        return e.msg(), 200


@a10rest.route("/expectedvalue/<eid>/<pid>", methods=["GET"])
def getEVep(eid, pid):

    print(" eid", eid, " pid", pid)
    e = expectedValues.getExpectedValueByElementPolicyIDs(typ, eid, pid)

    if e.rc() != constants.SUCCESS:
        return e.msg(), 404
    else:
        return e.msg(), 200


@a10rest.route("/expectedvalue", methods=["POST"])
def addEV():
    content = request.json
    print("content", content)

    e = expectedValues.addExpectedValue(content)

    if e.rc() != constants.SUCCESS:
        return e.msg(), 400
    else:
        return e.msg(), 201


@a10rest.route("/expectedvalue/<itemid>", methods=["DELETE"])
def deleteEV(itemid):
    print("itemid", itemid)
    e = expectedValues.deleteExpectedValue(itemid)

    if e.rc() != constants.SUCCESS:
        return e.msg(), 404
    else:
        return e.msg(), 200


@a10rest.route("/expectedvalue", methods=["PUT"])
def updateEV():
    content = request.json
    print("content", content)

    e = expectedValues.updateExpectedValue(content)

    if e.rc() != constants.SUCCESS:
        return e.msg(), 400
    else:
        return e.msg(), 200


#
# CLAIMS - decided not to allow writing claims for the moment as the ASVR libraires do this during attestation
#


@a10rest.route("/claim/<itemid>", methods=["GET"])
def getclaim(itemid):
    print("itemid", itemid)
    e = claims.getClaim(itemid)

    if e.rc() != constants.SUCCESS:
        return e.msg(), 404
    else:
        return e.msg(), 200


#
# RESULTS - decided not to allow writing claims for the moment as the ASVR libraires do this during attestation
#


@a10rest.route("/result/<itemid>", methods=["GET"])
def getresult(itemid):
    print("itemid", itemid)
    e = results.getResult(itemid)

    if e.rc() != constants.SUCCESS:
        return e.msg(), 404
    else:
        return e.msg(), 200


@a10rest.route("/results/latest", methods=['GET'])
def getresultslatest():
    args = request.args
    out = list()
    
    if("timestamp" in args): # All results since timestamp
        try:
            out = results.getResultsSince(float(args["timestamp"]))
        except ValueError:
            return jsonify(out), 200
    else: ## First latest result of each element
        elems = elements.getElements()
        for i in range(len(elems)):
            r = results.getLatestResults(elems[i]['itemid'],1)
            if(r and r[0]):
                out.append(r[0])


    return jsonify(out), 200

@a10rest.route("/results/element/latest/<itemid>", methods=['GET'])
def getresultslatestlimit(itemid):
    args = request.args
    print("itemid", itemid)

    if("limit" in args):
        try:
            lim = int(args["limit"])
        except ValueError:
            lim = 10
    else:
        lim = 10
    
    rs = results.getLatestResults(itemid, lim)
    return jsonify(rs), 200

#
# ATTESTATION and VERIFICATION
#


@a10rest.route("/attest", methods=["POST"])
def attest():
    content = request.json
    print("\n\n****\n")
    eid = content["eid"]
    pid = content["pid"]
    cps = content["cps"]

    e = attestation.attest(eid, pid, cps)
    
    print("\nreturn from attest ",e,e.rc(),e.msg())

    if e.rc() != constants.SUCCESS:
        print(" returning 400")
        return e.msg(), 400
    else:
        print(" returning 201")
        return e.msg(), 201


@a10rest.route("/verify", methods=["POST"])
def verify():
    content = request.json
    #print("content", content)
    cid = content["cid"]
    rul = content["rule"]

    e = attestation.verify(cid, rul)

    if e.rc() != constants.SUCCESS:
        return e.msg(), 400
    else:
        return e.msg(), 201



#
# Rules
#

@a10rest.route("/rules", methods=["GET"])
def getRules():
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
    return jsonify(rsl), 200





#
# MESSAGES
#


@a10rest.route("/msg", methods=["POST"])
def receiveMessage():
    content = request.json
    print("msg content",content)

    msg=content.get('msg',"mising message")
    ope=content.get('op',"-")
    eid=content.get('elementid',"missing element ID")
    
    a10.asvr.db.announce.announceMessage(ope,{ 'msg':msg, 'elementid':eid })

    print("Message Received ",msg)
    return "rcvd",200
#
# Main
#


def main_debug(cert, key, config_filename="a10rest.conf"):
    a10rest.config.from_pyfile(config_filename)
    if cert and key:
        print("running in secure mode")        
        a10rest.run(
            debug=a10rest.config["FLASKDEBUG"],
            threaded=a10rest.config["FLASKTHREADED"],
            host=a10rest.config["DEFAULTHOST"],
            port=a10rest.config["DEFAULTPORT"],
            ssl_context=(cert, key),
        )
    else:
        print("running in insecure mode")        
        a10rest.run(
            debug=a10rest.config["FLASKDEBUG"],
            threaded=a10rest.config["FLASKTHREADED"],
            host=a10rest.config["DEFAULTHOST"],
            port=a10rest.config["DEFAULTPORT"],
        )

def main_production(cert, key, config_filename="a10rest.conf", t=16):
   from waitress import serve
   a10rest.config.from_pyfile(config_filename)
   serve(a10rest, host=a10rest.config["DEFAULTHOST"], port=a10rest.config["DEFAULTPORT"], threads=t)


ap = argparse.ArgumentParser(description='A10REST Nokia Attestation Server REST API')
ap.add_argument('-p', '--production', help="Run the REST server in production mode (Waitress instread of Flask Debug)",  action='store_true')
args = ap.parse_args()


if __name__ == "__main__":
    print("A10REST Starting")
    if args.production==True:
        print("Running in Production Mode")
        main_production("", "")
    else:
        print("Running in Debug Mode")        
        main_debug("","")