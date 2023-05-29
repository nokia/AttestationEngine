# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

curl   --header "Content-Type: application/json" -X POST --data '{  "ak_name": "XYZ",   "ak_pem": "QPR",   "asurl": ["http://127.0.0.1:8510"],   "description": "Local Machine .. asumed to be an x86 systems",   "ek_name": "DEF",   "ek_pem": "ABC",   "endpoint": "http://127.0.0.1:8530",  "name": "Locahost curl test", "protocol":"Fred",  "type": ["tpm2.0"]}' http://127.0.0.1:8520/element

curl   -X DELETE  http://127.0.0.1:8520/element?itemid=724fc19d-d036-4171-ad31-b0f4dcb00aff



curl   --header "Content-Type: application/json" -X PUT --data '{ "itemid":"dsgfsfg-7f37-4c8d-9a50-07948e85c1d9", "ak_name": "Arbunkle",  "ek_name": "D222EF",   "ek_pem": "ABC2",   "endpoint": "http://127.0.0.1:999999",  "name": "Locahost curl test updates"}' http://127.0.0.1:8520/element
