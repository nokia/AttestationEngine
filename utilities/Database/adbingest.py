# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import json
import sys
import requests
import pprint

args = len(sys.argv)
if args != 4:
    print("Incorrect number of arguments")
    sys.exit(1)

colls = sys.argv[1]
jfile = sys.argv[2]
a10ep = sys.argv[3]

if not (colls == "hashes" or colls == "policies"):
    print("Incorrect type: hashes or policies only")
    sys.exit(1)

print("Reading", jfile, "into", colls)

try:
    f = open(jfile, "r")
except:
    print("File", jfile, "does not exist")
    sys.exit(1)

try:
    j = json.load(f)
except Exception as e:
    print("Error in JSON ", e.string())
    sys.exit(1)

print(len(j), "items to import")

n = 0
report = []
for i in j:
    print("Progress :", (n + 1) / len(j) * 100, "%")
    if colls == "policies":
        r = requests.post(a10ep + "/policy", json=i)
    elif colls == "hashes":
        r = requests.post(a10ep + "/hash", json=i)
    else:
        print("Something has gone really badly wrong as this is unreachable code!")
        sys.exit(2)
    report.append({"n": n, "httpcode": r.status_code})
    n = n + 1
f.close()

print("Ingest complete")
pp = pprint.PrettyPrinter(indent=4)
pp.pprint(report)
