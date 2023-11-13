#!/usr/bin/python3
import ga10pythonlib as ga10
import argparse
closeSessionPars = argparse.ArgumentParser()

#add ip address, this is mandatory
closeSessionPars.add_argument("ip", help= "<ipaddress>:<port>")

closeSessionPars.add_argument("-s","--session", type=str, help = "session id")

closeSessionArguments = closeSessionPars.parse_args()


ga10.closeSession(closeSessionArguments.ip,closeSessionArguments.session)
   

