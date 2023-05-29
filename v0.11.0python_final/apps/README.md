# Attestation Applications

## WARNING

NOTE:  These MUST be written against a10rest, ie: the REST interface.

DO NOT UNDER ANYCIRUMSTANCES WRITE THESE AGAINST THE A10 LIBRARIES OTHERWISE EVERY TIME YOU START ONE OF THESE  YOU WILL CREATE A THREAD THAT WILL PING THE MQTT BROKER AND BECAUSE PYTHON IS A PITA YOU WILL NOT SEE THE ERROR MESSAGES AND YOU WILL BE DEBUGGING BLIND

## The Applications

   * ASMQTTVIEWER
   * Enroller
   * MobileAttestater


### ASMQTTViewer
A simple app that just prints out what it finds broadcast on the MQTT Channels. Can be used as a template for more advanced functionality, eg: listening to AS/R and sending alerts to somewhere.

### Enroller
A client and server for enrolling devices. The client runs on a device with a TPM and the server runs as an app somewhere and communicates with an A10REST endpoint. The client can call the server to request enrollment (after generating information about the device and provisioning the TPM), the server will then create credentials and secret and challenge the client to prove themselves (kind of like Roman Gladiators but with a TPM). If successful then the client is added to the list of elements in the attestation engine.

Not meant to be production ready but as an enrolment demonstrator.

###Mobile Attestor
An android app for talking to attestation engines

