#!/bin/sh
# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

#Script to quickly generate the EK and AK into the two handles that the TA likes

tpm2_createek -c 0x810100ee -G rsa -u /tmp/ek.pub
tpm2_createak -C 0x810100ee -c /tmp/ak.ctx -G rsa -g sha256 -s rsassa -u /tmp/ak.pub -f pem -n /tmp/ak.name
tpm2_evictcontrol -c /tmp/ak.ctx 0x810100aa
tpm2_getcap handles-persistent