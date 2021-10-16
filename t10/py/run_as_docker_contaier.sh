#!/bin/sh
# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

docker run -p 8530:8530 --device=/dev/tpm0 t10
