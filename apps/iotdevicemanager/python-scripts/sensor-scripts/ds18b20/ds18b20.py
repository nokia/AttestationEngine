import time
import os
import glob
import paho.mqtt.client as mqtt
import ast

#turn on GPIO mod
os.system("modprobe w1-gpio")
#turn on Temp mod
os.system("modprobe w1-therm")

# directory
directory = '/sys/bus/w1/devices/'
device_dir = glob.glob(directory + '28*')[0]
temperature_sensorfile = device_dir + '/w1_slave'

# read raw data from sensor
def get_rawdata():
  f = open(temperature_sensorfile, 'r')
  # Returns text
  lines = f.readlines()
  f.close()
  return lines

def read_temperature():
    # Read the raw temperature
    lines = get_rawdata()
 
    # While the first line does not contain "YES", wait 
    # and then read the device file again.
    while lines[0].strip()[-3:] != "YES":
        time.sleep(0.2)
        lines = get_rawdata()
 
    # Look for the position of the ="" in the second line of the
    # device file.
    position = lines[1].find("t=")
 
    # If "=" is found, convert the rest of the line after the
    # degrees Celsius
    if position != -1:
        temperature_string = lines[1][position+2:]
        temperature_celcius = float(temperature_string) / 1000.0
        return temperature_celcius

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
    

"""

Test print

while True:
    print(read_temperature())
    time.sleep(5)

"""

# Start publish

print("Starting DS18B20 reader")
params = ast.literal_eval(os.environ.get('DS18B20_PARAMS'))
print(" Parameters ",params)
__init__(params)

if (params==None):
    print("Missing parameters - set the DS18B20_PARAMS environment variable")
    exit(1)

print("Running")
try:
   while True:
     temperature = read_temperature()
     if (temperature)==None:
        print("Sensor error")
     else:
        print(temperature)
        publish(str(temperature))
        time.sleep(params["rate"])

except KeyboardInterrupt:
      print("Interrupted!")

print("Stopped")
