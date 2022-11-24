#Copyright 2021 Nokia
#Licensed under the BSD 3-Clause Clear License.
#SPDX-License-Identifier: BSD-3-Clear

import d10db

def executeAttestation(att,eva,ept=None):
    #
    # att is the name of the att structure in the database
    # eva is the name of the eva structure in the database
    # ept is the rest endpoint to be used. If none specified then the default rest endpoint
    #     will be used

    # attc - get from database
    attrecord = d10db.getatt(att)
    evarecord = d10db.geteva(eva)

    #if ept==None:
    #   ept == get from defaults 
    ept = "http://127.0.0.1:8520"

    ae = attlanguage.AttestationExecutor(attrecord,evarecord,ept)
    report = ae.execute()

    return report