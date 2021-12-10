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
       return render_template("claimprettyprint/incorrecttype.html", cla=c, msg="Claim does not appear to be a UEFI Eventlog")        
    else:
       pcrs=c.get("payload").get("payload").get("pcrs")
       
       #pcrs_sha1 = sorted(pcrs.get("sha1").items())
       #pcrs_sha1 = pcrs.get("sha1")
       #print("sha1=",pcrs_sha1)
       
       pcrlist = {}
       
       for p in ['sha1','sha256','sha384','sha512']:
          if pcrs.get(p)!=None:
          	ps = sorted( {int(k) : v for k, v in pcrs.get(p).items()}.items() )
          	pcrlist[p]=ps
       
     
       
       return render_template("claimprettyprint/pcrs.html", cla=c,pcrlist=pcrlist)  
    
    
    
    
    
@claims_blueprint.route("/claim/prettyprint/quote/<item_id>", methods=["GET"])
def claimprettyprintQuote(item_id):
    c = a10.asvr.claims.getClaim(item_id).msg()

    	
    if c.get("payload").get("payload").get("quote")==None:
       return render_template("claimprettyprint/incorrecttype.html", cla=c, msg="Claim does not appear to be a UEFI Eventlog")        
    else:
       return render_template("claimprettyprint/quote.html", cla=c)  
    
    
    
    
    
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
def claimprettyprintQuote(item_id):
    c = a10.asvr.claims.getClaim(item_id).msg()

        
    if c.get("payload").get("payload").get("imalog")==None:
       return render_template("claimprettyprint/incorrecttype.html", cla=c, msg="Claim does not appear to be an IMA log")        
    else:
       return render_template("claimprettyprint/imalog.html", cla=c)  