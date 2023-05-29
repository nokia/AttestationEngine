# Configuration file and message payload structure

Device and client configuration files are stored in the `/etc/iotDevice` directory.

Device configuration file is as follows:

```python
device_config = {
  "itemid": "",
  "hostname": socket.gethostname(),
  "address": get_ip_address(),
}
```

MQTT client configuration is as follows:

```python
client_config = {
  "host": "192.168.0.24",
  "port": 1883,
  "keepalive": 60
}
```

These two are built using a separate script which still needs some error handling etc.

Management message payload is created in the IoTElement class and the structure is:

```python
message = {
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
```

The reason for creating this in the IoTElement is that we'll always have a similar structure to the messages sent to and from the management system.

Device dict inside the message has information about the specific raspberry pi which runs the script in question. The values are read from device_config file. This structure is passed down from IoTElement first to BasicSensor and then to the concrete sensor implementation. The values are changed depending on what is going on in the system. Eg the device valid value is changed to True when the attestation for the pi is successful. The same goes for the sensor valid value.