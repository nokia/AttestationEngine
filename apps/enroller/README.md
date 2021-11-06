# Example Enrollment App

This app can be used as an endpoint for device initiated enrollment. It demonstrates how information can be gathered on a device, the TPM being set up and this information communicated along with proof of the device's validity

The app comes in two parts: one runs on the device itself, eg: Pi, PC etc - at the moment they use the TPM2_TOOLS, but will port to pytss.  The second part can run anywhere but must point to the AE REST API provided by A10REST

## Flow
```

+-----+                                +---------+                                 +-----------+                                     +-----+
| TPM |                                | Device  |                                 | Enroller  |                                     | AE  |
+-----+                                +---------+                                 +-----------+                                     +-----+
   |                 ---------------------\ |                                            |                                              |
   |                 | Start: ./provision |-|                                            |                                              |
   |                 |--------------------| |                                            |                                              |
   |                                        |                                            |                                              |
   |                                        | gather device details (jsondesc)           |                                              |
   |                                        |---------------------------------           |                                              |
   |                                        |                                |           |                                              |
   |                                        |<--------------------------------           |                                              |
   |                                        |                                            |                                              |
   |                                        | ask user if all is good                    |                                              |
   |                                        |------------------------                    |                                              |
   |                                        |                       |                    |                                              |
   |                                        |<-----------------------                    |                                              |
   |                                        |                                            |                                              |
   |                            generate EK |                                            |                                              |
   |<---------------------------------------|                                            |                                              |
   |                                        |                                            |                                              |
   | EKpub, EKname                          |                                            |                                              |
   |--------------------------------------->|                                            |                                              |
   |                                        |                                            |                                              |
   |                            generate AK |                                            |                                              |
   |<---------------------------------------|                                            |                                              |
   |                                        |                                            |                                              |
   | AKpub, AKname                          |                                            |                                              |
   |--------------------------------------->|                                            |                                              |
   |                                        |                                            |                                              |
   |                                        | update jsondesc with EK/AKpub & name       |                                              |
   |                                        |-------------------------------------       |                                              |
   |                                        |                                    |       |                                              |
   |                                        |<------------------------------------       |                                              |
   |                                        |                                            |                                              |
   |                                        | enroll(EKpub,AKname)                       |                                              |
   |                                        |------------------------------------------->|                                              |
   |                                        |                                            |                                              |
   |                                        |                                            | gemerate secret (s)                          |
   |                                        |                                            |--------------------                          |
   |                                        |                                            |                   |                          |
   |                                        |                                            |<-------------------                          |
   |                                        |                                            |                                              |
   |                                        |                                            | makeCredential EKpub, AKname, s -> m.cred    |
   |                                        |                                            |------------------------------------------    |
   |                                        |                                            |                                         |    |
   |                                        |                                            |<-----------------------------------------    |
   |                                        |                                            |                                              |
   |                                        |                              return m.cred |                                              |
   |                                        |<-------------------------------------------|                                              |
   |                                        |                                            |                                              |
   |                                        | activateCredential -> a.cred               |                                              |
   |                                        |-----------------------------               |                                              |
   |                                        |                            |               |                                              |
   |                                        |<----------------------------               |                                              |
   |                                        |                                            |                                              |
   |                                        | send jsondesc and secret                   |                                              |
   |                                        |------------------------------------------->|                                              |
   |                                        |                                            |                                              |
   |                                        |                                            | secret == m.cred ?                           |
   |                                        |                                            |-------------------                           |
   |                                        |                                            |                  |                           |
   |                                        |                                            |<------------------                           |
   |                                        |                                            | ---------------------------\                 |
   |                                        |                                            |-| we assume all is OK here |                 |
   |                                        |                                            | |--------------------------|                 |
   |                                        |                                            |                                              |
   |                                        |                                            | addElement(jsondesc) -> itemid               |
   |                                        |                                            |--------------------------------------------->|
   |                                        |                                            |                                              |
   |                                        |                                            |                                return itemid |
   |                                        |                                            |<---------------------------------------------|
   |                                        |                                            |                                              |
   |                                        |                              return itemid |                                              |
   |                                        |<-------------------------------------------|                                              |
   |                                        |                                            |                                              |
   |                      create NVRAM area |                                            |                                              |
   |<---------------------------------------|                                            |                                              |
   |                                        |                                            |                                              |
   |      write jsondesc - pubkeys + itemid |                                            |                                              |
   |<---------------------------------------|                                            |                                              |
   |                                        |                                            |                                              |

```

                 



## Server
Ensure tpm2_tools are installed and you have a10rest running somewhere. Replace the IP address with the address of the A10rest endpoint

```sh
enroller.py http://127.0.0.1:8520

```

The enroller will listen on port 8521

## Client
To run the client type `./provision`


## Appendix

From https://textart.io/sequence

```
object TPM Device Enroller AE

note left of Device: Start: ./provision
Device->Device: gather device details (jsondesc)
Device->Device: ask user if all is good

Device->TPM: generate EK
TPM->Device: EKpub, EKname
Device->TPM: generate AK
TPM->Device: AKpub, AKname
Device->Device: update jsondesc with EK/AKpub & name


Device->Enroller: enroll(EKpub,AKname)
Enroller->Enroller: gemerate secret (s)
Enroller->Enroller: makeCredential EKpub, AKname, s -> m.cred
Enroller->Device: return m.cred

Device->Device: activateCredential -> a.cred
Device->Enroller: send jsondesc and secret

Enroller->Enroller: secret == m.cred ?
note right of Enroller:  we assume all is OK here

Enroller->AE: addElement(jsondesc) -> itemid
AE->Enroller: return itemid

Enroller->Device: return itemid

Device->TPM: create NVRAM area
Device->TPM: write jsondesc - pubkeys + itemid

```
