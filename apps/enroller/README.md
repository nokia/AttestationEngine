# Example Enrolment App

This app can be used as an endpoint for device initiated enrolment. It demonstrates how information can be gathered on a device, the TPM being set up and this information communicated along with proof of the device's validity

The app comes in two parts: one runs on the device itself, eg: Pi, PC etc - at the moment they use the TPM2_TOOLS, but will port to pytss.  The second part can run anywhere but must point to the AE REST API provided by A10REST

## Prerequisites

   * tpm2_tss to be installed on both client and server
   * A client device with a TPM2.0
   * A server that can run the enrolserver
   * An attestation engine with a10rest running

   * pytss *or* tpm2_tools (and maybe tpm2_abrmd)

Two versions of the client and server scripts are provided, those starting with 't' use the tools and those starting with 'p' use the native python libraries

## Server
Ensure tpm2_tools are installed and you have a10rest running somewhere. Replace the IP address with the address of the A10rest endpoint

To start the tools version:

```sh
python3 tenrollserver.py http://192.168.1.82:8520

```

To start the python version:

```sh
python3 penrollserver.py http://192.168.1.82:8520
```


The enroller will listen on port 8521 and communicate with the A10 REST API on http://192.168.1.82:8520

## Client
Included in the client directory are all the files which should be transferred to a client device for provisioning.

   * constructTPM2JSONobject.py - a convenience file for generating the JSON element description for A10
   * enrol.py - the script that calls the enrolment server and performs the credential checking
   * exampleEnrollDocument.json - an *example* JSON element description 
   * provision - a bash script for provisioning the TPM, constructing the JSON element description and calling the enrolment


### Provisioning
To provision an element, eg: Pi, x86 machine run `./provision` which will ask a number of questions, most of which you can give the default answers to.

This script has a number of check points where you can hit Ctrl-C to stop the running.

During the running of the script it will call `constructTPM2JSONobject.py` to generate a file in the local directory called `enrol.json`

You can stop the script before it executes the `entrol.py` script

### Enrolling
To enrol, either allow the `provison` script to run or alternatively call the enrolment script explicitly. The first parameter is the IP address of the enrolment server and the second is the name of the JSON element description file, which will normally be `enrol.json` if using the scripts above.

To use the tools version:

```sh
python3 tenroll.py http://192.168.1.82:8521 enroll.json
```


To use the python version:

```sh
python3 penroll.py http://192.168.1.82:8521 enroll.json
```


A typical session might look like

```sh
pi@hitapi1:~/eclient $ !172
python3 tenroll.py http://192.168.1.82:8521 enroll.json
837197674484b3f81a90cc8d46a5d724fd52d76e06520b64f2a1da1b331469aa
certinfodata:7634614d427173346578543665737148635842414139594d6e6e596b3174
REVEALED SECRET IS  v4aMBqs4exT6esqHcXBAA9YMnnYk1t
Element with id  3f0c8c5e-5fbf-4c5a-82e8-216363617bea  created
pi@hitapi1:~/eclient $ 
```

or with the python version:

```sh
pi@hitapi1:~/eclient $ !172
python3 penroll.py http://192.168.1.82:8521 enroll.json
837197674484b3f81a90cc8d46a5d724fd52d76e06520b64f2a1da1b331469aa
certinfodata:7634614d427173346578543665737148635842414139594d6e6e596b3174
REVEALED SECRET IS  v4aMBqs4exT6esqHcXBAA9YMnnYk1t
Element with id  3f0c8c5e-5fbf-4c5a-82e8-216363617bea  created
pi@hitapi1:~/eclient $ 
```


The element id can be used with a10rest or a call to u10, eg: `http://192.168.1.82:8540/element/3f0c8c5e-5fbf-4c5a-82e8-216363617bea` (assuming u10 is running at that address).



