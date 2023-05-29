# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause Clear License.
# SPDX-License-Identifier: BSD-3-Clear

#
# This file is read by all modules for configuraion information
# The configuration file in /etc/a10.conf be edited after installation on a site specific basis
# A default configuration file is included in this directory for reference
#

# print("READING D10 CONFIGURAITON FILE")

import configparser

config = configparser.ConfigParser()
CONFIGURATIONFILE = "/etc/d10.conf"


try:
    config.read(CONFIGURATIONFILE)

    DEBUG = config["Debugging"]["debug"]
    MONGODBURL = config["mongo"]["mongodburl"]
    MONGODBNAME = config["mongo"]["mongodbname"]

except Exception as e:
    print("D10 configuration file error ", e, " while reading ", CONFIGURATIONFILE)
    print("Exiting.")
    exit(1)


if DEBUG == "on":
    print("Configuration")
    print("   +-- configuration file: ", CONFIGURATIONFILE)


def getConfiguration():
    return {
        "configurationfile": CONFIGURATIONFILE,
        "debug": DEBUG,     
        "mongodburl": MONGODBURL,
        "mongodbname": MONGODBNAME,
    }
