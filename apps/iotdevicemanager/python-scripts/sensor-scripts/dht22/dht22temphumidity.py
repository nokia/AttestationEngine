import servicebase
import Adafruit_DHT
import time
import paho.mqtt.client as mqtt
import RPi.GPIO as GPIO
import uuid
import sys


class DHT22TempHumidity(servicebase.ServiceBase):
   def __init__(self,params):
      self.dhtsensor = Adafruit_DHT.DHT22
      self.dhtpin = params["dhtpin"] 
      self.ledpin = params["ledpin"]
      GPIO.setmode(GPIO.BCM) 
      GPIO.setwarnings(False)
      GPIO.setup(self.ledpin, GPIO.OUT)
      super().__init__(params)
      
   def readValue(self):
      h,t = Adafruit_DHT.read_retry(self.dhtsensor,self.dhtpin)
      return round(h,1), round(t,1)

   def ledon(self):
      GPIO.output(self.ledpin,True)

   def ledoff(self):
      GPIO.output(self.ledpin,False)
  
   def start(self):
      self.ledoff() 
      return self.readValue()
   
