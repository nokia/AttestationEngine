#!/bin/sh
# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause


#
# This script sets up the ssl verify and proxy for accessing Nokia gitlabe2
# Some newer distributions do not supply the CA root keys and git has a hard time with SSL connnections
#
# Also, proxies....
#
# ...not, ponies
#

git config --global http.sslVerify "false"
git config --global http.proxy http://10.144.1.10:8080

