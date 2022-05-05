import json
import paho.mqtt.client as mqtt
import json
from datetime import datetime

mqttBroker = 'add the ip here'
deviceName = 'iotpi01'
client = mqtt.Client('Iot-Device-Manager')
client.connect(mqttBroker, port=1883, bind_address='')
file = open('/home/pi/iot-device-manager/iotid.txt', 'r')
deviceId = file.read().splitlines()[0]
file.close()
MQTT_MSG=json.dumps({"deviceName": deviceName, "_id": deviceId, "channels":  ["light", "noise"],  "timestamp": datetime.timestamp(datetime.now()), "disconnect": False});

client.publish('ANNOUNCEMENTS', MQTT_MSG)
