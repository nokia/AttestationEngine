import adafruit_dht
import board
import time
import paho.mqtt.client as mqtt
import uuid
import json

dht_device = adafruit_dht.DHT22(board.D18, 
use_pulseio=False)

mqttBroker = #broker
mqttPort = 1883

client = mqtt.Client(str(uuid.uuid4()))
client.connect(mqttBroker, port = mqttPort, bind_address="")


MQTT_MSG=json.dumps({"deviceName":
"iotpi007",
"channels":
"temperature, humidity"})

client.publish("ANNOUNCEMENTS", MQTT_MSG)

time.sleep(1)

try:
    while True:
        temperature = dht_device.temperature
        humidity = dht_device.humidity
        if humidity is not None and temperature is not None:
            print("Temp={0:0.1f}*C Humidity={1:0.1f}%".format(temperature,humidity))
            client.publish("temperature", temperature)
            client.publish("humidity", humidity)
            time.sleep(5)
        else:
            print("Failed to retrieve data from humidity sensor") 
            
except Exception as e:
    dht_device.exit()
    print("an exception occurred", e.args)
