# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

from flask import Blueprint

status_endpoint = Blueprint("status_endpoint", __name__)


@status_endpoint.route("/", methods=["GET"])
def status_homepage():
    page = "Trust Agent Services Running"
    return page
