# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import datetime


def futc(t):
    # formats a timestamp to a UTC date:
    return datetime.datetime.utcfromtimestamp(float(t)).strftime("%Y-%m-%d_%H:%M:%S")
