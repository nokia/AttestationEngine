import time
import os
import paho.mqtt.client as mqtt
import ast
import RPi.GPIO as GPIO

GPIO.setwarnings(False)
GPIO.setmode(GPIO.BOARD)
#output from DFR0033 sensor
GPIO.setup(11, GPIO.IN)


def __init__(params):
      # Certain parameters must be provided
      # MQTT broker IP
      # MQTT broker Port
      # Channel Name

    mqtt.Client().connect(params["mqttbrokerip"], port=int(params["mqttbrokerport"]))


def publish(msg):

    # Publishes the data packet msg to the MQTT broker

    fmsg = str(msg)

    mqtt.Client().publish(params["mqttchannel"],fmsg)

def stop():
    mqtt.Client().disconnect()


# Start publish

print("Starting DFR0030 reader")
params = ast.literal_eval(os.environ.get('DFR0030_PARAMS'))
print(" Parameters ",params)
__init__(params)

if (params==None):
    print("Missing parameters - set the DFR0030_PARAMS environment variable")
    exit(1)

print("Running")
try:
   while True:
     i=GPIO.input(11)
     if i==1:
        publish("magnetic object detected")
        time.sleep(params["rate"])
     else:
        time.sleep(0.1)

except KeyboardInterrupt:
      print("Interrupted!")

print("Stopped") 
