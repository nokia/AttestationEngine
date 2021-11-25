# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

# from a10.asvr.protocols import A10HttpRest, A10ContainerImage, A10ArduinoUSB
# from a10.asvr.protocols import A10DummyProtocol

import a10.asvr.protocols.A10DummyProtocol
import a10.asvr.protocols.A10HttpRest


import a10.structures.constants
import a10.structures.returncode

REGISTER = {
    # 	A10HttpRest.A10HttpRest.NAME: A10HttpRest.A10HttpRest,
    # 	A10ArduinoUSB.A10Usb.NAME: A10ArduinoUSB.A10Usb,
    # 	A10ContainerImage.A10Container.NAME: A10ContainerImage.A10Container
    a10.asvr.protocols.A10DummyProtocol.A10DummyProtocol.NAME: a10.asvr.protocols.A10DummyProtocol.A10DummyProtocol,
    a10.asvr.protocols.A10HttpRest.A10HttpRest.NAME: a10.asvr.protocols.A10HttpRest.A10HttpRest,
}


def getRegisteredProtocols():
    return REGISTER.keys()


def getProtocolHandler(n):
    try:
        print("Getting handler")
        p = REGISTER[n]
        print("Found handler ", n)
        return a10.structures.returncode.ReturnCode(a10.structures.constants.SUCCESS, p)
    except KeyError as err:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.UNREGISTEREDPROTOCOL,
            "Unregistered Protocol " + (str(err)),
        )
    except Exception as err:
        return a10.structures.returncode.ReturnCode(
            a10.structures.constants.GENERALERROR, "General error " + (str(err))
        )
