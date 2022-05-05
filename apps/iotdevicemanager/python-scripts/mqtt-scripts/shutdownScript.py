import json
import paho.mqtt.client as mqtt
import time
import json
from datetime import datetime

mqttBroker = 'test.mosquitto.org'
deviceName = 'iotpi011'
client = mqtt.Client('Iot-Device-Manager')
client.connect(mqttBroker, port=1883, bind_address='')

MQTT_MSG=json.dumps({"deviceName": deviceName, "_id": "38c28ce0-d49f-47f8-aa77-e14c42504657", "channels":  ["light", "noise"],  "timestamp": datetime.timestamp(datetime.now()), "disconnect": True});

client.publish('ANNOUNCEMENTS', MQTT_MSG)