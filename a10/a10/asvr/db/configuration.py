# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

#
# This file is read by all modules for configuraion information
# The configuration file in /etc/a10.conf be edited after installation on a site specific basis
# A default configuration file is included in this directory for reference
#

# print("READING ASVR CONFIGURAITON FILE")

import configparser

import a10.structures.identity

ASSESSIONIDENTITY = a10.structures.identity.generateID()

config = configparser.ConfigParser()
CONFIGURATIONFILE = "/etc/a10.conf"


try:
    config.read(CONFIGURATIONFILE)

    ASVRNAME = config["Identification"]["asvrname"]
    DEBUG = config["Debugging"]["debug"]

    LOGFILE = config["Logging"]["logfile"]

    MQTTCLIENTNAME = config["mqtt"]["mqttclientname"]
    MQTTADDRESS = config["mqtt"]["mqttaddress"]
    MQTTPORT = config["mqtt"]["mqttport"]
    MQTTKEEPALIVEPING = config["mqtt"]["keepaliveping"]

    MONGODBURL = config["mongo"]["mongodburl"]
    MONGODBNAME = config["mongo"]["mongodbname"]

except Exception as e:
    print("A10 configuration file error ", e, " while reading ", CONFIGURATIONFILE)
    print("Exiting.")
    exit(1)


if DEBUG == "on":
    print("Configuration")
    print("   +-- configuration file: ", CONFIGURATIONFILE)


def getConfiguration():
    return {
        "AS_Session_Identity": ASSESSIONIDENTITY,
        "configurationfile": CONFIGURATIONFILE,
        "asvrname": ASVRNAME,
        "debug": DEBUG,
        "logfile": LOGFILE,
        "mqttclientname": MQTTCLIENTNAME,
        "mqttaddress": MQTTADDRESS,
        "mqttport": MQTTPORT,
        "mqttkeepaliveping": MQTTKEEPALIVEPING,
        "mongodburl": MONGODBURL,
        "mongodbname": MONGODBNAME,
    }
