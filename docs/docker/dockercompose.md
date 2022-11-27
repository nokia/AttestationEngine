# Running with Docker Compose

NOTE: it appears almost impossible to use ssh-agent inside docker-compose - if you have a SANE solution that allows it to run and add all the keys in the attestationelementkeys volume then please please please let me know

## Building the components

From the root directory of the attesstation server distribution we need to build the components A10REST, U10 and FC10. Firstly check that everything is there and that you are in the right place:

```bash
ian@hitadebian:~/AttestationEngine$ ls
a10rest  a10server  a10structures  apps  CONTRIBUTING.md  docs  fc10  LICENSE  README.md  t10  tests  u10  utilities
```

Then build the three components (if you require sudo for docker use these, otherwise without sudo below)

```bash
sudo docker build -t a10rest -f a10rest/Dockerfile.local .
sudo docker build -t u10 -f u10/Dockerfile.local .
sudo docker build -t fc10 -f fc10/Dockerfile.local .
```

and without sudo:

```bash
docker build -t a10rest -f a10rest/Dockerfile.local .
docker build -t u10 -f u10/Dockerfile.local .
docker build -t fc10 -f fc10/Dockerfile.local .
```

This takes a while and requires a connection to the internet in order to download the base images and required packages.

## Configuration

The directory `..../AttestationEngine/utilities/dockercomposedeployment` contains an example docker-compose setup and the files `a10.conf` and `mosquitto.conf` can be modified accordingly. Usually no changes are required.

If you wish to change the port assignments then edit the ports in `docker-compose.yml` but again this is usually not required.

## Running

To start the containers use `sudo docker-compose up` and the following should occur along with more output. For an error regarding volumes see below.

```bash
ian@hitadebian:~/AttestationEngine/utilities/dockercomposedeployment$ sudo docker-compose up
Starting mongo      ... done
Starting messagebus                           ... done
Starting dockercomposedeployment_databaseui_1 ... done
Starting a10rest                              ... done
Starting u10                                  ... done
Starting fc10                                 ... done
Attaching to mongo, messagebus, dockercomposedeployment_databaseui_1, a10rest, u10, fc10
...
```

### Volumes

THe first time docker-compose is used the system may require volumnes to be created to store persistent data. Two volumes are required, one for the mongo database and one for a location to place keys used by the attestation server, eg: ssh keys for elements with the TPMSENDSSL protocol.


If you see an error similar to this...

```bash
$ sudo docker-compose up
Creating network "dockercomposedeployment_attestationnetwork" with driver "bridge"
ERROR: Volume attestationdata declared as external, but could not be found. Please create the volume manually using `docker volume create --name=attestationdata` and try again.
```

...then run the following command (as suggested above and then re-try docker-compose)

```bash
$ sudo docker volume create --name=attestationdata
attestationdata
$ sudo docker volume create --name=attestationelementkeys
attestationelementkeys

```

The `attestationdata` volume is mounted as `/data/db` by the mongo component. The `attestationelementkeys` volume is mounted at `/var/attestation/keys` by any component that has direct access to the A10 libraries, ie: U10 and A10REST. This location is used for the storage of SSH keys and is typically referred to in the element description.

Refer to the section below on copying ssh keys to the attestationkeys volume


## Connecting

From a local machine the following should be accessible:

   * http://127.0.0.1:8520/v2 - should return a welcome message (See below for a curl example, or use a browser)
   * http://127.0.0.1:8540/v2 - should return the U10 main page
   * http://127.0.0.1:8542/v2 - should return the FC10 main page

The test for a working *A10REST* component is

```bash
$ curl http://127.0.0.1:8520/v2/
{
  "msg": "Hello from A10REST v2"
}
```

*U10*'s main page's configuration section should match the `a10.conf` file used with docker-compose.

*FC10*'s main page contains a link to then endpoint `http://a10rest:8520/v2` with the name *Docker Compose Installation*, and when accessed should return the current or achived elements as applicable


## Further Setup

After starting the system it will be necessary to load the PCR Schemas, Hashes and Policies. These are found in `....\AttestationEngine/utilities/Database`. The format and process for these is decribed in the `README.md` file in that directory. Assuming the IP address of the A10REST API is 127.0.0.1 then the following can be executed from that directory.

```bash
python3 adbingest.py hashes hashes.json http://127.0.0.1:8520
python3 adbingest.py policies policies.json http://127.0.0.1:8520
python3 adbingest.py pcrschemas pcrschemas.json http://127.0.0.1:8520

```


### Checking in on a container
You can connect to a container by creating a  bash shell

For example, find the container ID and connect a bash shell


```bash
ian@:~$ sudo docker ps
CONTAINER ID   IMAGE               COMMAND                  CREATED          STATUS          PORTS                                                                                  NAMES
02ec754ebfd8   u10                 "python3 u10.py"         16 seconds ago   Up 14 seconds   0.0.0.0:8540->8540/tcp, :::8540->8540/tcp                                              u10
f1cfcac21fef   fc10                "python3 fc.py"          21 minutes ago   Up 14 seconds   0.0.0.0:8542->8542/tcp, :::8542->8542/tcp                                              fc10
8b299168b47b   a10rest             "python3 a10rest.py"     21 minutes ago   Up 14 seconds   0.0.0.0:8520->8520/tcp, :::8520->8520/tcp                                              a10rest
de6675516ddc   mongo-express       "tini -- /docker-ent…"   9 days ago       Up 14 seconds   0.0.0.0:8555->8081/tcp, :::8555->8081/tcp                                              dockercomposedeployment_databaseui_1
10de6c735380   mongo               "docker-entrypoint.s…"   9 days ago       Up 16 seconds   27017/tcp                                                                              mongo
cb8ba5138310   eclipse-mosquitto   "/docker-entrypoint.…"   9 days ago       Up 16 seconds   0.0.0.0:8560->1883/tcp, :::8560->1883/tcp, 0.0.0.0:8561->9000/tcp, :::8561->9000/tcp   messagebus
ian@:~$ sudo docker exec -it 02ec754ebfd8 /bin/bash
root@02ec754ebfd8:/nae/u10# pwd
/nae/u10
root@02ec754ebfd8:/nae/u10# exit
exit
ian@:~$ 
```

### Attestation Keys
Attestation keys are kepy in the `attestationelementkeys` volume. To populate this volume with keys follow this procedure:

Firsly create ssh keys for the element in question. Note change a.b.c.d to the correct IP address.  I'm using a pi here wtith default username and password.

```bash
$ ssh-keygen -t rsa -f ./homepis -b 4096 -C "home pi keys"
Generating public/private rsa key pair.
Enter passphrase (empty for no passphrase): 
Enter same passphrase again: 
Your identification has been saved in ./homepis
Your public key has been saved in ./homepis.pub
The key fingerprint is:
SHA256:EydQU8NPH9v1lUfuhS+wtNT4yDEEcd9twBcTnPCQS+M home pi keys
The key's randomart image is:
+---[RSA 4096]----+
|      ..oo=oo+==*|
|       . ..+.B=OB|
|        o .oX.**@|
|         + +.Eo++|
|        S   = o o|
|         .     . |
|                 |
|                 |
|                 |
+----[SHA256]-----+
$ ls homepis*
homepis  homepis.pub
$ ssh-copy-id -i homepis.pub pi@a.b.c.d
/usr/bin/ssh-copy-id: INFO: Source of key(s) to be installed: "homepis.pub"
/usr/bin/ssh-copy-id: INFO: attempting to log in with the new key(s), to filter out any that are already installed
/usr/bin/ssh-copy-id: INFO: 1 key(s) remain to be installed -- if you are prompted now it is to install the new keys
pi@1a.b.c.d's password: 

Number of key(s) added: 1

Now try logging into the machine, with:   "ssh 'pi@a.b.c.d'"
and check to make sure that only the key(s) you wanted were added.

$ ssh -i homepis pi@a.b.c.d
Linux mypi 5.15.32+ #1538 Thu Mar 31 19:37:58 BST 2022 armv6l

The programs included with the Debian GNU/Linux system are free software;
the exact distribution terms for each program are described in the
individual files in /usr/share/doc/*/copyright.

Debian GNU/Linux comes with ABSOLUTELY NO WARRANTY, to the extent
permitted by applicable law.
Last login: Fri Nov 25 21:56:59 2022 from d.e.f.g

SSH is enabled and the default password for the 'pi' user has not been changed.
This is a security risk - please login as the 'pi' user and type 'passwd' to set a new password.

pi@mypi$ exit
logout
Connection to a.b.c.d closed.
$ 

```

The keys now need to be copied to the `attestationelementskeys` volume. There are a few ways of doing this:

#### As root
This is bad

```bash
$su -
#cd /var/lib/docker/volumes/attestationelementkeys/_data
#cp /home/ian/homepi* .
```