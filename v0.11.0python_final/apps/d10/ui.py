
from flask import Flask, request, send_from_directory, jsonify, Blueprint, render_template
import secrets
import d10db

home_blueprint = Blueprint(
    "home", __name__, static_folder="../static", template_folder="../templates/"
)

secret = secrets.token_urlsafe(64)
home_blueprint.secret_key = secret


@home_blueprint.route("/favicon.ico")
def favicon():
    return send_from_directory(
        home_blueprint.static_folder, "favicon.ico", mimetype="image/vnd.microsoft.icon"
    )


@home_blueprint.route("/")
def hello():
    dbstatus = d10db.getDatabaseStatus()
    return render_template("home.html", dbstatus=dbstatus)

@home_blueprint.route("/list")
def list():
    atts = d10db.getatts()
    evas = d10db.getevas()

    return render_template("list.html", atts=atts, evas=evas)