# U10 - Attestation Engine UI

This is a basic UI for exploring and interacting with the attestation engine.

This guide will show you how to run a STANDALONE, BARE O/S a10rest instance. Refer to the root directory INSTALL.md for docker instructions

## Installation
Ensure that the a10 libraries have been installed. If a pypi server is available then the following commands will work. If you are running your own server then refer to the second example which contains the necessary parameters for ths

```bash
pip3 install a10
```

This is the full form for a local, untrusted pypi repository with a proxy thrown in for good measure too. Edit as required.

```bash
pip3 install --index-url x.x.x.x/simple a10 -v --trusted-host x.x.x.x  --proxy=y.y.y.y
```

## Prerequisites

A mosquitto broker and mongo database must be running as referred to in the a10.conf file.


## Configuration

There are two configuration files to look at

### u10.conf

This is used by python flask and resides in the same directory as u10.py. It contains settings for flask and which interfaces u10 will listen too (default: 0.0.0.0) and which port (default: 8540).

```
# Configuration file for the Attestation Server

# Port on which to run the attestation server (default: 8540)
DEFAULTPORT = 8540
# Host on which to listen for the attestation server (default: 0.0.0.0)
DEFAULTHOST = "0.0.0.0"
# Debug mode for the flask app used for development to see errors and stack traces (default: True)
FLASKDEBUG = True
# Run in threaded mode to respond to more than one request at once (default: True)
FLASKTHREADED = True
```

### a10.conf

U10 requires a file `/etc/a10.conf` - this file is hard coded in `a10/db/configuration.py` - don't change that unless you really know what you are doing.

An example a10.conf is provided in the u10 directory - this should be edited and placed in /etc.

## Running

To run simply execute u10:

```bash
python3 u10.py

```

Then point your browser at 127.0.0.1:8540 or to whichever machine's IP address you are using.

## Stopping

Press CTRL-C twice. The first to stop u10 and the second to stop any threads such as the MQTT keep alive ping. 
