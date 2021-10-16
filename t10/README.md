# Trust Agent

This is the code for the *reference* trust agent. It basically supports EVERY API I can think of; inlcuding things that are not supported on some devices, eg: UEFI on ARM.


## What's here

   * In the `py` directory is the Python3 POC TA.
   * In the `go` directory is the GoLang POC TA.
   * In the `provisioning` directory are various provisioning scripts, eg: tpm2 elements, notpm elements etc
   * In the `systemd` directory are the templates for the systemd services and the start and stop scripts
   
 
## Installation

Easier method is to download the code , eg: git pull, and then do this - it installed the python version. I am assuming you are in the t10 directory.

Before starting this, it might be easier to manually create `/opt/ta` and change its owner to someone who can easily write, eg: the current user.  Some comments require sudo.

Also, edit ta.serivce and check the requiremnts on the Wants sections for abrmd machines and PiFakeBoot machines.

```bash
systemctl stop ta.service
mkdir /opt/ta
cp -r py/* /opt/ta
cp systemd/ta.service /etc/systemd/system
cp systemd/ta.start /opt/ta
cp systemd/ta.stop /opt/ta
chmod 644 /opt/ta/ta.start
chmod 644 /opt/ta/ta.stop
systemd daemon-reload
sudo systemd enable ta.service
systemd start ta.service
```
 
 
   
## A Note on Raspberry PIs

Good to know, the TA basically starts in more or less the same way. However, the Let's Trust TPMs can perform a proper reset as if they were power cycled using the GPIO pins, where as the Infineon TPMs require someone to press the reset button on the device (unless you rewire the big round gold wire).

Actually the above probably should be in the Fake measured boot for the Pis
