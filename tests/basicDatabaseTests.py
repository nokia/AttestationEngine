# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import sys
import a10.asvr.elements
import a10.asvr.policies
import a10.asvr.expectedvalues
import a10.asvr.claims
import a10.asvr.results


import a10.structures.constants
import a10.structures.convenience


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


bigbanner("Element Tests")
banner("Creating element")

# Use this for convenience, but everything else uses dict as per the libraries
e_convenience = a10.structures.convenience.TPM2Element(
    "TestElement",
    "This is a test element",
    "http://127.0.0.1:8530",
    "A10HTTPREST",
    "ekpem",
    "ekname",
    "akpem",
    "akname",
)

# now as a dict
e = e_convenience.j()
print(e)

banner("Adding element")

r = a10.asvr.elements.addElement(e)
print("Result code ", r.rc(), r.msg())

if r.rc() != a10.structures.constants.SUCCESS:
    print("An error has occured")
    sys.exit("Stop.")

banner("Getting element")

eid = r.msg()
r = a10.asvr.elements.getElement(eid)
print("Result code ", r.rc(), r.msg(), type(r.msg()))

e2 = r.msg()

banner("Updating element")

e2["TestFlag"] = "ReallyYes"
e2["endpoint"] = "http://127.0.0.2:9876"

r = a10.asvr.elements.updateElement(e2)
print("Result code ", r.rc(), r.msg())

banner("Getting element")

r = a10.asvr.elements.getElement(eid)
print("Result code ", r.rc(), r.msg(), type(r.msg()))


banner("Deleting element")

r = a10.asvr.elements.deleteElement(eid)
print("Result code ", r.rc(), r.msg())

banner(
    "Getting element - this should fail with a "
    + str(a10.structures.constants.ITEMDOESNOTEXIST)
    + " error code"
)

r = a10.asvr.elements.getElement(eid)
print("Result code ", r.rc(), r.msg())


#
#
#  Policy Tests
#
#


bigbanner("Policy Tests")
banner("Creating policy")

# Use this for convenience, but everything else uses dict as per the libraries
e = {
    "intent": "tpm2/quote",
    "criteria": {"pcrselection": "sha256:0,1,2,3,4", "hashalg": "sha256"},
}
print(e)


banner("Adding policy")

r = a10.asvr.policies.addPolicy(e)
print("Result code ", r.rc(), r.msg())

if r.rc() != a10.structures.constants.SUCCESS:
    print("An error has occured")
    sys.exit("Stop.")

banner("Getting policy")

eid = r.msg()
r = a10.asvr.policies.getPolicy(eid)
print("Result code ", r.rc(), r.msg(), type(r.msg()))

e2 = r.msg()

banner("Updating policy")

e2["TestFlag"] = "ReallyYes"
e2["criteria"]["pcrselection"] = "sha256:0,1,2,3,4,5"

r = a10.asvr.policies.updatePolicy(e2)
print("Result code ", r.rc(), r.msg())

banner("Getting policy")

r = a10.asvr.policies.getPolicy(eid)
print("Result code ", r.rc(), r.msg(), type(r.msg()))


banner("Deleting policy")

r = a10.asvr.policies.deletePolicy(eid)
print("Result code ", r.rc(), r.msg())

banner(
    "Getting policy - this should fail with a "
    + str(a10.structures.constants.ITEMDOESNOTEXIST)
    + " error code"
)

r = a10.asvr.policies.getPolicy(eid)
print("Result code ", r.rc(), r.msg())


#
#
#  Expected Value Tests
#
#


bigbanner("Expected Value Tests")
banner("Creating expected value")

# Use this for convenience, but everything else uses dict as per the libraries
e = {
    "elementID": "123",
    "policyID": "456",
    "evs": {"attestedValue": "anflbl5igtvzxligvxezt", "firmware": "1234567890"},
}
print(e)


banner("Adding expected value")

r = a10.asvr.expectedvalues.addExpectedValue(e)
print("Result code ", r.rc(), r.msg())

if r.rc() != a10.structures.constants.SUCCESS:
    print("An error has occurred")
    sys.exit("Stop.")

banner("Getting expected value")

eid = r.msg()
r = a10.asvr.expectedvalues.getExpectedValue(eid)
print("Result code ", r.rc(), r.msg(), type(r.msg()))

e2 = r.msg()

banner("Updating expected value")

e2["TestFlag"] = "ReallyYes"

r = a10.asvr.expectedvalues.updateExpectedValue(e2)
print("Result code ", r.rc(), r.msg())

banner("Getting expected value")

r = a10.asvr.expectedvalues.getExpectedValue(eid)
print("Result code ", r.rc(), r.msg(), type(r.msg()))


banner("Deleting expected value")

r = a10.asvr.expectedvalues.deleteExpectedValue(eid)
print("Result code ", r.rc(), r.msg())

banner(
    "Getting expected value - this should fail with a "
    + str(a10.structures.constants.ITEMDOESNOTEXIST)
    + " error code"
)

r = a10.asvr.expectedvalues.getExpectedValue(eid)
print("Result code ", r.rc(), r.msg())


#
#
#  Claim Tests
#
#


bigbanner("Claim Tests")
banner("Creating claim")

c = {
    "header": {
        "as_requested": "1",
        "as_received": "1",
        "ta_received": "1",
        "ta_complete": "1",
        "elementID": "1",
        "policyID": "1",
    },
    "payload": "the payload",
    "footer": {"hash": "2", "signature": "2"},
}

print(c)


banner("Adding claim value")

r = a10.asvr.claims.addClaim(c)
print("Result code ", r.rc(), r.msg())

if r.rc() != a10.structures.constants.SUCCESS:
    print("An error has occurred")
    sys.exit("Stop.")

banner("Getting claim value")

eid = r.msg()
r = a10.asvr.claims.getClaim(eid)
print("Result code ", r.rc(), r.msg(), type(r.msg()))


#
#
#  Result Tests
#
#


bigbanner("Result Tests")
banner("Creating result")

c = {"type": "result", "verifiedAt": "1", "claimID": "1", "message": "f"}

print(c)


banner("Adding result value")

r = a10.asvr.results.addResult(c)
print("Result code ", r.rc(), r.msg())

if r.rc() != a10.structures.constants.SUCCESS:
    print("An error has occurred")
    sys.exit("Stop.")

banner("Getting result value")

eid = r.msg()
r = a10.asvr.results.getResult(eid)
print("Result code ", r.rc(), r.msg(), type(r.msg()))


#
#
#  Multiple Item Tests
#
#


bigbanner("Multiple Item Tests")
banner("No elements - assuming this is run on a blank database")

r = a10.asvr.elements.getElements()

banner("Creating multiple elements")

for i in range(1, 10):
    econv = a10.structures.convenience.TPM2Element(
        "TestElement" + str(i),
        "This is a test element",
        "http://127.0.0.1:8530",
        "A10HTTPREST",
        "ekpem",
        "ekname",
        "akpem",
        "akname",
    )

    r = a10.asvr.elements.addElement(econv.j())
    print("   --- adding element ", i, "-->", r.rc(), r.msg())

banner("Returning multiple elements")

r = a10.asvr.elements.getElements()
print("Element list is ", r.rc(), r.msg())
