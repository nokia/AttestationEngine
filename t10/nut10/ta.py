#Copyright 2021 Nokia
#Licensed under the BSD 3-Clause Clear License.
#SPDX-License-Identifier: BSD-3-Clear

from flask import Flask, request, jsonify

from endpoints.tpm2_endpoint import tpm2_endpoint
from endpoints.uefi_endpoint import uefi_endpoint

import requests
import configparser
import sys
import os
import signal

VERSION = "0.3.1.nu"
ASVRS = []
ASVRS_RESP = []

USESIGNALS = "no"
if len(sys.argv)>1:
    if sys.argv[1]=="-s":
        USESIGNALS = "yes"

ta = Flask(__name__)

ta.register_blueprint(tpm2_endpoint, url_prefix="/tpm2")
ta.register_blueprint(uefi_endpoint, url_prefix="/uefi")



def listroutes():
    print("Defined Endpoints")
    for rule in ta.url_map.iter_rules():
        print(rule)


def getconfiguration(path):
    global ASVRS

    config = configparser.ConfigParser()
    
    # the config for the list of asvrs is
    # elementID , AS URL ; ... repeat

    try:
        config.read(path)
        es = config["Asvr"]["asvrs"].split(";")  # this will return a list of comma separated elementID and AE urls
        for e in es:
            a = e.split(",")
            ASVRS.append((a[0],a[1]))
    except Exception as e:
        print("T10 configuration file error ",e," write reading ",path,". Exiting.")
        exit(1)


def announce(m,msg="-"):
    global ASVRS_RESP


    ASVRS_RESP = []

    for a in ASVRS:
        eid = a[0]
        url = a[1]
        print("messaging",m,"to",url,"as",eid)
        try:
            r = requests.post(url+"/msg",json = {'msg':msg,'elementid':eid,'op':m})
            ASVRS_RESP.append( 
               { "url":url, "status":r.status_code, "response":r.text}
            )
        except Exception as e:
            ASVRS_RESP.append( 
               { "url":url, "exception":str(e) }
            )
        


@ta.route("/", methods=["GET"])
def status_homepage():
    services = [r.rule for r in ta.url_map.iter_rules()]

    rc = {
        "title": "T10 <Nu> Trust Agent",
        "version": VERSION,
        "services": str(services),
        "platform": sys.platform,
        "os": os.name,
        "pid": os.getpid(),
        "asvrs": ASVRS,
        "asvrresponses" : ASVRS_RESP,
        "usesignals" : USESIGNALS
    }

    return jsonify(rc), 200

@ta.route("/ta/reannounce", methods=["GET"])
def ta_reannounce():
    announce("ta_reannounce")
    return "reannouce", 200

def receiveSignal(signalNumber, frame):
    print('Received:', signalNumber)
    announce("ta_signal",msg=str(signalNumber))


def main(cert, key, config_filename="ta_config.cfg"):
    listroutes()
    getconfiguration("/etc/t10.conf")
    announce("ta_startup")

    ta.config.from_pyfile(config_filename)
    if cert and key:
        ta.run(
            debug=ta.config["FLASKDEBUG"],
            threaded=ta.config["FLASKTHREADED"],
            host=ta.config["DEFAULTHOST"],
            port=ta.config["DEFAULTPORT"],
            ssl_context=(cert, key),
        )
    else:
        ta.run(
            debug=ta.config["FLASKDEBUG"],
            threaded=ta.config["FLASKTHREADED"],
            host=ta.config["DEFAULTHOST"],
            port=ta.config["DEFAULTPORT"],
        )


if __name__ == "__main__":
    print("TA Starting")
    #trap certain signals, all except SIGTERM (-9) which can't be caught
    if USESIGNALS=="yes":
        signal.signal(signal.SIGHUP,receiveSignal)
        signal.signal(signal.SIGQUIT,receiveSignal)
        signal.signal(signal.SIGINT,receiveSignal)
        signal.signal(signal.SIGABRT,receiveSignal)
        signal.signal(signal.SIGALRM,receiveSignal)
        signal.signal(signal.SIGTERM,receiveSignal)
        signal.signal(signal.SIGBREAK,receiveSignal)

    #GO!
    main("", "")
    #if we can ever get here
    announce("ta_stop")

