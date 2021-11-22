# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import paho.mqtt.client as mqtt
import paho.mqtt.subscribe as subscribe
import json
import ast
import threading
from colors import *


def processMessage(p, t):
    print("Message received ", p, t)
    m = ast.literal_eval(p.decode("ascii"))
    print(m, t, t == "AS/C")
    if t == "AS/IM":
        s = (
            m["t"].ljust(20)
            + " - "
            + m["op"].ljust(7)
            + m["data"]["kind"].ljust(10)
            + " "
            + m["data"]["itemid"]
        )
        print(color(s, fg="white"))
    elif t == "AS/C":
        s = (
            m["t"].ljust(20)
            + " - "
            + m["op"].ljust(7)
            + m["data"]["kind"].ljust(10)
            + " "
            + m["data"]["itemid"]
        )
        print(color(s, fg="cyan"))
    elif t == "AS/R":
        r = str(m["data"]["result"])
        s = (
            m["t"].ljust(20)
            + " - "
            + m["op"].ljust(7)
            + m["data"]["kind"].ljust(10)
            + " "
            + m["data"]["itemid"]
        )
        if r == "0":
            print(color(s, fg="green"), " ", color(r, fg="blue", bg="black"))
        elif r == "9001":
            print(color(s, fg="red"), " ", color(r, fg="yellow", bg="red"))
        else:
            print(color(s, fg="yellow"), " ", color(r, fg="blue", bg="orange"))
    else:
        print(color("UNEXPECTED INTERNAL ERROR " + t + " " + p, fg="orange"))


def on_connect(client, metadata, flags, rc):
    print(" +--- MQTT Client connected")


def on_disconnect(client, metadata, flags, rc):
    print(" +--- MQTT Client disconnected, retrying connect")
    try:
        client.reconnect()
    except:
        print(" +--- MQTT client reconnection error")


def on_message(client, userdata, message):
    print("message")
    # x = threading.Thread(target=processMessage, args=(message.payload, message.topic,))
    # x.start()
    processMessage(message.payload, message.topic)


# MAIN

print("\n\nAS MQTT Terminal Viewer\n\n")

broker_port = 1883
# broker_port= 8560
# broker_url="10.144.176.146"
broker_url = "127.0.0.1"


client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message
client.connect(broker_url, broker_port)

print(" +--- MQTT Client connection is ", client)

client.subscribe("AS/R", qos=1)
client.subscribe("AS/C", qos=1)
client.subscribe("AS/IM", qos=1)
client.subscribe("AS/MQTTPING", qos=1)

print(" +--- Running, press ctrl+C to stop\n\n")

client.loop_start()

x = input("Press CTRL+C to stop")

print(" +--- Exiting.")
