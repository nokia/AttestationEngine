import argparse
import ast

from flask import Flask, render_template, request
from forensicsDocument import *

import requests

import fcrules
import fcanalytics

from pcrDifferences import PCRDifference  
from systeminfoDifferences import SystemInformationDifference  
from quoteDifferences import *
from elementDifferences import *

app = Flask(__name__)


def getListOfASVRs():
    l = [  ("DockerCompose Installatíon","http://a10rest:8520/v2","Use this when using docker compose"), 
           ("Localhost","http://127.0.0.1:8520/v2","Use this for testing locally only"), 
           ("IoT K4 (Ext)","http://194.157.71.11:8520/v2","External access to IoT K4 - requires port forwarding to work"), 
           ("IoT K4 (Int)","http://192.168.11.79:8520/v2","Internal access to IoT K4 - use on local server only"),            
           ("Ian VM","http://192.168.71.128:8520/v2","Private"),
           ("Covid server","http://10.144.176.154:8520/v2","Requires VPN. Has nothing to do with 5G :)")     
    ]
    return l


@app.route("/", methods=["GET"])
def home():
    return render_template("home.html",  asvrlist=getListOfASVRs())



@app.route("/listelements", methods=["GET"])
def listelements():
    asvr=request.args.get('asvr')
    arch=request.args.get('archived')

    print("asvr=",asvr,"archived",arch)
    
    if arch==None:
        es=requests.get( request.args.get('asvr')+"/elements/summary" )
        arch=0
    else:
        es=requests.get( request.args.get('asvr')+"/elements/summary?archived=1" )
        arch=1

    print("elements=",es.status_code )
    print("elements=",es.json() )

    return render_template("listelements.html",  es = es.json(), asvr=asvr, arch=arch)



@app.route("/mfd", methods=["GET"])
def mfd():
    eid=request.args.get('eid')    
    asvr=request.args.get('asvr')
    alist=request.args.get('alist')

    try:
        claimslimit=request.args.get('limit')
    except:
        claimslimit=250

    print("the asvr address ",asvr,",eid=",eid,"claimslimit=",claimslimit)

    try:
        f = ForensicsDocument(asvr, eid, limit=claimslimit)
    except ForensicsException as e:
        return render_template("errorpage.html",errmsg=str(e))
    except Exception as e: 
        return render_template("errorpage.html",errmsg=str(e)+" NB: This error is from a proper bug.")

    #print("alist = ",alist)
    if alist==None:    
        fcrules.applyAllAnalysisRules(f)
    else:
        aactuallist = ast.literal_eval(alist)
        #print(type(aactuallist),len(aactuallist))
        fcrules.applySelectedAnalysisRules(f,aactuallist)



    d =  f.getDocument()

    return render_template("ftl.html",  element = d["element"], 
                                        timeline = d["timeline"], metadata = d["metadata"], 
                                        analysisfunctions=d["analysisfunctions"], errors=d["errors"],
                                        analytics=fcanalytics.generateAnalytics(d))


#
# Code to run everything here
#

def main_debug(cert, key, config_filename="fc.conf"):
    app.config.from_pyfile(config_filename)
    if cert and key:
        print("running in secure mode")        
        app.run(
            debug=app.config["FLASKDEBUG"],
            threaded=app.config["FLASKTHREADED"],
            host=app.config["DEFAULTHOST"],
            port=app.config["DEFAULTPORT"],
            ssl_context=(cert, key),
        )
    else:
        print("running in insecure mode")        
        app.run(
            debug=app.config["FLASKDEBUG"],
            threaded=app.config["FLASKTHREADED"],
            host=app.config["DEFAULTHOST"],
            port=app.config["DEFAULTPORT"],
        )

def main_production(cert, key, config_filename="fc.conf", t=16):
   from waitress import serve
   app.config.from_pyfile(config_filename)
   serve(app, host=app.config["DEFAULTHOST"], port=app.config["DEFAULTPORT"], threads=t)


#
# Main and process arguments
#


ap = argparse.ArgumentParser(description='Forensics Server')
ap.add_argument('-p', '--production', help="Run the Forensics Server server in production mode (Waitress instread of Flask Debug)",  action='store_true')
args = ap.parse_args()


if __name__ == "__main__":
    print("Forensics Server Starting")
    if args.production==True:
        print("Running in Production Mode")
        main_production("", "")
    else:
        print("Running in Debug Mode")        
        main_debug("","")


