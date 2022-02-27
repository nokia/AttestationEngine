# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

from urllib.parse import urlparse, parse_qs

import serial

import a10.asvr.protocols.A10ProtocolBase


class A10Usb(a10.asvr.protocols.A10ProtocolBase.A10ProtocolBase):
    NAME = "A10ARDUINOUSB"

    def __init__(self, endpoint, policyintent, policyparameters):
        super().__init__(endpoint, policyintent, policyparameters, None)

    def exec(self):
        print("Calling protocol A10ArduinoUSB ", self.endpoint)

        # Lots of cool pyserial stuff here
        url = urlparse(self.endpoint)
        cmd = parse_qs(url.query)["cmd"][0]
        cmd = "q\n"

        print("CALLING DEVICE AT ", url.path)
        print("COMMAND SEQUENCE  ", cmd.encode())

        ser = serial.Serial(url.path, timeout=3)
        ser.write(b"q")

        ret = ser.read(100)

        print("Returned ", ret)
        ser.close()

        # Here we need to make a valid claim structure
        payload = {"sn": "42"}
        header = {"ta_received": "1", "ta_complete": "2", "url": url}
        footer = {"hash": "12345", "signature": "fred signed this"}

        claim = {
            "type": "claim ",
            "header": header,
            "payload": payload,
            "footer": footer,
        }

        return claim
