# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import sys
import a10.asvr.elements
import a10.asvr.policies
import a10.asvr.expectedvalues
import a10.asvr.claims
import a10.asvr.results
import a10.asvr.attestation
import a10.structures.constants
import a10.asvr.rules.rule_dispatcher


part = 0
count = 0


def bigbanner(t):
    global part, count
    count = 0
    part = part + 1
    print(" ")
    print("+========================================================================")
    print("+")
    print("+ Part", part, "   ", t)
    print("+")
    print("+========================================================================")


def banner(t):
    global count
    count = count + 1
    print(" ")
    print("+------------------------------------------------------------------------")
    print("+ Test", part, "/", count, "   ", t)
    print("+------------------------------------------------------------------------")


#
#
#  START HERE
#
#


#
#
#  Element Tests
#
#


bigbanner("Initialisation")
banner("Creating an element, policy and expected value")


e = {
    "type": ["tpm2.0"],
    "name": "Test Dummy",
    "description": "Dummy Element for Test",
    "hostname": "iolive-2-ThinkPad-X-Carbon-5th",
    "endpoint": "http://192.168.1.218:8530",
    "protocol": "A10DUMMYPROTOCOL",
    "asurl": ["http://192.168.71.128:8510", "http://10.144.176.154:8510"],
    "ek_pem": "ABC",
    "ak_pem": "DEF",
    "ek_name": "QPR",
    "ak_name": "XYZ",
    "hash": "882389482934",
    "signature": "xbbxfgbxfb",
    "itemid": "bebc3af9-5050-45ee-9b54-656a68de56ed",
    "new field": "this is a new field",
}


print(e)

banner("Adding element")

r = a10.asvr.elements.addElement(e)
print("Result code ", r.rc(), r.msg())

if r.rc() != a10.structures.constants.SUCCESS:
    print("An error has occured")
    sys.exit("Stop.")

eid = r.msg()

e = {
    "intent": "tpm2/quote",
    "criteria": {"pcrselection": "sha256:0,1,2,3,4", "hashalg": "sha256",},
    "parameters": {},
}
print(e)


banner("Adding policy")

r = a10.asvr.policies.addPolicy(e)
print("Result code ", r.rc(), r.msg())

if r.rc() != a10.structures.constants.SUCCESS:
    print("An error has occured")
    sys.exit("Stop.")

pid = r.msg()

e = {
    "elementID": eid,
    "policyID": pid,
    "evs": {"attestedValue": "anflbl5igtvzxligvxezt", "firmware": "1234567890"},
}
print(e)


banner("Adding expected value")

r = a10.asvr.expectedvalues.addExpectedValue(e)
print("Result code ", r.rc(), r.msg())

if r.rc() != a10.structures.constants.SUCCESS:
    print("An error has occurred")
    sys.exit("Stop.")

evid = r.msg()

print("Element, Policy and EV IDs are ", eid, pid, evid)


bigbanner("Attestation")


banner("Showing registered rules")


r = a10.asvr.rules.rule_dispatcher.getRegisteredRules()
print("Result code A", r, type(r))


banner("Calling attest")

r = a10.asvr.attestation.attest(eid, pid, {})
print("R is ", r)
print("Result code 1 ", r.rc(), r.msg())
cid = r.msg()
r = a10.asvr.claims.getClaim(cid)
print("Result code 2 ", r.rc(), r.msg(), type(r.msg()))


banner("Calling verify")

rules = ("nullrules/AlwaysError", {})
r = a10.asvr.attestation.verify(cid, rules)
print("Result code 3 ", r.rc(), r.msg(), type(r.msg()))


banner("Getting the result")
rid = r.msg()
r = a10.asvr.results.getResult(rid)
print("Result code ", r.rc(), r.msg(), type(r.msg()))
