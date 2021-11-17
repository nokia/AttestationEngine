#!/usr/bin/python3
# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause


import sys
import yaml
import json

print("JSON Element Description Constructor Utility")
print("Number of arguments:", len(sys.argv), "arguments.")
print("Argument List:", str(sys.argv))
print("\n\n")

name = sys.argv[1]
description = sys.argv[2]
location = sys.argv[3]
types = sys.argv[4]

hostname = sys.argv[5]
protocol = sys.argv[6]
ip = sys.argv[7]
port = sys.argv[8]
asurl = sys.argv[9]
ekhandle = sys.argv[10]
akhandle = sys.argv[11]
uefieventlog = sys.argv[12]

akpem = open(sys.argv[13], "r")
akyaml = open(sys.argv[14], "r")
ekpem = open(sys.argv[15], "r")
ekyaml = open(sys.argv[16], "r")

akpemdata = akpem.read()
akyamldata = akyaml.read()

ekpemdata = ekpem.read()
ekyamldata = ekyaml.read()


an = yaml.load(akyamldata, Loader=yaml.Loader)
en = yaml.load(ekyamldata, Loader=yaml.Loader)


akpem.close()
akyaml.close()
ekpem.close()
ekyaml.close()


j = {
    "type": [types],
    "name": name,
    "description": description,
    "location": location,
    "hostname": hostname,
    "endpoint": "http://" + ip + ":" + port,
    "protocol": protocol,
    "asurl": [asurl],
    "tpm2": {
        "tpm0": {
            "akhandle": akhandle,
            "akname": an["name"],
            "akpem": akpemdata,
            "ekhandle": ekhandle,
            "ekname": en["name"],
            "ekpem": ekpemdata,
        }
    },
    "uefi": {"eventlog": uefieventlog},
}

# print(j)
print("Writing to enrol.json")
enr = open("enrol.json", "w")
enr.write(json.dumps(j, indent=3, sort_keys=True))
enr.close()

print("Done")
