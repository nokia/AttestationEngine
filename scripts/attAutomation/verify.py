#!/usr/bin/python3
import ga10pythonlib as ga10
import argparse


#argparse for create verify function
verifyPars = argparse.ArgumentParser()


verifyPars.add_argument("-r", "--rule", type = str, help="rule name")

verifyPars.add_argument("-s", "--session", type = str, help="session id")

verifyPars.add_argument("-c", "--claim", type = str, help = "claim id")

verifyPars.add_argument("-sE", "--server", type = str, help= "attestation server; <ip address>:<port>")

verifyArguments = verifyPars.parse_args()


results = ga10.verify(verifyArguments.rule,verifyArguments.session,verifyArguments.claim,verifyArguments.server)

print(results['result'])
