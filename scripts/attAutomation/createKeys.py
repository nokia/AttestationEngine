#!/usr/bin/python3
import argparse
import ga10pythonlib as ga10
#arparse for creating keys

keyPars = argparse.ArgumentParser()

keyPars.add_argument("device", help = "<username>@<ip address>")

keyArguments = keyPars.parse_args()



ga10.createKeys(keyArguments.device)