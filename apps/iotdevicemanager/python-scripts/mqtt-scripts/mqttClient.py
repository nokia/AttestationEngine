import paho.mqtt.client as mqtt
import time

def on_message(client, userData, message):
    print("Received: ", str(message.payload.decode("utf-8")))

mqttBroker = 'test.mosquitto.org'
client = mqtt.Client("Web_client")
client.connect(mqttBroker)

client.loop_start()

client.subscribe("RANDOM_NUMBER")
client.on_message = on_message
time.sleep(20)

client.loop_end()