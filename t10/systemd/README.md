# Installation

Instructions for getting the ta installed and running with systemd

## A home for the trust agent

First create a suitable home for these files, eg `/opt/ta` and set it to a suitable user, eg: tss, xxx, pi, whatever...

```bash
sudo mkdir /opt/ta
sudo chown xxx /opt/ta
```

## TA

We assume you are using the reference nut10 trust agent. Copy this to the suitable place above. You'll find nut10 in AttestationEngine/t10/nut10

You can copy the whole directory or the contents recursively as you see fit. Just make sure you get the paths correct for systemd.

## SYSTEMD


Copy ta.start and ta.stop to /opt/ta and then place `ta.service` in /etc/systemd/system and made executable by all

```bash
chmod 644 ta.service
```

Edit ta.service, ta.start and ta.stop to ensure that the correct files are being pointed to. Also ensure that ta.service Wants and After are commented out if NOT using tpm2_abrmd resource manager.

In ta.service ExecStart and ExecStop must point to where ta.start and ta.stop are located, for example this might be (using the above example)

```txt
ExecStart=/opt/ta/ta.start
ExecStop=/opt/ta/ta.stop
```

Then edit ta.start and ta.stop so that these point to the correct trust agent to start. This is set in a variable in ta.start. For example

```txt
TAPATH=/opt/ta/nut10/ta.py
```

NB: ta.stop has nothing of important in it - just for future reference. 


To start, restart and stop the ta use the following commands respectively

```bash
sudo systemd start ta.service
sudo systemd restart ta.service
sudo systemd stop ta.service

```

Check for errors using `journalctl -xe` and `sudo systemctl status ta.service` if necessary.

To enable on next boot

```bash
sudo systemd enable ta.service
```

