# Running with Docker Compose

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

THe first time docker-compose is used the system may require a volumne to be created to store persistent data. For example:

```bash
$ sudo docker-compose up
Creating network "dockercomposedeployment_attestationnetwork" with driver "bridge"
ERROR: Volume attestationdata declared as external, but could not be found. Please create the volume manually using `docker volume create --name=attestationdata` and try again.
```

Run the following command (as suggested above and then re-try docker-compose)

```bash
sudo docker volume create --name=attestationdata
attestationdata
ian@hitadebian:~/AttestationEngine/utilities/dockercomposedeployment$ sudo docker-compose up
Creating messagebus ... done
Creating mongo      ... done
Creating a10rest                              ... done
Creating u10                                  ... done
Creating dockercomposedeployment_databaseui_1 ... done
Creating fc10                                 ... done
Attaching to messagebus, mongo, u10, dockercomposedeployment_databaseui_1, a10rest, fc10
```

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