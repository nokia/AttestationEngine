#!/bin/python3
# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import paho.mqtt.client as mqtt
import paho.mqtt.subscribe as subscribe
import json
import ast
import threading
from colors import *
import argparse


ap = argparse.ArgumentParser(description='Displays the log file in real-time')

ap.add_argument('mqtt_address', help="IP Address of an MQTT broker associated with an A10 instance")
ap.add_argument('-p', '--mqttport', help="MQTT Broker port, default: 1883",  type=int, default=1883)
ap.add_argument('-nt', '--notthreaded', help="Do not spawn threads during printing",  action='store_true')
ap.add_argument('-q','--quiet', help="Suppress additional output, just the log only", action='store_true')

args = ap.parse_args()


def processMessage(p, t):
    m = ast.literal_eval(p.decode("ascii"))

    if t == "AS/IM":
        s = (
            m["t"].ljust(20)
            + " - "
            + m["op"].ljust(7)
            + m["data"]["type"].ljust(10)
            + " "
            + m["data"]["itemid"]
        )
        print(color(s, fg="white"))
    elif t == "AS/C":
        s = (
            m["t"].ljust(20)
            + " - "
            + m["op"].ljust(7)
            + m["data"]["type"].ljust(10)
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
            + m["data"]["type"].ljust(10)
            + " "
            + m["data"]["itemid"]
        )
        if r == "0":
            print(color(s, fg="green"), " ", color(r, fg="green"))
        elif r == "9001":
            print(color(s, fg="red"), " ", color(r, fg="red"))
        else:
            print(color(s, fg="yellow"), " ", color(r, fg="orange"))
    else:
        print(color("UNEXPECTED INTERNAL ERROR " + t + " " + p, fg="yellow"))


def on_connect(client, metadata, flags, rc):
    printm(" +--- MQTT Client connected")


def on_disconnect(client, metadata, flags, rc):
    printm(" +--- MQTT Client disconnected, retrying connect")
    try:
        client.reconnect()
    except:
        printm(" +--- MQTT client reconnection error")


def on_message(client, userdata, message):
    if args.notthreaded==False:
        x = threading.Thread(target=processMessage, args=(message.payload, message.topic,))
        x.start()
    else:
        processMessage(message.payload, message.topic)


def printm(s):
    if args.quiet==False:
        print(s)

# MAIN

printm("AS MQTT Terminal Viewer\n")

broker_port = args.mqttport
broker_url = args.mqtt_address

printm("Using broker at "+broker_url+" on port "+str(broker_port))


client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message
client.connect(broker_url, broker_port)

printm(" +--- MQTT Client connection is "+str(client))

client.subscribe("AS/R", qos=1)
client.subscribe("AS/C", qos=1)
client.subscribe("AS/IM", qos=1)

printm(" +--- Running, press ctrl+C to stop\n\n")

client.loop_start()

if args.quiet==False:
    x = input("Press CTRL+C to stop")
else:
    x = input()

printm(" +--- Exiting.")
