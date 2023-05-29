# ASMQTTVIEW

A simple utility for printing out the log file in real-time. Just point it at the MQTT broker (Mosquitto usually) where the log for A10 is being broadcast.

This file can be used as a template for more complex processing, eg: reacting to verification errors and reporting those.

## Prerequisites
This depends upon paho-mqtt, ansicolors, both of which may be installed using pip or pip3.  Json and threading should already be provided.

## Running
To run, just provide the address of a suitable MQTT broker

```bash
./asmqttviewer.py 127.0.0.1
```

Hit CTRL-C to exit.

### Example of Output
An example session is shown below:

```bash
$ ./asmqttviewer.py 127.0.0.1 
AS MQTT Terminal Viewer

Using broker at 127.0.0.1 on port 1883
 +--- MQTT Client connection is <paho.mqtt.client.Client object at 0x7fe2ebdb2be0>
 +--- Running, press ctrl+C to stop


 +--- MQTT Client connected
Press CTRL+C to stop
1647777499.867791    - add    session    58614537-6fef-4463-a0a2-2b811a78d71a
1647777499.874184    - add    session    9346fd6a-a975-422a-af4f-1149b5d9fdfe
1647777501.045108    - add    claim      bf430191-108b-4848-b54b-b25eea322f2f
1647777501.055127    - add    result     da3a6e1f-9b59-4542-95f6-40a27e56a800   0
1647777501.063679    - add    result     d5f8f1a0-9b9b-48aa-bc79-4d09514f736e   9001
1647777501.074713    - add    result     da4e5737-1f99-4075-a99b-7fda398f39a3   9002
1647777501.093801    - add    result     e3858129-a9d0-41c0-9db0-659c5050c10d   9100
1647777501.103127    - update session    9346fd6a-a975-422a-af4f-1149b5d9fdfe
1647777501.108352    - update session    58614537-6fef-4463-a0a2-2b811a78d71a
^CTraceback (most recent call last):
  File "/home/ian/AttestationEngine/apps/asmqttviewer/./asmqttviewer.py", line 118, in <module>
    x = input("Press CTRL+C to stop")
KeyboardInterrupt
$ 
```

The UUID is the itemid of a corresponding element, policy, expectedvalue, claim, result or session.  If a result is obtained then the result code is also shown. These are defined in a10structures, but briefly: 0=success, 9001=fail, 9002=internal error, 9100=external error.


## Options
To list all the options available use `--help`

```bash
./asmqttviewer.py --help
usage: asmqttviewer.py [-h] [-p MQTTPORT] [-nt] [-q] mqtt_address

Displays the log file in real-time

positional arguments:
  mqtt_address          IP Address of an MQTT broker associated with an A10 instance

optional arguments:
  -h, --help            show this help message and exit
  -p MQTTPORT, --mqttport MQTTPORT
                        MQTT Broker port, default: 1883
  -nt, --notthreaded    Do not spawn threads during printing
  -q, --quiet           Suppress additional output, just the log only

```

The threaded option is explained below.

The quiet option suppresses various friendly messages that are generally superfluous. Just remember CTRL-C exits.

## Threaded vs Non-Threaded
As processing of the message may take some time, especially if more queries need to be made and marshalling/unmarshalling of JSON, XML etc needs to be performed, it is possible that paho-mqtt receives another message from the MQTT broker during this time. Usage of the threaded model means that all processing of a message is made in a separate new thread and we can respond to incoming messages without affecting the currently running thread(s),

In this simple version here this is unlikely to happen.