# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause


import a10.structures.constants
import a10.structures.identity
import a10.structures.returncode
import a10.structures.timestamps
import a10.structures.claim
import a10.structures.result

import a10.asvr.protocols.protocol_dispatcher
import a10.asvr.rules.rule_dispatcher

from a10.asvr import elements, policies, expectedvalues, claims, results


def attest(e, p, aps):
    """
    This is the attestation process. Adds the claim if not an error to the database - well depends upon the kind of error, eg: network timeout, 404 etc
    
    :params uuid4 e: The item id of the elent
    :params uui4 p: The itemi do the policy
    :params dict aps: the additional paraeters to be used
    :returns ResultCode:
    :rtype: ResultCode
    """

    # 1. get the element structure

    element = elements.getElement(e).msg()

    # 2. get the policy structure

    policy = policies.getPolicy(p).msg()

    # 3. call the element at the correct endpoint -- using POST (to be HTTP standard compliant because stuff goes in the body)
    #       -- all sorts of stuff can happen here depending upon the type, eg: tpms_attest requires a certain kind of nonce etc

    result = resolvePolicyIntent(element, policy, aps)

    # 4. get the result and add it to the claims
    #       -- generate a claim ID (aid is the standard term we are using for all objects) and push it into the database and return the claim ID
    if result.rc() == a10.structures.constants.PROTOCOLSUCCESS:
        addClaimResult = claims.addClaim(result.msg())
        return addClaimResult
    else:
        return result


def resolvePolicyIntent(element, policy, additionalparameters):
    """
      The type are STR, STR and DICT (!!! <- dict is really important!!!)
    
    
      This function actually resolves the policy intent
      It returns a claim structure
    
    
    """

    endpoint = element["endpoint"]
    protocol = element["protocol"]
    policyintent = policy["intent"]
    policyparameters = policy["parameters"]
    e_aid = element["itemid"]
    p_aid = policy["itemid"]

    requestedTime = a10.structures.timestamps.now()

    #
    # In reality, these behave as "intents" ... the policy structure etc is just a DSL for
    # the request to the protocol object .
    # In the case of A10HTTPREST, nothing needs to be done - this is probably not a good idea
    # in the long term :)
    #

    #
    # So now we should have everything in good shape
    #
    # endpoint
    # policyintent
    # policyparameters
    # callparameters
    # protocol
    #
    # So we can now do a simple call to a specific protocol class and call the "exec" function
    #
    # First set up the protocol handler class
    #

    handler_return = a10.asvr.protocols.protocol_dispatcher.getProtocolHandler(protocol)
    if handler_return.rc() != a10.structures.constants.SUCCESS:
        return handler_return  # this is a return structure anyway :)

    protocol_handler = handler_return.msg()  # this is the actual class instance
    handler_instance = protocol_handler(
        endpoint, policyintent, policyparameters, additionalparameters
    )

    #
    # And make the call!
    #

    exec_result = handler_instance.exec()

    if exec_result.rc() != a10.structures.constants.PROTOCOLSUCCESS:
        return exec_result  # this is a ResultCode already

    # Into this variable is where we write the finalised JSON result
    # actually it is a python dict and we convert afterwards
    resultStructure = exec_result.msg()
    receivedTime = a10.structures.timestamps.now()
    theClaim = a10.structures.claim.Claim(
        element,
        policy,
        requestedTime,
        receivedTime,
        resultStructure,
        additionalparameters,
    )

    # If we get here then everything has gone well - we got something. If the network failed then we still get a claim
    if exec_result.rc() != a10.structures.constants.PROTOCOLSUCCESS:
        return exec_result  # this is a ResultCode already
    else:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.PROTOCOLSUCCESS, theClaim.asDict()
        )


def verify(cid, rule):
    # cid is a claim ID
    # r is a structure of rules   -  this must be a DICT

    # Verify the claim according to rule r

    # A rule is a 2-tuple  ( rule name, parameters )
    # Where the parameters is a json document that may be understood by the receiving rule
    # The claim contains the eid and pid, for example: ("tpm2_firmwareVersion", {} )

    rule_name = rule[0]
    rule_parameters = rule[1]

    # The fule is constructed as a string...
    handler_return = a10.asvr.rules.rule_dispatcher.getRuleHandler(rule_name)
    if handler_return.rc() != a10.structures.constants.RULESUCCESS:
        return handler_return  # this is a return structure anyway :)

    rule_handler = handler_return.msg()  # this is the actual class instance
    handler_instance = rule_handler(cid, rule_parameters)

    #
    # And make the call!
    #

    application_result = handler_instance.apply()

    # Into this variable is where we write the finalised JSON result
    # actually it is a python dict and we convert afterwards

    verifiedAt = a10.structures.timestamps.now()

    # get the element and policy ids from the claim and add these to the results
    clm = claims.getClaim(cid).msg()

    # What needs to be in a result are:
    # the ids of the claim, pid and eid
    # the rule name that was applied
    # the parameters to that rule
    # verification time

    # the rule results   application_result[0]
    # the message           [1]
    # additional            [2]

    cid = clm["itemid"]
    eid = clm["header"]["element"]["itemid"]
    pid = clm["header"]["policy"]["itemid"]

    theResult = a10.structures.result.Result(
        application_result["result"],
        application_result["message"],
        application_result["additional"],
        eid,
        pid,
        cid,
        verifiedAt,
        rule_parameters,
        rule_name,
        application_result["ev"],
    )

    # and add the result to the database and return the result
    rid = results.addResult(theResult.asDict())
    return rid
