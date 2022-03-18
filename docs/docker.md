# Running with Docker

These instructions tell how to build the core components as docker containers

## Pre-requisites

In these examples we run a10rest, u10 and nut10 as docker containers on a host *already* running MongoDB and Mosquitto


## Configuration File

Set up a10.conf in a suitable place. Both a10rest and u10 expect it to be in `/etc/a10.conf`

## Building and Running a10rest

NB: This applies to all docker build, but it might be required to add `--network=host` to the build commands to allow pip3 to run correctly. No idea, just copied and pasted it from Stackexchange

```bash
~/AttestationEngine$ docker build -t a10rest -f a10rest/Dockerfile.local .
~/AttestationEngine$ docker run -v /etc/a10.conf:/etc/a10.conf:ro -p 8520:8520 --network="host" a10rest
```

And to test:

```bash
ian@ubuntu:~$ curl -X GET http://127.0.0.1:8520/v2/
{
  "msg": "Hello from A10REST v2"
}
```


## Building and Running u10

```bash
~/AttestationEngine$ docker build -t u10 -f u10/Dockerfile.local .
~/AttestationEngine$ docker run -v /etc/a10.conf:/etc/a10.conf:ro -p 8540:8540 --network="host" u10
```

## Building and Running Enrollment Server

This is an optional part but useful. It comes in two pieces, the server - which communications with A10REST and the client which runs *interactively* on some device.

```bash
cd apps/enroller/server/
docker build -t enrolserver -f Dockerfile.local .
cd apps/enroller/client/
docker build -t enrolclientprovision -f Dockerfile.provision .
docker build -t enrolclientupdateelement -f Dockerfile.updateelement .
```

To run the server:

```bash
docker run -p 8521:8521 --network "host" -e TPM2TOOLS_TCTI='mssim:host=localhost,port=2321' enrolserver http://127.0.0.1:8520
```

To run the client, on a machine TO BE PROVISIONED OR UPDATED:

```bash
docker run -it --device=/dev/tpm0 enrolclientprovision
docker run -it --device=/dev/tpm0 enrolclientupdateelement
```


## Building and Running nut10

The `Dockerfile.local` is set up for x86 machines and includes TPM2TOOLS, Intel TXT Tools, Linux IMA and assumes the UEFI eventlog is present.

NB: because we use Ubuntu as a base, some Ubuntu versions ask for tzdata configuration, see here: https://anonoz.github.io/tech/2020/04/24/docker-build-stuck-tzdata.html  Seems that 18.04 works fine, and at the time of writing 20.04 did not.

```bash
~/AttestationEngine$ docker build -t nut10 -f t10/nut10/Dockerfile.local .
```

If you wish to communicate with a local TPM use the `--device` option, or, set the TPM2TOOLS_TCTI variable as in the two example below:

```bash
~/AttestationEngine$ docker run -p 8530:8530 --network "host" --device=/dev/tpm0 nut10
~/AttestationEngine$ docker run -p 8530:8530 --network "host" -e TPM2TOOLS_TCTI='mssim:host=localhost,port=2321' nut10
```

If you are using UEFI eventlog, TXT and/or IMA then the necessary files and devices will additionally be required by docker. NB: UEFI and IMA have specific files, while TXT requires access to `/dev/mem`. Additionally note if using TXT then the UEFI eventlog will not be available to due how grub/tboot (does not) call UEFI ExitBootServices before the kernel is started.

```bash
docker run -p 8530:8530 -v /sys/kernel/secuity/tpm0/binary_bios_measurements:/sys/kernel/secuity/tpm0/binary_bios_measurements:ro -v /sys/kernel/security/ima/ascii_runtime_measurements:/sys/kernel/security/ima/ascii_runtime_measurements:ro --device=/dev/mem --network "host" --device=/dev/tpm0 nut10
```