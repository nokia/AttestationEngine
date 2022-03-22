#!/usr/bin/python3

import argparse
import attlanguage

#
# setup the arguments parser
#


ap = argparse.ArgumentParser(description='Attest Elements Command Line Utility')
ap.add_argument('template', help="Location of the template file")
ap.add_argument('elements', help="Location of the elements file")
ap.add_argument('-r', '--restendpoint', help="Address of an A10REST endpoint", default="http://127.0.0.1:8520")
ap.add_argument('-P', '--prettyprint', help="Address of an A10REST endpoint",  action='store_true')
ap.add_argument('-p', '--progress', help="Show progress, 0=none, 1=a little",  type=int, default=0)


args = ap.parse_args()

#print(args.template, args.elements, args.restendpoint, args.prettyprint, args.progress)


ae = attlanguage.AttestationExecutor(args.template,args.elements,args.restendpoint)

if args.prettyprint==True:
	ae.prettyprint()

ae.execute(progress=args.progress)