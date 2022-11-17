#!/usr/bin/python3

from pprint import pprint
from forensicsDocument import *


from pcrDifferences import PCRDifference  
from systeminfoDifferences import SystemInformationDifference  
from quoteDifferences import *
from elementDifferences import *

import argparse
import sys
import json

#
# setup the arguments parser
#

ap = argparse.ArgumentParser(description='Generate forensics data for a given element.')
ap.add_argument('asvr', help="The address of the attesation REST API. NB: assumes the v2 interface")
ap.add_argument('eid', help="The element itemid")
ap.add_argument('-o','--outputfile', help="Filename to save the forensics document JSON into")
ap.add_argument('-q','--quiet', help="Quiet mode - no output to the terminal,  NB: -m and -e override this.", action='store_true')
ap.add_argument('-m','--metadata', help="Print the metadata contained in the forensics document.", action='store_true')
ap.add_argument('-l','--limit', help="Maximum number of claims to retreive. Defaults to 250.")

args = ap.parse_args()

#
# The main bit
#

# Generate Document

fd = {}

print("args",args.asvr,args.eid,args.limit)

if args.limit == None:
    claimslimit = 250
else:
    claimslimit = int(args.limit)

try:
    fd = ForensicsDocument(args.asvr, args.eid, limit = claimslimit )
    print("FD is ",fd)
except Exception as e:
    print("Element not found probably: ",e)
    sys.exit(1)

# Apply Analyses

fd.analyse(PCRDifference())
fd.analyse(QuoteResetDifference())
fd.analyse(QuoteRestartDifference())
fd.analyse(QuoteSafeDifference())
fd.analyse(QuoteFirmwareDifference())
fd.analyse(QuoteQualifiedSignerDifference())
fc.analyse(QuoteClockCheck())
fd.analyse(ElementDifference())
fd.analyse(SystemInformationDifference())


# Process Arguments


if args.quiet == False:
    pprint(fd.getDocument(), sort_dicts=False)

if args.metadata == True:
    print("\nMetadata:")
    pprint(fd.getMetadata(), sort_dicts=True)

if args.outputfile != None:
    f = open(args.outputfile,"w")
    f.write(json.dumps(fd.getDocument()))
    f.close()

