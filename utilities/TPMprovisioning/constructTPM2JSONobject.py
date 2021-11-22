#!/usr/bin/python3
# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause


import sys
import yaml
import json

print("Number of arguments:", len(sys.argv), "arguments.")
print("Argument List:", str(sys.argv))
print("\n\n")

name = sys.argv[1]
description = sys.argv[2]
hostname = sys.argv[3]
protocol = sys.argv[4]
ip = sys.argv[5]
port = sys.argv[6]
asurl = sys.argv[7]
ekdata = sys.argv[8]
akdata = sys.argv[9]
ekpem = sys.argv[10]
akpem = sys.argv[11]


fekpem = open(ekpem, "r")
fakpem = open(akpem, "r")

ekpublic = fekpem.read()
akpublic = fakpem.read()

fekpem.close()
fakpem.close()


fek = open(ekdata, "r")
fak = open(akdata, "r")

# Seems like CLoader is not found in yaml...at least on the Pis
en = yaml.load(fek.read(), Loader=yaml.Loader)
an = yaml.load(fak.read(), Loader=yaml.Loader)

fek.close()
fak.close()


ek_qname = en["qualified name"]
ak_qname = an["qualified name"]

# print(ek_qname,ak_qname)

j = {
    "type": ["tpm2.0"],
    "name": name,
    "description": description,
    "hostname": hostname,
    "endpoint": "http://" + ip + ":" + port,
    "protocol": protocol,
    "asurl": [asurl],
    "ek_pem": ekpublic,
    "ak_pem": akpublic,
    "ek_name": ek_qname,
    "ak_name": ak_qname,
}

# print(j)
print("\n")

print(json.dumps(j, indent=3, sort_keys=True))
