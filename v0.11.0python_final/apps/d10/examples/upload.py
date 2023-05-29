import requests
import os
import json

print("This script uploads the contents of this direcetory to the D10 REST API, assumed to be on 127.0.0.1:8544")

for f in os.listdir():
    if f.endswith(".att"):
        print("Uploading att ",f)
        o = open(f,"r")
        n = f[:-len(".att")]
        j = {"name":n,"att":o.read()}
        r = requests.post("http://127.0.0.1:8544/rest/att",json=j)
        print("    --- ",r)

for f in os.listdir():
    if f.endswith(".eva"):
        print("Uploading eva ",f)
        o = open(f,"r")
        n = f[:-len(".eva")]
        j = {"name":n,"eva":o.read()}
        r = requests.post("http://127.0.0.1:8544/rest/eva",json=j)
        print("    --- ",r)        