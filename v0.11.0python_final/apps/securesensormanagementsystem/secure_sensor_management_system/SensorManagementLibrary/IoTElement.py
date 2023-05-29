import paho.mqtt.client as mqtt
import json
from datetime import datetime


class IoTElement:

    def __init__(self):
        self.client = mqtt.Client()
        self.device_config = self.read_config_file(
            '/etc/iotDevice/device_config.json')
        self.mqtt_client_config = self.read_config_file(
            '/etc/iotDevice/mqtt_client_config.json')

        self.connect_to_mqtt_client()

        # This is just a a structure for the payload sent from different
        # parts of the program.
        self.message = {
            "event": "",
            "message": "",
            "messagetimestamp": "",
            "device": {
                "itemid": self.device_config["itemid"],
                "hostname": self.device_config["hostname"],
                "address": self.device_config["address"],
                "starttimestamp": "",
                "valid": False,
                "validtimestamp": ""
            },
            "sensor": {
                "name": "",
                "starttimestamp": "",
                "valid": False,
                "validtimestamp": ""
            }
        }

    def connect_to_mqtt_client(self):
        try:
            self.client.connect(
                self.mqtt_client_config["mqtt_host"],
                self.mqtt_client_config["mqtt_port"],
                self.mqtt_client_config["mqtt_keepalive"]
            )
            print("IoTElement method: Connect succesfull")
        except ValueError:
            print("IoTElement method: Cannot connect to MQTT broker.")

    def read_config_file(self, path):
        try:
            with open(path, 'r') as f:
                print("IoTElement: File opened")
                return json.loads(f.read())
        except IOError:
            print("IoTElement: File opening not succesfull")

    def get_time_stamp(self):
        now = datetime.now()
        date_time = now.strftime("%d.%m.%Y, %H:%M:%S")
        return date_time