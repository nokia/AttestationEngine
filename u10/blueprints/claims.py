# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import secrets
import json
import base64
import subprocess
import tempfile
import yaml

from flask import Blueprint, render_template, flash, redirect

import a10.structures.constants
import a10.structures.identity
import a10.asvr.claims

from . import formatting

claims_blueprint = Blueprint(
    "claims", __name__, static_folder="../static", template_folder="../templates/"
)

secret = secrets.token_urlsafe(64)
claims_blueprint.secret_key = secret


@claims_blueprint.route("/claims", methods=["GET"])
def claims():
    cs = a10.asvr.claims.getClaimsFull(500)

    for c in cs:
        c["receivedUTC"] = formatting.futc(c["header"]["as_received"])
        c["requestedUTC"] = formatting.futc(c["header"]["as_requested"])
        c["timedifference"] = round(
            float(c["header"]["as_received"]) - float(c["header"]["as_requested"]), 4
        )

    return render_template("claims.html", claims=cs)


@claims_blueprint.route("/claim/<item_id>", methods=["GET"])
def claim(item_id):
    c = a10.asvr.claims.getClaim(item_id).msg()
    pp = json.dumps(c, sort_keys=True, indent=4)

    #print("PP",pp)

    c["receivedUTC"] = formatting.futc(c["header"]["as_received"])
    c["requestedUTC"] = formatting.futc(c["header"]["as_requested"])
    c["timedifference"] = round(
        float(c["header"]["as_received"]) - float(c["header"]["as_requested"]), 4
    )

    rs = a10.asvr.claims.getAssociatedResults(item_id)
    for r in rs:
        r["difference"] = round(
            float(r["verifiedAt"]) - float(c["header"]["as_received"]), 4
        )
        r["verifiedAtUTC"] = formatting.futc(r["verifiedAt"])

    return render_template("claim.html", cla=c, pp=pp, rs=rs)
    
    
@claims_blueprint.route("/claim/prettyprint/pcrs/<item_id>", methods=["GET"])
def claimprettyprintPCRs(item_id):
    c = a10.asvr.claims.getClaim(item_id).msg()
 

    if c.get("payload").get("payload").get("pcrs")==None:
       return render_template("claimprettyprint/incorrecttype.html", cla=c, msg="Claim does not appear to be PCR values")        
    else:
       pcrs=c.get("payload").get("payload").get("pcrs")
       
       #pcrs_sha1 = sorted(pcrs.get("sha1").items())
       #pcrs_sha1 = pcrs.get("sha1")
       #print("sha1=",pcrs_sha1)
       
       pcrlist = {}
       
       for p in ['sha1','sha256','sha384','sha512']:
          if pcrs.get(p)!=None:
            print(" CLAIM - getting ",p)
            print(" PCRS ",type(pcrs.get(p)))
            #OK, it turns out that if pcrs.get(p) is NOT a dict then it is probably empty
            #eg: Lenovo T440, reports sha1 and sha256, but no PCRs in the sha256 bank
            if isinstance(pcrs.get(p),dict):
                ps = sorted( {int(k) : v for k, v in pcrs.get(p).items()}.items() )
                pcrlist[p]=ps
       
     
       #get the pcr schema, we check that
       #claim.header.element.tpm2.tpm0.pcrschema exists
       #this returns None if no schema is included
       pcrschema=None
       try:
            pcrschema=c.get("header").get("element").get("tpm2").get("tpm0").get("pcrschema")
       except:
            pcrschema=="-"
          

       return render_template("claimprettyprint/pcrs.html", cla=c,pcrlist=pcrlist,pcrschema=pcrschema)  
    
    





@claims_blueprint.route("/claim/prettyprint/quote/<item_id>", methods=["GET"])
def claimprettyprintQuote(item_id):
    c = a10.asvr.claims.getClaim(item_id).msg()

        
    if c.get("payload").get("payload").get("quote")==None:
       return render_template("claimprettyprint/incorrecttype.html", cla=c, msg="Claim does not appear to be a quote")        
    else:
       return render_template("claimprettyprint/quote.html", cla=c)  


@claims_blueprint.route("/claim/prettyprint/sysinfo/<item_id>", methods=["GET"])
def claimprettyprintSysinfo(item_id):
    c = a10.asvr.claims.getClaim(item_id).msg()

        
    if c.get("payload").get("payload").get("systeminfo")==None:
       return render_template("claimprettyprint/incorrecttype.html", cla=c, msg="Claim does not appear to be a system info structure")        
    else:
       return render_template("claimprettyprint/systeminfo.html", cla=c)  

    
     
@claims_blueprint.route("/claim/prettyprint/uefieventlog/<item_id>", methods=["GET"])
def claimprettyprintUEFIEventLog(item_id):
    c = a10.asvr.claims.getClaim(item_id).msg()

    # claim body contains a base85 encoded claim
    if c.get("payload").get("payload").get("encoding")!="base85/utf-8":
       return render_template("claimprettyprint/incorrecttype.html", cla=c, msg="UEFI Eventlog must be in base/utf-8 encoding")        
    
    if c.get("payload").get("payload").get("eventlog")==None:
       return render_template("claimprettyprint/incorrecttype.html", cla=c, msg="Claim does not appear to be a UEFI Eventlog")        


    #
    # Decode
    #

    undecoded_eventlog = c.get("payload").get("payload").get("eventlog")
    decoded_eventlog = base64.b85decode(undecoded_eventlog)

    #
    # Write to temporary file
    #

    tf = tempfile.NamedTemporaryFile()
    tf.write(decoded_eventlog)
    tf.seek(0)

    #
    # Process file with tpm2_eventlog
    #
    # This command generates yaml which we load and generate a python dict
    #

    cmd = "tpm2_eventlog " + tf.name
    tpm2_eventlog_yaml = subprocess.check_output(cmd.split())
    tpm2_eventlog_dict = yaml.load(tpm2_eventlog_yaml, Loader=yaml.BaseLoader)
    evdec1len = len(tpm2_eventlog_dict['events'])

    # NB: if we do more processing then we must call tf.seek(0) to reset the file pointer
    # maybe here goes a call to the IBM tpm tools eventlog reader from Ken

    #
    # Close file
    #

    tf.close()

    #
    # Render page
    #

    return render_template("claimprettyprint/uefieventlog.html", cla=c, evdec1= tpm2_eventlog_dict, evdec1len=evdec1len)        

   
@claims_blueprint.route("/claim/prettyprint/imalog/<item_id>", methods=["GET"])
def claimprettyprintIMAlog(item_id):
    c = a10.asvr.claims.getClaim(item_id).msg()

    imalog_raw = c.get("payload").get("payload").get("imalog")
        
    if imalog_raw==None:
       return render_template("claimprettyprint/incorrecttype.html", cla=c, msg="Claim does not appear to be an IMA log")        

    #
    # The ima log is a space delimited file
    # PCR Template-Hash Policy Filedata-Hash Filename-hint Filename-Signature (if the latter is present)
    #  Policies: ima-ng, ima and ima-sig (the latter includes the signature)
    #

    lines = imalog_raw.split("\n")

    imalog=[]

    for l in lines:
        logentry = l.split(" ")
        print("le=",logentry)
        if len(logentry)==5:               #ima-ng and ima
            imalog.append( { "pcr":logentry[0],
                 "thash":logentry[1],
                 "pol":logentry[2],
                 "fhash":logentry[3],
                 "nhint":logentry[4],
                 "fsig":"" }) 
        elif len(logentry)==6:            #ima-sig
             imalog.append( { "pcr":logentry[0],
                 "thash":logentry[1],
                 "pol":logentry[2],
                 "fhash":logentry[3],
                 "nhint":logentry[4],
                 "fsig":logentry[5] } )      
        else:                           #something else, maybe a PCR read ?
             imalog.append( { "pcr":"",
                 "thash":"",
                 "pol":"?",
                 "fhash":"",
                 "nhint":"",
                 "fsig":"" })                

    print("LINES ",len(lines),lines)

    #
    # Render the page
    #

    return render_template("claimprettyprint/imalog.html", cla=c, imalog=imalog, es=len(imalog))  