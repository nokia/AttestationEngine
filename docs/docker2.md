# Running with Docker 2

Read the section on building the components first, then move to this which describes everything in turn.



# Building
NB: This applies to all docker build, but it might be required to add `--network=host` to the build commands to allow pip3 to run correctly. No idea, just copied and pasted it from Stackexchange

Start from the root directory of the NAE installation.

```bash
docker build -t a10rest -f a10rest/Dockerfile.local .
docker build -t u10 -f u10/Dockerfile.local .
docker build -t nut10 -f t10/nut10/Dockerfile.local .
cd apps/enroller/client
docker build -t updateelement -f Dockerfile.updateelement .
docker build -t provision -f Dockerfile.provision .
cd ../server
docker build -t enrolserver -f Dockerfile.local .
```

# Running
To run use the following - with local modifications if necessary. Docker usually requires sudo.

Ensure mongodb and mosquitto are available and `/etc/a10.conf` has been configured accordingly. 

In the examples below we have included the option `--network="host"` this may or might not be necessary depending upon the usage of these containers. Typically it is required when trying to access local resources, eg: the monogo and mosquitto components running "bare metal".


## Core + Enrollment Server

To run the core + enrollment server if using a local TPM (for the enrolment server)

```bash
docker run -v /etc/a10.conf:/etc/a10.conf:ro -p 8540:8540 --network="host" u10
docker run -v /etc/a10.conf:/etc/a10.conf:ro -p 8520:8520 --network="host" a10rest
docker run -p 8521:8521 --network "host" --device=/dev/tpm0 enrolserver http://127.0.0.1:8520
```

Or if using, eg: a SWTPM 

```bash
docker run -v /etc/a10.conf:/etc/a10.conf:ro -p 8540:8540 --network="host" u10
docker run -v /etc/a10.conf:/etc/a10.conf:ro -p 8520:8520 --network="host" a10rest
docker run -p 8521:8521 --network "host" -e TPM2TOOLS_TCTI='mssim:host=localhost,port=2321' enrolserver http://127.0.0.1:8520
```


## Trust Agent

To run a trust agent on each node that requires it

```bash
docker run -p 8530:8530 --network "host" --device=/dev/tpm0 nut10
```

or

```bash
docker run -p 8530:8530 --network "host" -e TPM2TOOLS_TCTI='mssim:host=localhost,port=2321' nut10
```

## Provisioning/updating nodes

To provision or update use the interactive mode and the required container respectively. For example to access the local TPM

```bash
docker run -it --device=/dev/tpm0 enrolclientprovision
docker run -it --device=/dev/tpm0 enrolclientupdateelement
```

or a SWTPM:

```bash
docker run -it --device=/dev/tpm0 -e TPM2TOOLS_TCTI='mssim:host=localhost,port=2321' enrolclientprovision
docker run -it --device=/dev/tpm0 -e TPM2TOOLS_TCTI='mssim:host=localhost,port=2321' enrolclientupdateelement
```