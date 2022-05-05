import time
import sys
import paho.mqtt.client as mqtt


class ServiceBase():
   def __init__(self,params):
      # Certain parameters must be provided
      # MQTT broker IP
      # MQTT broker Port
      # Channel Name
      
      self.mqttbrokerip=params["mqttbrokerip"]
      self.mqttbrokerport=params["mqttbrokerport"]
      self.mqttchannel=params["mqttchannel"]
      
      self.mqttc = mqtt.Client()
      self.mqttc.connect(self.mqttbrokerip)
      
   def publish(self,msg):
      # Publishes the data packet msg to the MQTT broker
      
      fmsg = str({ "ch":self.mqttchannel, "p":msg })
      
      self.mqttc.publish(self.mqttchannel,fmsg)


   def stop(self):
      self.mqttc.disconnect()
