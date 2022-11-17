# Running with Docker Compose

## Building the components

From the root directory of the attesstation server distribution we need to build the components A10REST, U10 and FC10. Firstly check that everything is there and that you are in the right place:

```bash
ian@hitadebian:~/AttestationEngine$ ls
a10rest  a10server  a10structures  apps  CONTRIBUTING.md  docs  fc10  LICENSE  README.md  t10  tests  u10  utilities
```

Then build the three components:

```bash
sudo docker build -t a10rest -f a10rest/Dockerfile.local .
sudo docker build -t u10 -f u10/Dockerfile.local .
sudo docker build -t fc10 -f fc10/Dockerfile.local .
```

This takes a while and requires a connection to the internet in order to download the base images and required packages.

## Configuriation

The directory `..../AttestationEngine/utilities/dockercomposedeployment` contains an example docker-compose setup and the files `a10.conf` and `mosquitto.conf` can be modified accordingly. Usually no changes are required.

If you wish to change the port assignments then edit the ports in `docker-compose.yml` but again this is usually not required.

## Running

To start the containers:

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

From a local machine the following should be accessible:

   * http://127.0.0.1:8520/v2 - should return a welcome message (See below for a curl example, or use a browser)
   * http://127.0.0.1:8520/v2 - should return the U10 main page
   * http://127.0.0.1:8520/v2 - should return the FC10 main page

The test for a working *A10REST* component is

```bash
$ curl http://127.0.0.1:8520/v2/
{
  "msg": "Hello from A10REST v2"
}
```

*U10*'s main page's configuration section should match the `a10.conf` file used with docker-compose.

*FC10*'s main page contains a link to then endpoint `http://a10rest:8520/v2` with the name *Docker Compose Installation*, and when accessed should return the current or achived elements as applicable

