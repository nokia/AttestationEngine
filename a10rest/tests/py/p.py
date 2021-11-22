# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import requests
import threading


def test():
    r = requests.get("http://192.168.1.82:8520/")
    print(r, r.text)

    # e={'itemid':'bebc3af9-5050-45ee-9b54-656a68de56ed'}
    # r = requests.get('http://192.168.1.82:8520/element',params=e)
    # print(r,r.text)

    e = {
        "eid": "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "pid": "fc34db2d-2a26-4eda-b1d4-3064821d2e27",
        "aps": {},
    }
    r = requests.put("http://192.168.1.82:8520/attest", json=e)
    c = r.text
    print(r, c)

    e = {"cid": c, "rule": ("tpm2rules.TPM2QuoteStandardVerify", {})}
    r = requests.put("http://192.168.1.82:8520/verify", json=e)
    c = r.text
    print(r, c)


def attest_and_verify(eid, pid, aps, rule):
    cp = {"eid": eid, "pid": pid, "aps": aps}
    r = requests.put("http://192.168.1.82:8520/attest", json=cp)
    c = r.text
    rp = {"cid": c, "rule": rule}
    r = requests.put("http://192.168.1.82:8520/verify", json=rp)
    v = r.text
    return v


print("SEQUENTIAL TEST")

db = [
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "fc34db2d-2a26-4eda-b1d4-3064821d2e27",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "df3b50a5-1ea4-41b1-b949-d92a65dd454c",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
]


for e in db:
    print(attest_and_verify(e[0], e[1], e[2], e[3]))


x = input("?")

print("PARALLEL TEST")


tdb = [
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "fc34db2d-2a26-4eda-b1d4-3064821d2e27",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "df3b50a5-1ea4-41b1-b949-d92a65dd454c",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "fc34db2d-2a26-4eda-b1d4-3064821d2e27",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "df3b50a5-1ea4-41b1-b949-d92a65dd454c",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "fc34db2d-2a26-4eda-b1d4-3064821d2e27",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "df3b50a5-1ea4-41b1-b949-d92a65dd454c",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "fc34db2d-2a26-4eda-b1d4-3064821d2e27",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "df3b50a5-1ea4-41b1-b949-d92a65dd454c",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "fc34db2d-2a26-4eda-b1d4-3064821d2e27",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "df3b50a5-1ea4-41b1-b949-d92a65dd454c",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "fc34db2d-2a26-4eda-b1d4-3064821d2e27",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "df3b50a5-1ea4-41b1-b949-d92a65dd454c",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "fc34db2d-2a26-4eda-b1d4-3064821d2e27",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "df3b50a5-1ea4-41b1-b949-d92a65dd454c",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "fc34db2d-2a26-4eda-b1d4-3064821d2e27",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "df3b50a5-1ea4-41b1-b949-d92a65dd454c",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "fc34db2d-2a26-4eda-b1d4-3064821d2e27",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "df3b50a5-1ea4-41b1-b949-d92a65dd454c",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "fc34db2d-2a26-4eda-b1d4-3064821d2e27",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "df3b50a5-1ea4-41b1-b949-d92a65dd454c",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "fc34db2d-2a26-4eda-b1d4-3064821d2e27",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "df3b50a5-1ea4-41b1-b949-d92a65dd454c",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "fc34db2d-2a26-4eda-b1d4-3064821d2e27",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "df3b50a5-1ea4-41b1-b949-d92a65dd454c",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "fc34db2d-2a26-4eda-b1d4-3064821d2e27",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "df3b50a5-1ea4-41b1-b949-d92a65dd454c",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "fc34db2d-2a26-4eda-b1d4-3064821d2e27",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "df3b50a5-1ea4-41b1-b949-d92a65dd454c",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "fc34db2d-2a26-4eda-b1d4-3064821d2e27",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "df3b50a5-1ea4-41b1-b949-d92a65dd454c",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "fc34db2d-2a26-4eda-b1d4-3064821d2e27",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "df3b50a5-1ea4-41b1-b949-d92a65dd454c",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "fc34db2d-2a26-4eda-b1d4-3064821d2e27",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "df3b50a5-1ea4-41b1-b949-d92a65dd454c",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "fc34db2d-2a26-4eda-b1d4-3064821d2e27",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
    (
        "bebc3af9-5050-45ee-9b54-656a68de56ed",
        "df3b50a5-1ea4-41b1-b949-d92a65dd454c",
        {},
        ("tpm2rules.TPM2QuoteStandardVerify", {}),
    ),
]

for e in tdb:
    print(e)
    t = threading.Thread(target=attest_and_verify, args=(e[0], e[1], e[2], e[3],))
    t.start()

print("PARALLEL TEST COMPLETE")
