#!/usr/bin/python3
import ga10pythonlib as ga10
import argparse
#arparse for create session function

sessionPars = argparse.ArgumentParser()

#add ip address, this is mandatory
sessionPars.add_argument("ip", help= "<ipaddress>:<port>")

sessionPars.add_argument("-m", "--message", type = str, help = "message displayed with session creation")


sessionArguments = sessionPars.parse_args()

   
ga10.createSession(sessionArguments.ip,sessionArguments.message)