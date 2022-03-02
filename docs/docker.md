# Running with Docker

These instructions tell how to build the components as docker containers

## Pre-requisites

In these examples we run a10rest, u10 and nut10 as docker containers on a host *already* running MongoDB and Mosquitto


## Configuration File

Set up a10.conf in a suitable place. Both a10rest and u10 expect it to be in `/etc/a10.conf`

## Building and Running a10rest

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