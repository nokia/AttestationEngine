import paho.mqtt.client as mqtt
from random import randrange, uniform
import time
import json
from datetime import datetime   

mqttBroker = 'test.mosquitto.org'
client = mqtt.Client("Random_Number")
client.connect(mqttBroker)

MQTT_MSG2=json.dumps({"_id": "38c28ce0-d49f-47f8-aa77-e14c42504657", "sensorType": "light", "sensorValue": "25", "timestamp": datetime.timestamp(datetime.now())});
MQTT_MSG3=json.dumps({"_id": "38c28ce0-d49f-47f8-aa77-e14c42504657", "sensorType": "light", "sensorValue": "23", "timestamp": datetime.timestamp(datetime.now())});

client.publish("light", MQTT_MSG2)
time.sleep(2)
client.publish("light", MQTT_MSG3)
