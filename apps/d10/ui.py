
from flask import Flask, request, send_from_directory, jsonify, Blueprint, render_template
import secrets


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
    return render_template("home.html")
