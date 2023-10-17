#!/usr/bin/python3
import ga10pythonlib as ga10
import argparse

#creates an element
#argparse for create element function
elementPars = argparse.ArgumentParser()
#add name of the device
elementPars.add_argument("-n", "--name", type=str,
                    help="element name")

#add description, this is optional, if this is not added the description will be generated automatically
elementPars.add_argument("-d", "--description", type= str, help="description of element")

#add ip address, this is mandatory
elementPars.add_argument("device", help= "<user>@<ip address>")

#tags such as tpm type, currently only supports one argument which is the tpm type
elementPars.add_argument("-t", "--tag", type = str, help="tag")

elementPars.add_argument("-sE", "--server", type=str, help= "attestation server; <ip address>:<port>")
elementArguments = elementPars.parse_args()


ga10.createElement(elementArguments.name,elementArguments.description,elementArguments.device,elementArguments.tag,elementArguments.server)