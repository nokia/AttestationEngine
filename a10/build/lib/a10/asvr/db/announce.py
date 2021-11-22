# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import threading
import time

import a10.structures.timestamps

import a10.asvr.db.log
import a10.asvr.db.mqtt
import a10.asvr.db.core

import a10.asvr.db.configuration


def announceItemManagement(op, data):
    t = a10.structures.timestamps.now()
    a10.asvr.db.log.writelog(t, "IM", op, data)
    a10.asvr.db.core.writeLogEntry(t, "IM", op, data)
    a10.asvr.db.mqtt.publish("AS/IM", t, op, data)


def announceClaim(op, data):
    t = a10.structures.timestamps.now()
    a10.asvr.db.log.writelog(t, "C", op, data)
    a10.asvr.db.core.writeLogEntry(t, "C", op, data)
    a10.asvr.db.mqtt.publish("AS/C", t, op, data)


def announceResult(op, data):
    t = a10.structures.timestamps.now()
    a10.asvr.db.log.writelog(t, "R", op, data)
    a10.asvr.db.core.writeLogEntry(t, "R", op, data)
    a10.asvr.db.mqtt.publish("AS/R", t, op, data)


def getLatestLogEntries(n=250):
    """ Returns the latest log entries 

	:params int n: number of entries to return, defaults to 250
	:returns: list of log entries
	:rtype: list dict or None
	"""

    return a10.asvr.db.core.getLatestLogEntries(n)


def getLogEntryCount():
    """ Returns the number of log entries

	:returns: number of log entries
	:rtype: int
	"""
    return a10.asvr.db.core.getLogEntryCount()
