# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause


def quote(pcrs, ak, nonce):

    q = {
        "magic": "1",
        "type": "2",
        "qualifiedSigner": "3",
        "extraData": "4" + nonce,
        "resetCount": "5",
        "restartCount": "6",
        "safe": "7",
        "attested": "8",
        "firmwareVersion": "9",
        "signature": "0",
    }

    return q
