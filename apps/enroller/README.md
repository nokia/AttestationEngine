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



# Alternative

The software can be run stand alone, at least in the sense where the provision programme can run and the resulting JSON copied to U10's interface. 

The `TPM2TOOLS_TCTI` environment variable can also be set - useful for working with devices that do not have TPM2 tools installed...first set up the `TPM2TOOLS_TCTI` variable to use ssh and select the correct private key

```bash

$ export TPM2TOOLS_TCTI="cmd:ssh -i /home/ian/keys/homepis pi@192.168.1.126 tpm2_send"
$ tpm2_getrandom 16
����Y\`ݶ5Ȉ��$ 
```

Then run the `provisioning` script - MAKING SURE THAT IT IS USING THE VARIABLE ABOVE. A full session looks like this:

Some notes:

   1. male sure to change the hostname (as the localhost is picked up, not the remote)
   2. make sure to change the IP address for the same reason as above
   3. protocol must be A10TPMSENDSSL

Handles 0x810100ee an 0x810100aa are the defaults. Otherwise most items are largely irrelevant and just select the default.

STOP once the JSON record is shown.


```bash
ian@hitadebian:~/AttestationEngine/apps/enroller/client$ ./provision 
Provisioning Script: JSON Element Description Generation
TCTI is (if set): cmd:ssh -i /home/ian/keys/homepis pi@192.168.1.126 tpm2_send
Setting up the basic element identification details
Short  Name [hitadebian]: cholera
Longer Description [to lazy to write this]: Littlepi
Location [60.22309,24.75877]: 
Types (comma separated) [x86,tpm2,uefi,arm,pi]: 
This machine's hostname [hitadebian]: cholera
This machine's IP address [192.168.1.32 172.20.0.1 172.17.0.1]: 192.168.1.126
Protocol [A10HTTPREST]: A10TPMSENDSSL
TA Port [8530]: 
Attestation server URLs (comma separated if more than one) [http://10.144.176.154:8510]: 
Setting up keys - the EK will be removed after these operations
Generation will fail if keys already exist at the given handles
Enter EK handle location [0x810100ee]: 
Enter AK handle location [0x810100aa]: 
If the UEFI eventlog is available accept the default (or modify the location)
If this is not a UEFI machine then delete the default answer and leave blank
Enter UEFI eventlog location [/sys/kernel/security/tpm0/binary_bios_measurements]: 

This is what I have
cholera
Littlepi
60.22309,24.75877
x86,tpm2,uefi,arm,pi
cholera
A10TPMSENDSSL 192.168.1.126 8530
http://10.144.176.154:8510
0x810100ee
0x810100aa
/sys/kernel/security/tpm0/binary_bios_measurements
Does everything look good?  Ctrl-C exits, Enter continues
Continuing with setup
Removing any existing keys from 0x810100ee and 0x810100aa
WARNING:esys:src/tss2-esys/api/Esys_ReadPublic.c:320:Esys_ReadPublic_Finish() Received TPM Error 
ERROR:esys:src/tss2-esys/esys_tr.c:231:Esys_TR_FromTPMPublic_Finish() Error ReadPublic ErrorCode (0x0000018b) 
ERROR:esys:src/tss2-esys/esys_tr.c:321:Esys_TR_FromTPMPublic() Error TR FromTPMPublic ErrorCode (0x0000018b) 
ERROR: Esys_TR_FromTPMPublic(0x18B) - tpm:handle(1):the handle is not correct for the use
ERROR:esys:src/tss2-esys/esys_tr.c:357:Esys_TR_Close() Error: Esys handle does not exist (70018). 
ERROR: Esys_TR_Close(0x70018) - esapi:The ESYS_TR resource object is bad
ERROR: Unable to run tpm2_evictcontrol
WARNING:esys:src/tss2-esys/api/Esys_ReadPublic.c:320:Esys_ReadPublic_Finish() Received TPM Error 
ERROR:esys:src/tss2-esys/esys_tr.c:231:Esys_TR_FromTPMPublic_Finish() Error ReadPublic ErrorCode (0x0000018b) 
ERROR:esys:src/tss2-esys/esys_tr.c:321:Esys_TR_FromTPMPublic() Error TR FromTPMPublic ErrorCode (0x0000018b) 
ERROR: Esys_TR_FromTPMPublic(0x18B) - tpm:handle(1):the handle is not correct for the use
ERROR:esys:src/tss2-esys/esys_tr.c:357:Esys_TR_Close() Error: Esys handle does not exist (70018). 
ERROR: Esys_TR_Close(0x70018) - esapi:The ESYS_TR resource object is bad
ERROR: Unable to run tpm2_evictcontrol
Clearing temporary objects from TPM
Persistent handles - should be empty:
Transient handles - should be empty:
Session handles - should be empty:
TPM Cleared?  Ctrl-C exits, Enter continues
Generating temporary directory
/tmp/provision.uUeH99Kaq8zg
Generating EK
Generating AK
Generating PEM and data files
Listing temporary files in  /tmp/provision.uUeH99Kaq8zg
total 24
-rw-rw---- 1 ian ian 1253 Nov 26 14:33 ak.ctx
-rw-rw---- 1 ian ian  451 Nov 26 14:33 ak.pem
-rw-rw---- 1 ian ian  282 Nov 26 14:33 ak.pub
-rw-r--r-- 1 ian ian 1041 Nov 26 14:33 ak.yaml
-rw-rw---- 1 ian ian  451 Nov 26 14:33 ek.pem
-rw-r--r-- 1 ian ian 1130 Nov 26 14:33 ek.yaml
Clearing temporary objects from TPM
Persistent handles - should contain AK and EK:
- 0x810100AA
- 0x810100EE
Transient handles - should be empty:
Session handles - should (might) be empty:
TPM Check Things #2?  Ctrl-C exits, Enter continues
Constructing JSON object
JSON Element Description Constructor Utility
Number of arguments: 17 arguments.
Argument List: ['constructTPM2JSONobject.py', 'cholera', 'Littlepi', '60.22309,24.75877', 'x86,tpm2,uefi,arm,pi', 'cholera', 'A10TPMSENDSSL', '192.168.1.126', '8530', 'http://10.144.176.154:8510', '0x810100ee', '0x810100aa', '/sys/kernel/security/tpm0/binary_bios_measurements', '/tmp/provision.uUeH99Kaq8zg/ak.pem', '/tmp/provision.uUeH99Kaq8zg/ak.yaml', '/tmp/provision.uUeH99Kaq8zg/ek.pem', '/tmp/provision.uUeH99Kaq8zg/ek.yaml']



Writing to enrol.json
Done
This is the start of the object*********************
{
   "asurl": [
      "http://10.144.176.154:8510"
   ],
   "description": "Littlepi",
   "endpoint": "http://192.168.1.126:8530",
   "hostname": "cholera",
   "location": "60.22309,24.75877",
   "name": "cholera",
   "protocol": "A10TPMSENDSSL",
   "tpm2": {
      "tpm0": {
         "akhandle": "0x810100aa",
         "akname": "000b0053aae8152e027c5495da17db8d8591390ba10562bb6f8e06dc062f67f2788b",
         "akpem": "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtzbemumWFzoaYeT6Q8qm\n/7UCyc3V79z9tyb4ueVCWWOlqkXWhbNR/lcIbZHKj3S0cJtnjSPw2caaBzyuW798\n6Bv+tvn6EXV5x6e1pp0LcY1JxX9KY+D5uw/T8ZI72/et0uVmZgOTgj06lb/5wUZI\nWTOoZ3G0m08ucpU43DCP0zHK5sykjnvucE/99LAaQreDfGG0rt9IiuuHWuJ0T6tJ\ngRuAmjl9+acmCBgD1NwfhXUUVm9hens9FIi2HsAMVxu+HAb1BtefM2Gpm8dyMypC\nNeyCR7dqk2AfOA0JbE8+v+4Nn8ReWUzj7clbP8UF0GovZjgB405rVhEVyNT6ekbJ\n4wIDAQAB\n-----END PUBLIC KEY-----\n",
         "ekhandle": "0x810100ee",
         "ekname": "000bd0e5a08adb2fcdc0e9f08d7ba01ede747cd558652f679c76e235fc1c328294e4",
         "ekpem": "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAs2Ge+ekF1olAXGpu9kkz\nwZdLTDNoXZcT1ZSomSRqUOM15nxcgOWok0IVmeeNzYjClM7VdyxaU99KRhG+0Azn\n5x5+9jbtbIRKvdQAZbVhPnsvIcAY3+X/BZL6pah961dM4dspJJBT1DcqseWUztxo\n7D2gs6SjuCbyHfPSB8guq9XfDRon9zfHFfejVCToHUbEOSom+s3ClZ/3AoQ4B8D/\n/N1yYBuM/47itAsVOwdZ2BzE56a/QJGRsI4861pgM/+kcyb1SNsrf9cgFPCVM0XY\n84j4a7YnLXcQ9j54gM6i34Uj6omo1XJN9jVd8Jc0r3OCuzr70A237j2RG3sRwm0Z\n+wIDAQAB\n-----END PUBLIC KEY-----\n"
      }
   },
   "type": [
      "x86,tpm2,uefi,arm,pi"
   ],
   "uefi": {
      "eventlog": "/sys/kernel/security/tpm0/binary_bios_measurements"
   }
}This is the end of the object*********************
Does everything look good?  Ctrl-C exits, Enter continues
```

Press Ctrl-C to exit the script.

Copy the JSON record and paste it into a new element in U10.

   1. you might need to modify the "type" section as appropriate. The provisioning script generates a list of a single string, not a list of multiple individual strings.  
   2. Delete anything not required, eg: uefi section on a raspberry pi for example.
   3. Make sure it is still valid JSON before hitting submit!

```json
{
   "asurl": [
      "http://10.144.176.154:8510"
   ],
   "description": "Littlepi",
   "endpoint": "http://192.168.1.126:8530",
   "hostname": "cholera",
   "location": "60.22309,24.75877",
   "name": "cholera",
   "protocol": "A10TPMSENDSSL",
   "tpm2": {
      "tpm0": {
         "akhandle": "0x810100aa",
         "akname": "000b0053aae8152e027c5495da17db8d8591390ba10562bb6f8e06dc062f67f2788b",
         "akpem": "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtzbemumWFzoaYeT6Q8qm\n/7UCyc3V79z9tyb4ueVCWWOlqkXWhbNR/lcIbZHKj3S0cJtnjSPw2caaBzyuW798\n6Bv+tvn6EXV5x6e1pp0LcY1JxX9KY+D5uw/T8ZI72/et0uVmZgOTgj06lb/5wUZI\nWTOoZ3G0m08ucpU43DCP0zHK5sykjnvucE/99LAaQreDfGG0rt9IiuuHWuJ0T6tJ\ngRuAmjl9+acmCBgD1NwfhXUUVm9hens9FIi2HsAMVxu+HAb1BtefM2Gpm8dyMypC\nNeyCR7dqk2AfOA0JbE8+v+4Nn8ReWUzj7clbP8UF0GovZjgB405rVhEVyNT6ekbJ\n4wIDAQAB\n-----END PUBLIC KEY-----\n",
         "ekhandle": "0x810100ee",
         "ekname": "000bd0e5a08adb2fcdc0e9f08d7ba01ede747cd558652f679c76e235fc1c328294e4",
         "ekpem": "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAs2Ge+ekF1olAXGpu9kkz\nwZdLTDNoXZcT1ZSomSRqUOM15nxcgOWok0IVmeeNzYjClM7VdyxaU99KRhG+0Azn\n5x5+9jbtbIRKvdQAZbVhPnsvIcAY3+X/BZL6pah961dM4dspJJBT1DcqseWUztxo\n7D2gs6SjuCbyHfPSB8guq9XfDRon9zfHFfejVCToHUbEOSom+s3ClZ/3AoQ4B8D/\n/N1yYBuM/47itAsVOwdZ2BzE56a/QJGRsI4861pgM/+kcyb1SNsrf9cgFPCVM0XY\n84j4a7YnLXcQ9j54gM6i34Uj6omo1XJN9jVd8Jc0r3OCuzr70A237j2RG3sRwm0Z\n+wIDAQAB\n-----END PUBLIC KEY-----\n"
      }
   },
   "type": [
      "tpm2","arm","pi"
   ]
}
```

Now add a section to provide information on where the SSH keys are stored. This section is shown below, edit the key name, and username as appropriate. Add this into the element description at the top level.

```json
    "a10_tpm_send_ssl": {
        "key": "/var/attestation/homepis",
        "timeout": 20,
        "username": "pi"
    }
```

