# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import os
import secrets
from flask import Blueprint, send_from_directory, render_template

import a10.structures.constants
import a10.structures.identity

import a10.asvr.db.core
import a10.asvr.db.configuration

home_blueprint = Blueprint(
    "home", __name__, static_folder="../static", template_folder="../templates/"
)

secret = secrets.token_urlsafe(64)
home_blueprint.secret_key = secret

release = "2021.9"
codename = "The Pink Dirndl Of Doom"


@home_blueprint.route("/favicon.ico")
def favicon():
    return send_from_directory(
        home_blueprint.static_folder, "favicon.ico", mimetype="image/vnd.microsoft.icon"
    )


@home_blueprint.route("/")
def hello():
    dbstatus = a10.asvr.db.core.getDatabaseStatus()
    constatus = a10.asvr.db.configuration.getConfiguration()
    return render_template(
        "home/home.html",
        d={"dbstatus": dbstatus, "configuration": constatus},
        release=release,
    )


@home_blueprint.route("/help")
def help():
    return render_template("home/help.html")


@home_blueprint.route("/about")
def about():
    return render_template("home/about.html", release=release, codename=codename)
