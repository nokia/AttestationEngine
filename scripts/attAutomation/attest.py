#!/usr/bin/python3
import ga10pythonlib as ga10
import argparse


attestPars = argparse.ArgumentParser()
#add name of the device
attestPars.add_argument("-e", "--element", type=str,
                    help="element name")
attestPars.add_argument("-params", "--parameters", type= str, help="parameters name")

#add description, this is optional, if this is not added the description will be generated automatically
attestPars.add_argument("-p", "--policy", type= str, help="policy name")

#tags such as tpm type, currently only supports one argument which is the tpm type
attestPars.add_argument("-s", "--session", type = str, help="session id")

attestPars.add_argument("-sE", "--server", type = str, help= "attestation server; <ip address>:<port>")
attestArguments = attestPars.parse_args()


    
ga10.attest(attestArguments.policy,attestArguments.server,attestArguments.element,attestArguments.session,attestArguments.parameters)
