# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

from dictdiffer import diff

#
# Very simply this just calculates the difference between the two dictionaries
# What is not done is checking the types of the structures coming in
#

def pcrdiff(p1,p2):
    #
    # p1 and p2 are dictionaries of PCR values
    #
    # Returns the dictdiffer difference structure.
    #

    d = diff(p1,p2)

    return d

def quotediff(q1,q2):
    #
    # q1 and q2 are dictionaries of the quote TPMS_ATTEST structure
    #
    # Returns the dictdiffer difference structure.
    #    

    d = diff(q1,q2)

    return d