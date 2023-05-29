# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

# from a10.asvr import elements, policies, claims
import pprint


class NamedItem:
    def __init__(self, n, d):
        print("named Item element init")
        self.s = {}
        print("Dictionary set")

        self.s["name"] = n
        self.s["description"] = d
        self.s["itemid"] = None

    def setItemID(self, i):
        self.s["itemid"] = i

    def setProperty(self, p, v):
        self.s[p] = v

    def j(self):
        # returns the structure as a python dictionary (or json)
        return self.s

    def pp(self, d=2, w=60):
        # prettyprints the structures
        pprint.pprint(self.s, depth=d, width=w)


#
#  Element Structures
#


class AbstractElement(NamedItem):
    def __init__(self, n, d):
        super().__init__(n, d)


class ElementGroup(AbstractElement):
    def __init__(self, n, d, es=[]):
        super().__init__(n, d)

        print("ElementGroup element init")

        self.s["type"] = "ElementGroup"
        self.s["elements"] = es


class BaseElement(AbstractElement):
    def __init__(self, n, d, e, a):
        super().__init__(n, d)

        print("Base element init")

        self.s["type"] = "<<abstract>>baseelement"
        self.s["endpoint"] = e
        self.s["asurl"] = a


class TPM2Element(BaseElement):
    def __init__(self, n, d, e, a, ek_pem, ek_name, ak_pem, ak_name):
        super().__init__(n, d, e, a)

        self.s["type"] = "tpm2.0"
        self.s["ek_pem"] = ek_pem
        self.s["ek_name"] = ek_name
        self.s["ak_pem"] = ak_pem
        self.s["ak_name"] = ak_name


#
#  Policy Structures
#


class BasePolicy(NamedItem):
    def __init__(self, n, d):
        super().__init__(n, d)

        self.s["criteria"] = {}


class TPM2Quote(BasePolicy):
    def __init__(self, n, d, p, h="sha256"):
        super().__init__(n, d)

        self.s["type"] = "tpm2/quote"
        self.s["criteria"]["pcrselection"] = p
        self.s["criteria"]["hashalg"] = h


class TPM2PCRS(BasePolicy):
    def __init__(self, n, d):
        super().__init__(n, d)

        self.s["type"] = "tpm2/pcrs"


class TPM2EventLog(BasePolicy):
    def __init__(self, n, d):
        super().__init__(n, d)

        self.s["type"] = "tpm2/eventlog"


#
#  Expected Value Structures
#


class BaseExpectedValue(NamedItem):
    def __init__(self, n, d, eid, pid):
        super().__init__(n, d)

        self.s["elementID"] = eid
        self.s["policyID"] = pid
        self.s["evs"] = {}


class TPM2AttestedValuePCRDigest(BaseExpectedValue):
    def __init__(self, n, d, eid, pid, pcrDigest):
        super().__init__(n, d, eid, pid)

        self.s["type"] = "tpm2_attestedValuePCRdigest"
        self.s["evs"]["pcrDigest"] = pcrDigest


class TPM2Firmware(BaseExpectedValue):
    def __init__(self, n, d, eid, pid, f):
        super().__init__(n, d, eid, pid)

        self.s["type"] = "tpm2_firmwareVersion"
        self.s["evs"]["firmwareVersion"] = f
        self.s["evs"]["test"] = "fred"


#
#  Claim
#


#
#  Result
#


#
#  HashLibEntry
#
