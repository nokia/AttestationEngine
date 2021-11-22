# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import datetime
import hashlib


class Claim:
    def __init__(self):
        self.header = {}
        self.payload = {}
        self.signature = {}

    def addHeaderItem(self, k, v):
        self.header[k] = v

    def addPayloadItem(self, k, v):
        self.payload[k] = v

    def sign(self):
        m = hashlib.sha256()
        m.update(str(self.header).encode())
        m.update(str(self.payload).encode())
        self.signature["hash"] = str(m.digest())
        self.signature["signature"] = "signed"

    def getClaim(self):
        c = {"header": self.header, "payload": self.payload, "footer": self.signature}
        return c
