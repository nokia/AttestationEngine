# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import secrets

# import json
from flask import Blueprint, render_template
import a10.asvr.elements



qrcodes_blueprint = Blueprint(
    "qrcodes", __name__, static_folder="../static", template_folder="../templates/"
)

secret = secrets.token_urlsafe(64)
qrcodes_blueprint.secret_key = secret


@qrcodes_blueprint.route("/qrcodes", methods=["GET"])
def qrcodes():
    es = a10.asvr.elements.getElementsFull()
    essummary = []

    for e in es:
       print("\n",e['itemid'])
       essummary.append(  { "itemid":e['itemid'], 
                            "name":e['name'],
                            "endpoint":e["endpoint"] 

        }   )

    return render_template("informationpages/qrcodes.html", es=essummary)
