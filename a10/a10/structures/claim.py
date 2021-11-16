# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import a10.structures.timestamps


class Claim:
    def __init__(self, e_struct, p_struct, treq, trec, payload, aps):
        self.s = {}
        self.s["header"] = {}

        self.s["header"]["element"] = e_struct
        self.s["header"]["policy"] = p_struct
        self.s["header"]["as_received"] = trec
        self.s["header"]["as_requested"] = treq
        self.s["header"]["additionalparameters"] = aps
        self.s["payload"] = payload

    def asDict(self):
        return self.s
