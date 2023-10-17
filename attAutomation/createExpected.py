#!/usr/bin/python3
import ga10pythonlib as ga10
import argparse

#arparse for create expected value function

expectedPars = argparse.ArgumentParser()

expectedPars.add_argument("-n", "--name", type = str, help = "expected value name")

expectedPars.add_argument("-d", "--description", type = str, help = "description of expected value")

expectedPars.add_argument("-e", "--element", type = str, help = "element used for expected value")

expectedPars.add_argument("-p", "--policy", type = str, help = "policy used for expected value")

expectedPars.add_argument("-c", "--claim", type = str, help = "claim id")

expectedPars.add_argument("-s", "--session", type = str, help = "session id")

expectedPars.add_argument("-sE", "--server", type = str, help= "attestation server; <ip address>:<port>")
expectedArguments = expectedPars.parse_args()


ga10.createExpected(expectedArguments.name,expectedArguments.description,expectedArguments.element,expectedArguments.policy,expectedArguments.session,expectedArguments.claim,expectedArguments.server)