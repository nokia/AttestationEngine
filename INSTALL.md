# Installation with Docker

*IMPORTANT NOTE* The docker container version is the recommended way of deploying A10 and its components. This is the way to do things when you just want to run the system. If you want to develop then you should run everything locally without docker. In this case please refer to the specific README and INSTALL files in those directories.

The container is built using docker-compose from the individual elements A10, A10REST and U10. Once configured the system can be started from the command line or systemd or whatever as required:

```bash
docker-compose up -d
```

The container contains:

   * U10 with the A10 libraries (deployed as docker containers)
   * A10REST with the A10 libraries (deployed as docker containers)
   * Mongodb
   * Mosquitto
   * Mongo-express (as a covenient UI for viewing the database)
   
 It requires two files to be created
 
   * a10.conf
   * mosquitto.conf (a default is supplied)
   
 And a docker volume to store the data persistently
 
   * docker volume called `attestationdata`
   
The rest of the sections detail how to build the A10, U10 and A10REST containers, if not already done, how to configure docker-compose and how to run the system.
   

## Building A10

Follow the building instructions in the a10/README.md
To upload to the pypi server do the following

## A10.CONF

The file a10.conf is used as the configuration file for the various components. It is supplied as an external file to u10 and a10rest below. An example file for working on your local machine is given below

```
[Identification]
asvrname = A10_development

[Debugging]
debug=on

[Logging]
logfile = /tmp/a10.log

[mqtt]
mqttclientname= a10mqtt
mqttaddress=10.144.176.154
mqttport=1883
keepaliveping=10

[mongo]
mongodburl=mongodb://10.144.176.154:27017/
mongodbname=PRODUCTION_ASVR
```

The important lines are the addreses of the mqtt server and the mongo database, as well as the name of the database. 

The keepaliveping must be below 60 - a good value is 45 - this is because mosquitto has a nsaty habit of disconnecting clients that are only subscribing and not producing data. You can also use this as a heartbeat

## Building and Running U10

In the u10 directory there is a Dockerfile which gathers together everything *except* the a10.conf file used to configure things. In this example we have a docker repository at x.x.x.x:5000

NOTE: at this point in time we do not have these installed on any public docker repo

It may be necessary to edit the Dockerfile to configure the location of the pypi server and any proxies required by pip3.

```bash
docker build -t u10 .
docker tag a10rest x.x.x.x/u10
docker push x.x.x.x/u10
```

To run use the following command. You must supply an a10.conf file somewhere, eg: /home/ioliver/a10.conf as we have done here.  It is probably a good idea to ensure that if you want this u10 interacting with the same database as a10rest later then the a10.conf file is the same.

The default port for a10rest is 8520

```bash
docker run -it -v /home/ioliver/a10.conf:/etc/a10.conf -p 8540:8540 x.x.x.x/u10
```

The file above `/home/ioliver/a10.conf` is where you put all your configuration information to run the container.  

u10 serves the pages on port 8540 and this needs to be mapped to a suitable local port,eg: 8544 in this case

Note: any data created will not be saved permanently unless a volume is created for such purposes. This is best done as part of the docker-compose process detailed below.


## Building and Running A10rest

In the a10rest directory there is a Dockerfile which gathers together everything *except* the a10.conf file used to configure things. 

It may be necessary to edit the Dockerfile to configure the location of the pypi server and any proxies required by pip3.

```bash
docker build -t a10rest .
docker tag a10rest x.x.x.x/a10rest
docker push x.x.x.x/a10rest
```


To run use the following command. You must supply an a10.conf file somewhere, eg: /home/ioliver/a10.conf as we have done here. It is probably a good idea to ensure that if you want this a10rest interacting with the same database as u10 from earlier then the a10.conf file is the same.

The default port for a10rest is 8520

```bash
docker run -it -v /home/ioliver/a10.conf:/etc/a10.conf -p 8520:8520 x.x.x.x/a10rest 
```

The file above `/home/ioliver/a10.conf` is where you put all your configuration information to run the container.  

u10 serves the pages on port 8530 and this needs to be mapped to a suitable local port,eg: 8543 in this case

Note: any data created will not be saved permanently unless a volume is created for such purposes. This is best done as part of the docker-compose process detailed below.

## Docker Compose

Use the docker compose file in the directory `utilities/dockercomposedeployment`. The docker-compose file assumes that u10 and a10rest are on available docker respositories. To bring the system up you must first have an a10.conf file in a place where u10 and a10rest can obtain this file. An example a10.conf file is provided in the dockercomposedeployment directory. 

### Configuration 

Edit the docker-compose-yml file as required to set the u10/a10rest repository locations and exposed port numbers and a10.conf and mosquitto.conf file locations.

1. Check the networking and docker (see section below on networking)
1. Edit the locations of the docker images if you are using a different docker repository
2. Edit the exposed ports for the databaseui, messagebus, u10 and a10rest
3. Make an a10.conf file available to u10 and a10rest by editing the file pointed to in the volumes section
4. Make a mosquitto.conf file available to messagebus. There is a default mosquitto.conf supplied for reference. This file should work as is.
5. Ensure a volume is available for the data - docker-compose does this automatically (???)
5. A search and replace is necessary if you wish to change the name of the volume, eg: for multiple separate instances of the attestation engine

### Volumes
As the recommended method of using this is by docker-compose (or helm charts in the future) a volume to store the attestation database is automatically created externally and persisted.

If a volume needs to be created:

```bash
docker volume create --name=attestationdata
```
Further information about this can be found using the commands below

```bash
docker volume ls
docker volume inspect attestationdata
```


### Networking
Because the environment needs to talk to the outside world, then you *might* need to allow the docker0 network to route to the host. This worked on Ubuntu 21.04

```bash
sudo sysctl net.ipv4.conf.all.forwarding=1
sudo iptables -P FORWARD ACCEPT
```

See here: docs.docker.com/network/bridge


### Starting

To start the system (the -d option runs this in daemon or background mode - it can be left out for testing)

```bash
docker-compose up -d
```

If all is working you should be able to point a browser at the address of the machine running the docker containers at whatever port u10 is running on to get the u10 GUI. To test a10rest simply run curl as shown below (or use a browser) and you get a hello message:

Make sure you modify the IP address and port to point at the correct machine, eg: 10.144.176.154

```bash
curl 10.144.176.154:8521
Hello from A10REST
```

The u10, mqtt and mongo-express services are available on the approriate other ports


### Database Contents
By default the database is unpoplulated.

Under utilities/Database are a set of files with starting information for a black attestation server. These include

   * Policies
   * Hash Values

### Shutdown

To shutdown the system

```bash
docker-compose down
```





