/**
 * Copyright 2021 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clear
 */

db = db.getSiblingDB('asvr')

db.claims.drop()
db.elements.drop()
db.expectedvalues.drop()
db.policies.drop()
db.results.drop()
db.hashes.drop()
db.log.drop()




