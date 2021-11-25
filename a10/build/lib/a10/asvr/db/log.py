# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import logging
import a10.asvr.db.configuration


logging.basicConfig(
    filename=a10.asvr.db.configuration.LOGFILE,
    level=logging.DEBUG,
    format="%(asctime)s %(message)s",
    datefmt="%m/%d/%Y %I:%M:%S %p",
)


def writelog(t, ch, op, data):
    payload = t + "," + ch + "," + op + "," + str(data)
    logging.info(payload)
