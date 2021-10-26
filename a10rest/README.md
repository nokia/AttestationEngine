# A10REST - Attestation Engine REST API

This is a basic API for interacting with the attestation engine.

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

### Swagger

The python3 package flask-swagger is used to generate a JSON file that can be used by Swagger.

```bash
pip3 install flask-swagger
```

The document is exposed on the `/spec` endpoint when running.

## Prerequisites

A mosquitto broker and mongo database must be running as referred to in the a10.conf file.

## Configuration

There are two configuration files to look at

### a10rest.conf

This is used by python flask and resides in the same directory as u10.py. It contains settings for flask and which interfaces u10 will listen too (default: 0.0.0.0) and which port (default: 8540).

```
# Configuration file for the Attestation Server REST API

# Port on which to run the attestation server (default: 8520)
DEFAULTPORT = 8520
# Host on which to listen for the attestation server (default: 0.0.0.0)
DEFAULTHOST = "0.0.0.0"
# Debug mode for the flask app used for development to see errors and stack traces (default: True)
FLASKDEBUG = True
# Run in threaded mode to respond to more than one request at once (default: True)
FLASKTHREADED = True

```

### a10.conf

A10REST requires a file `/etc/a10.conf` - this file is hard coded in `a10/db/configuration.py` - don't change that unless you really know what you are doing.

An example a10.conf is provided in the u10 directory - this should be edited and placed in /etc.

## Running

To run simply execute u10:

```bash
python3 a10rest.py

```

Then point your browser at 127.0.0.1:8520 or to whichever machine's IP address you are using and you well get a simple `Hello form A10REST` block of text back.

To get something useful back use `curl`, for example the commands to get all elements and a specific element are written as below. See the API documentation for the full REST API.

```bash
$ curl -X GET 127.0.0.1:8520/elements

['6bc17cb0-ed0b-48d0-a0e7-f38e24668e60', '1b427527-5f9a-4704-b59f-ccfb6ccb29cb']

$ curl -X GET 127.0.0.1:8520/element/1b427527-5f9a-4704-b59f-ccfb6ccb29cb

{
  "ak_name": "000b72d05d70613d3b1cb71f728acf6dff27ead85549c26a1e63434386bc7233147e", 
  "ak_pem": "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqh...AQAB\n-----END PUBLIC KEY-----\n", 
  "asurl": [
    "http://127.0.0.1:8510"
  ], 
  "description": "This is the home Ubuntu VM running on Vmware Player. Accessible only at home!", 
  "ek_name": "000b7cfa2d59278480ce0e5551dc1f29d0119b3689607d5a81f9e031735b2602852b", 
  "ek_pem": "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkq...IDAQAB\n-----END PUBLIC KEY-----\n", 
  "endpoint": "http://127.0.0.1:8530", 
  "itemid": "1b427527-5f9a-4704-b59f-ccfb6ccb29cb", 
  "name": "Home Ubuntu VM", 
  "protocol": "A10HTTPREST", 
  "type": [
    "tpm2.0", 
    "tpmsim", 
    "hita"
  ]
}

```


## Stopping

Press CTRL-C twice. The first to stop a10rest and the second to stop any threads such as the MQTT keep alive ping. 
