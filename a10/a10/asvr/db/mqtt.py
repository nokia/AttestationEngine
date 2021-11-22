# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import paho.mqtt.client as mqtt
import a10.structures.identity
import a10.asvr.db.configuration
import threading
import time


def on_disconnect(client, userdata, rc):
    logging.info("disconnecting reason  " + str(rc))
    client.connected_flag = False
    client.disconnect_flag = True


def on_connect(client, metadata, flags, rc):
    print("Connected mqtt: {}".format(rc))


def on_disconnect(client, metadata, flags, rc):
    print("MQTT Disconnected")
    try:
        client.reconnect()
    except:
        print("Connection is fscked")


def publish(ch, t, op, data):
    payload = str({"t": t, "op": op, "data": data})
    mqttc.publish(ch, payload)


def sendKeepAlive():
    print(
        "Starting keepalive ping with rate ",
        a10.asvr.db.configuration.MQTTKEEPALIVEPING,
    )
    while True:
        print("ping!")
        publish(
            "AS/MQTTPING",
            "ping",
            "ping",
            {"session": a10.asvr.db.configuration.ASSESSIONIDENTITY},
        )
        time.sleep(int(a10.asvr.db.configuration.MQTTKEEPALIVEPING))


print(a10.asvr.db.configuration.MQTTADDRESS)
#
# This is a bit nasty, but if two clients have the same name then the earlier one
# will be kicked off by the MQTT broker - at least in mosquitto
# So we will add the AS_Session_Identity and a UUID
#
id = (
    a10.asvr.db.configuration.MQTTCLIENTNAME
    + "_"
    + a10.asvr.db.configuration.ASSESSIONIDENTITY
    + "_"
    + a10.structures.identity.generateID()
)
print("mqtt client id is ", id)
mqttc = mqtt.Client(id)
mqttc.on_connect = on_connect
mqttc.connect(a10.asvr.db.configuration.MQTTADDRESS)


# KEEP ALIVE PING
print("Starting keep alive thead")
keepalivethread = threading.Thread(target=sendKeepAlive)
print("Keep alive thread ID is ", keepalivethread)
keepalivethread.start()
