# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

#import datetime
#import os
#import sys
import secrets

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

from flask import Blueprint, jsonify, request

v2_blueprint = Blueprint(
    "home", __name__, static_folder="../static", template_folder="../templates/",url_prefix='/v2'
)

secret = secrets.token_urlsafe(64)
v2_blueprint.secret_key = secret


#
# Home
#


@v2_blueprint.route("/")
def hello():
    return {"msg":"Hello from A10REST v2"},200


#
# ELEMENTS (ALL OF THEM)
#


@v2_blueprint.route("/elements", methods=["GET"])
def getelements():
    es = [x["itemid"] for x in elements.getElements()]
    return jsonify({"elements":es,"count":len(es)}), 200


@v2_blueprint.route("/elements/types", methods=["GET"])
def getTypes():
    ts = list(types.getTypes())
    return jsonify({"types":ts}), 200

@v2_blueprint.route("/elements/type/<elementtype>", methods=["GET"])
def getElementsByType(elementtype):
    es = [x["itemid"] for x in elements.getElementsByType(elementtype)]
    return jsonify({"elements":es,"count":len(es),"type":elementtype}), 200

#
# ELEMENT (SINGULAR)
#


@v2_blueprint.route("/element/<itemid>", methods=["GET"])
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
        return jsonify({"msg":elem.msg()}), 404
    else:
        return jsonify(elem.msg()), 200



@v2_blueprint.route("/element/name/<elementname>", methods=["GET"])
def getelementbyname(elementname):
    elem = elements.getElementByName(elementname)

    if elem.rc() != constants.SUCCESS:
        return jsonify({"msg":elem.msg()}), 404
    else:
        return jsonify(elem.msg()), 200


@v2_blueprint.route("/element", methods=["POST"])
def addElement():
    content=request.json

    e = elements.addElement(content)

    if e.rc() != constants.SUCCESS:
        return jsonify({"msg":e.msg()}), 400
    else:
        return jsonify({"itemid":e.msg()}), 201


@v2_blueprint.route("/element/<itemid>", methods=["DELETE"])
def deleteElement(itemid):
    #itemid = request.args.get("itemid")
    print("itemid", itemid)
    e = elements.deleteElement(itemid)

    if e.rc() != constants.SUCCESS:
        return jsonify({"msg":e.msg()}), 404
    else:
        return jsonify({"msg":e.msg()}), 200


@v2_blueprint.route("/element/<itemid>", methods=["PUT","PATCH"])
def updateElement(itemid):
    print("HERE PUT")
    content = request.json
    print("PUT parameter",itemid)
    print("PUT content", content)
    
    e = elements.updateElement(content)
    print(e)
    print(e.rc(),e.msg())

    if e.rc() != constants.SUCCESS:
        return jsonify({"msg":e.msg()}), 400
    else:
        return jsonify({"msg":e.msg()}), 200

#
# POLICIES
#
@v2_blueprint.route("/policies", methods=["GET"])
def getpolicies():
    ps = [x["itemid"] for x in policies.getPolicies()]
    print("PS",ps)
    return jsonify({"policies":ps,"count":len(ps)}), 200


@v2_blueprint.route("/policy/<itemid>", methods=["GET"])
def getpolicy(itemid):
    e = policies.getPolicy(itemid)
    
    if e.rc() != constants.SUCCESS:
        return jsonify({"msg":e.msg()}), 404
    else:
        return jsonify(e.msg()), 200




@v2_blueprint.route("/policy/name/<policyname>", methods=["GET"])
def getpolicybyname(policyname):
    pol = policies.getPolicyByName(policyname)

    if pol.rc() != constants.SUCCESS:
        return jsonify({"msg":pol.msg()}), 404
    else:
        return jsonify(pol.msg()), 200



@v2_blueprint.route("/policy", methods=["POST"])
def addPolicy():
    content = request.json
    print("content", content)

    e = policies.addPolicy(content)

    if e.rc() != constants.SUCCESS:
        return jsonify({"msg":e.msg()}), 400
    else:
        return jsonify({"policy":e.msg()}), 201


@v2_blueprint.route("/policy", methods=["DELETE"])
def deletePolicy():
    itemid = request.args.get("itemid")
    print("itemid", itemid)
    e = policies.deletePolicy(itemid)

    if e.rc() != constants.SUCCESS:
        return jsonify({"msg":e.msg()}), 404
    else:
        return jsonify({"msg":e.msg()}), 200


@v2_blueprint.route("/policy", methods=["PUT","PATCH"])
def updatePolicy():
    content = request.json
    print("content", content)

    e = policies.updatePolicy(content)

    if e.rc() != constants.SUCCESS:
        return jsonify({"msg":e.msg()}), 404
    else:
        return jsonify({"msg":e.msg()}), 200


#
# EXPECTED VALUES
#


@v2_blueprint.route("/expectedvalue/<itemid>", methods=["GET"])
def getEV(itemid):
    print("itemid", itemid)
    e = expectedValues.getExpectedValue(itemid)

    if e.rc() != constants.SUCCESS:
        return jsonify({"msg":e.msg()}), 404
    else:
        return jsonify({"expectedvalue":e.msg()}), 200


@v2_blueprint.route("/expectedvalue/<eid>/<pid>", methods=["GET"])
def getEVep(eid, pid):

    print(" eid", eid, " pid", pid)
    e = expectedValues.getExpectedValueByElementPolicyIDs(typ, eid, pid)

    if e.rc() != constants.SUCCESS:
        return jsonify({"msg":e.msg()}), 404
    else:
        return jsonify({"expectedvalue":e.msg()}), 200


@v2_blueprint.route("/expectedvalue", methods=["POST"])
def addEV():
    content = request.json
    print("content", content)

    e = expectedValues.addExpectedValue(content)

    if e.rc() != constants.SUCCESS:
        return jsonify({"msg":e.msg()}), 404
    else:
        return jsonify({"expectedvalue":e.msg()}), 201


@v2_blueprint.route("/expectedvalue/<itemid>", methods=["DELETE"])
def deleteEV(itemid):
    print("itemid", itemid)
    e = expectedValues.deleteExpectedValue(itemid)

    if e.rc() != constants.SUCCESS:
        return jsonify({"msg":e.msg()}), 404
    else:
        return jsonify({"msg":e.msg()}), 200


@v2_blueprint.route("/expectedvalue", methods=["PUT","PATCH"])
def updateEV():
    content = request.json
    print("content", content)

    e = expectedValues.updateExpectedValue(content)

    if e.rc() != constants.SUCCESS:
        return jsonify({"msg":e.msg()}), 404
    else:
        return jsonify({"msg":e.msg()}), 200


#
# CLAIMS - decided not to allow writing claims for the moment as the ASVR libraires do this during attestation
#

@v2_blueprint.route("/claims", methods=["GET"])
def getclaims():
    if("limit" in request.args):
        try:
            lim = int(request.args["limit"])
        except ValueError:
            lim = 10
    else:
        lim = 10
    cs = [x["itemid"] for x in claims.getClaims(lim)]
    return jsonify({"claims":cs,"count":len(cs),"limit":lim}), 200
   

@v2_blueprint.route("/claims/element/<itemid>", methods=["GET"])
def getclaimsforelement(itemid):
    if("limit" in request.args):
        try:
            lim = int(request.args["limit"])
        except ValueError:
            lim = 100
    else:
        lim = 100


    cs = [x["itemid"] for x in claims.getClaimsForElement(itemid,lim)]
    return jsonify({"claims":cs,"count":len(cs),"limit":lim}), 200


@v2_blueprint.route("/claim/<itemid>", methods=["GET"])
def getclaim(itemid):
    print("itemid", itemid)
    e = claims.getClaim(itemid)

    if e.rc() != constants.SUCCESS:
        return jsonify({"e":elem.msg()}), 404
    else:
        return jsonify({"claim":e.msg()}), 200


#
# RESULTS - decided not to allow writing claims for the moment as the ASVR libraries do this during attestation
#

@v2_blueprint.route("/results", methods=["GET"])
def getResults():
    """
       Returns the latest x results
    """
    if("limit" in request.args):
        try:
            lim = int(request.args["limit"])
        except ValueError:
            lim = 500
    else:
        lim = 500
    rs = [x["itemid"] for x in results.getResults(lim)]
    return jsonify({"results":rs,"count":len(rs),"limit":lim}), 200
   
 
@v2_blueprint.route("/results/latest", methods=['GET'])
def getresultslatest():
    """
        get the the results since a given point in time
    """    
    if("timestamp" not in request.args): # All results since timestamp
        return jsonify({"msg":"value error in timestamp"}), 400

    out = results.getResultsSince(float(request.args["timestamp"]))

    return jsonify({"results":out,"since":request.args["timestamp"],"count":len(out)}), 200





@v2_blueprint.route("/result/<itemid>", methods=["GET"])
def getresult(itemid):
    print("itemid", itemid)
    e = results.getResult(itemid)

    if e.rc() != constants.SUCCESS:
        return jsonify({"msg":e.msg()}), 404
    else:
        return jsonify({"result":e.msg()}), 200



@v2_blueprint.route("/results/element/latest/<itemid>", methods=['GET'])
def getresultslatestforelement(itemid):
    """
        get the the results since a given point in time for a given element
    """    
    if ("timestamp" not in request.args): # All results since timestamp
        return jsonify({"msg":"value error in timestamp"}), 400
    if ("limit" in request.args):
        try:
            lim = int(request.args["limit"])
        except ValueError:
            return jsonify({"msg":"value error in limit"}), 400
    else:
        lim = 500
    rs = results.getLatestResults(itemid, lim)
    return jsonify({"count":len(rs),"results":rs, "limit":lim, "elementID":itemid}), 200



@v2_blueprint.route("/results/element/latest/<eid>/<pid>", methods=['GET'])
def getresultslatestforelementandpolicy(eid,pid):
    """
        get the the results since a given point in time for a given element
    """    
    #if ("timestamp" not in request.args): # All results since timestamp
    #    return jsonify({"msg":"value error in timestamp"}), 400
    if ("limit" in request.args):
        try:
            lim = int(request.args["limit"])
        except ValueError:
            return jsonify({"msg":"value error in limit"}), 400
    else:
        lim = 500
    rs = results.getLatestResultsForElementAndPolicy(eid,pid, lim)
    return jsonify({"count":len(rs),"results":rs, "limit":lim, "elementID":eid, "policyID":pid}), 200




#
# ATTESTATION and VERIFICATION
#


@v2_blueprint.route("/attest", methods=["POST"])
def attest():
    content = request.json

    eid=None
    pid=None
    cps=None

    try:
        eid = content["eid"]
        pid = content["pid"]
        cps = content["cps"]
    except:
        return jsonify({"msg":"Missing one or more of eid,pid and/or cps"}),400

    e = attestation.attest(eid, pid, cps)
    
    print("\nreturn from attest ",e,e.rc(),e.msg())

    if e.rc() != constants.SUCCESS:
        return jsonify({"msg":e.msg()}), 404
    else:
        return jsonify({"claim",e.msg()}), 201


@v2_blueprint.route("/verify", methods=["POST"])
def verify():
    content = request.json

    cid=None
    rul=None

    try:
        cid = content["cid"]
        rul = content["rule"]
    except:
        return jsonify({"msg":"Missing cid and/or rule"}),400

    e = attestation.verify(cid, rul)

    if e.rc() != constants.SUCCESS:
        return jsonify({"msg":e.msg()}), 404
    else:
        return jsonify({"result",e.msg()}), 201



#
# Rules
#

@v2_blueprint.route("/rules", methods=["GET"])
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


@v2_blueprint.route("/msg", methods=["POST"])
def receiveMessage():
    content = request.json
    print("msg content",content)

    msg=content.get('msg',"mising message")
    ope=content.get('op',"-")
    eid=content.get('elementid',"missing element ID")
    
    a10.asvr.db.announce.announceMessage(ope,{ 'msg':msg, 'elementid':eid })

    print("Message Received ",msg)
    return jsonify({"msg":"ack"}),200

