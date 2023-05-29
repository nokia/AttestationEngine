# Setup

## Requirements

- one or more raspberry pi
- one or more sensors

In this project we used three raspberry pi devices. Sensors in use were IR-sensor, lux-sensor and time of flight-sensor. We also used a web-camera.

## Connecting sensors for pi

Tutorials for different kinds of raspberry pi sensors:
https://tutorials-raspberrypi.com/raspberry-pi-sensors-overview-50-important-components/

//images here if needed

We connected the lux-sensor and web-camera to one raspberry pi. The IR-sensor and time of flight-sensor were connected separately on remaining two raspberry pi devices.

## Required software for pi

Each raspberry pi needs a bootup file script that uses the systemd.

- Python3
- vim or nvim if you don't want to use nano
- mqtt
- flask (attestation, web-camera, REST)
- Imports from sensor manufacturer (board, busio etc.)

##

# Design

The system is build so that every class inherits a MQTT client from the IoTElement class. There is also a configuration file located in xxx which has all the necessary options to connect to the MQTT broker and send correct types of messages.

![device class diagram](documentation/pics/insidedevice.JPG)

# Data flow

When the system starts up it sends various MQTT messages to notify the broker about the state of the various subsystems. Firstly the validity of the device running the sensor script is checked. The sensor startup doesn't depend on the validity check. We can just see if the device is valid or not.

![sequence diagram](documentation/pics/devicesequence.JPG)

## MQTT topic naming conventions

Sensors are named as `sensor/webcam`, `sensor/ir`, `sensor/lux`, `sensor/tof`.

### Management channel

```
management/
```

### Alert channel

```
alert/
```

### Data channels

```
prefix/<measurementtype>
```

Measurementtype here means the type of measured data. This could be array of pixels, temperature, distance etc

Most important payload fields:
itemid and event

How to name different events:

device startup

device validation start

device validation ok

device validation fail

sensor startup

manager startup
