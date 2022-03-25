# Fake Measured Boot

This measures some interesting files and extends given PCRs.
It is run by systemd during the boot process and depends upon tpm2_tools being installed.

IT IS NOT A SECURE, TRUSTABLE METHOD OF MEASURING YOUR MACHINE. IT IS JUST HERE FOR DEMONSTRATION PURPOSES.

## Installation

Type `sudo ./install`

This copies everything to a directory /boot/measuredboot and sets up systemd

## "Measured" Boot

As Linux starts systemd calls the measure.start script which looks in the `measures1.d` and `measures256.d` directories. Under these are files which correspond to a given PCR number containing a list of files to be measured and their hash values extended to the appropriate banks and PCRs in the TPM.

## Log File

A log file can be found in `/var/log/measuredBootLog`

