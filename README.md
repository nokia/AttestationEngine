# A10 - Nokia Attestation Engine

This is the source for the Nokia Attestation Engine A10.

This software is used as the remote attestation engine as part of a trusted computing environment. This is the system that holds the known good values about devices and other elements, and provides the attestation and validation mechanisms.

The software here is provided as-is - there is no security (http for the win!) and the error checking in places is completely missing. The point of this was to explore more interesting mechanisms for remote attestation and to implement ideas from the IEFT RATS specification.

## Contents

Each directory contains a local README.md file with more information

   * ga10 - The main server-side engine.
   * ta10 - A reference trust agent for /dev/tpm* devices
   * dist - contains useful files, eg: systemd examples etc.
   * v0.11.0 - the older python3 based NAE.
  
## Getting it running QUICKLY

Ensure that Go is installed and correctly configured

Write a configuration file and ensure that it is available to ga10 in some suitable directory. A config file example is below.

To start the server

   cd ga10
   go get -u
   go run . -config=/somewhere/config.yaml

To start the TA

   cd ta10
   go get -u
   go run .

The TA requires access to /dev/tpm* devices (eg: /dev/tpmrm0), IMA log file, TXT log file and the UEFI event log. Either use sudo or setup permissions on these files.

## Security

A self-signed key is provided called temporary.key/crt - DO NOT USE THIS IN PRODUCTION OR ANYWHERE. Browsers will complain if you use this.
THIS IS NOT SECURE!!!

PUTTING PRIVATE KEYS ON GITHUB FOR ANYTHING ELSE THAN A DEMONSTRATION IS CRAZY. DO NOT DO THIS.

TO SAVE YOURSELF, SET THE use http FIELDS TO true.  

GENERATE YOUR OWN KEYS AND KEEP THEM SECURE.

## More security and evidence

A10 signs claims, results, sessions etc...the keys are randomly generated each him the system is started and aren't recorded anywhere. There is some code to talk PKCS#11 and has been tested with a YubiHSM but it isn't used. Don't rely upon it, I haven't tested it and it is just placeholder at this time. Yes, I'll get arund to writing the proper functionality real soon now...volunteers?

## Example Config File 

Note the lines with "CHANGE ME"

   * The name of the system can set to anything you want.
   * The MQTT client ID must be unique if you indent running more than one instance

See the note on security above

```yaml
#Some general naming
system:
  name: ASVR_GO_1

#MongoDB Configuration
database: 
  connection: mongodb://192.168.1.203:27017    "CHANGE ME"
  name: test1                                  "CHANGE ME"

#MQTT Configuration
messaging:
  broker: 192.168.1.203                        "CHANGE ME"
  port: 1883                                   "CHANGE ME"
  clientid: asvrgo1                            "CHANGE ME"

#REST Interface Configuration
rest:
  port: 8520
  crt: temporary.crt                     
  key: temporary.key
  usehttp: true

#Web Interface Configuration
web:
  port: 8540
  crt: temporary.crt
  key: temporary.key
  usehttp: true

#Log file
logging:
  logfilelocation: /tmp/ga10.log
  sessionupdatelogging: false
```


# Use in a Production Environment
Don't.  This is not secure and many points where errors and exceptions should be captured are not implemented.
