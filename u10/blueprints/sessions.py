# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import secrets
import json
from flask import Blueprint, render_template, flash, redirect, request

import a10.structures.constants
import a10.structures.identity

import a10.asvr.sessions
import a10.asvr.results
import a10.asvr.policies
import a10.asvr.types

from . import formatting

sessions_blueprint = Blueprint(
    "sessions", __name__, static_folder="../static", template_folder="../templates/"
)

secret = secrets.token_urlsafe(64)
sessions_blueprint.secret_key = secret


@sessions_blueprint.route("/sessions", methods=["GET"])
def sessions():
 

    os = a10.asvr.sessions.getOpenSessions()
    cs = a10.asvr.sessions.getClosedSessions()

    sessions=[]

    for s in os:
        ses = a10.asvr.sessions.getSession(s["itemid"]).msg()
        print(ses["opened"]," ", formatting.futc(ses["opened"]))
        ses["openedUTC"] = formatting.futc(ses["opened"])
        ses["numclaims"] = len(ses["claims"])
        ses["numresults"] = len(ses["results"])
        ses["numsessions"] = len(ses["sessions"])        
        sessions.append(ses)

    for s in cs:
        ses = a10.asvr.sessions.getSession(s["itemid"]).msg()
        ses["openedUTC"] = formatting.futc(ses["opened"])
        ses["closedUTC"] = formatting.futc(ses["closed"])
        dur = float(ses["closed"]) - float(ses["opened"])
        ses["duration"] = f'{dur:.4f}' 
        ses["numclaims"] = len(ses["claims"])
        ses["numresults"] = len(ses["results"])
        ses["numsessions"] = len(ses["sessions"])

        sessions.append(ses)

    sessions_sorted = sorted(sessions, key=lambda i: (i["opened"]),reverse=True)


    return render_template("sessions.html", sessions=sessions_sorted)
   


@sessions_blueprint.route("/session/<itemid>", methods=["GET"])
def session(itemid):
    s= a10.asvr.sessions.getSession(itemid)  

    ses=s.msg()

    ses["openedUTC"] = formatting.futc(ses["opened"])
    ses["closedUTC"] = formatting.futc(ses["closed"])
    dur = float(ses["closed"]) - float(ses["opened"])
    ses["duration"] = f'{dur:.4f}' 
    ses["numclaims"] = len(ses["claims"])
    ses["numresults"] = len(ses["results"])
    ses["numsessions"] = len(ses["sessions"])
    
    return render_template("session.html", ses=ses) 