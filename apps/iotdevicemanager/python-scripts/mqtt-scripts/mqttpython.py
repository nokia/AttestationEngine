import paho.mqtt.client as mqtt
from random import randrange, uniform
import time
import json

mqttBroker = 'test.mosquitto.org'
client = mqtt.Client("Random_Number")
client.connect(mqttBroker)
MQTT_MSG=json.dumps({"deviceName": "buddy","channels":  "temperature, humidity"});
MQTT_MSG2=json.dumps({"temperature": "25"});
MQTT_MSG3=json.dumps({"humidity": "25"});

randNumber = uniform(1.0,100.0)
client.publish("ANNOUNCEMENTS", MQTT_MSG)
time.sleep(2)
client.publish("temperature", MQTT_MSG2)
time.sleep(2)
client.publish("humidity", MQTT_MSG3)
