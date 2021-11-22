# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import secrets

# import json
from flask import Blueprint, render_template

# , flash, redirect, request
from ping3 import ping, verbose_ping
from urllib.parse import urlparse

import a10.asvr.elements

# import a10.structures.constants
# import a10.structures.identity
# import a10.structures.timestamps
# import a10.asvr.db.announce

from . import formatting


ping_blueprint = Blueprint(
    "ping", __name__, static_folder="../static", template_folder="../templates/"
)

secret = secrets.token_urlsafe(64)
ping_blueprint.secret_key = secret


@ping_blueprint.route("/ping/all", methods=["GET"])
def getping():
    es = a10.asvr.elements.getElementsFull()

    pingresults = []

    l = len(es)
    c = 1

    for e in es:
        print(c, "of", l, " - ", e["name"], e["endpoint"])
        c = c + 1
        u = urlparse(e["endpoint"])

        res = "Unknown Result"

        try:
            pr = ping(u.hostname)
            if pr == None:
                res = "Timeout/No Reply"
            elif pr == False:
                res = "Host unknown"
            else:
                res = str(pr) + "ms"
        except:
            res = "Malformed Address"

        p = {"name": e["name"], "ip": u.hostname, "itemid": e["itemid"], "result": res}

        pingresults.append(p)

    print("Finished pinging for the fjords. I have", len(pingresults), "results")
    print(pingresults)

    return render_template("ping.html", prs=pingresults)
