import adafruit_dht
import board
import time
import paho.mqtt.client as mqtt
from datetime import datetime
import json

dht_device = adafruit_dht.DHT22(board.D18, 
use_pulseio=False)

mqttBroker = 'ip address here'
mqttPort = 1883

client = mqtt.Client('Iot-Device-Manager')
client.connect(mqttBroker, port = mqttPort, bind_address="")

file = open('iotid.txt', 'r')
deviceId = file.read().splitlines()[0]
file.close()

time.sleep(1)

try:
    while True:
        temperature = dht_device.temperature
        humidity = dht_device.humidity
        if humidity is not None and temperature is not None:
            print("Temp={0:0.1f}*C Humidity={1:0.1f}%".format(temperature,humidity))
            MQTT_humidity=json.dumps({"_id": deviceId, "sensorType": "humidity", "sensorValue": humidity, "timestamp": datetime.timestamp(datetime.now())})
            MQTT_temperature=json.dumps({"_id": deviceId, "sensorType": "temperature", "sensorValue": temperature, "timestamp": datetime.timestamp(datetime.now())})
            client.publish("temperature", MQTT_temperature)
            client.publish("humidity", MQTT_humidity)
            time.sleep(5)
        else:
            print("Failed to retrieve data from humidity sensor") 
            
except Exception as e:
    dht_device.exit()
    print("an exception occurred", e.args)
