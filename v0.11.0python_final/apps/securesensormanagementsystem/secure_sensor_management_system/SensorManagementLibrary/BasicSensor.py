from SensorManagementLibrary.IoTElement import IoTElement
import json
import argparse
import pathlib
import inspect


class BasicSensor(IoTElement):

    def __init__(self):
        super().__init__()

        self.sensor_config = self.get_sensor_config()

        self.sensor_name = self.sensor_config["name"]
        self.frequency = self.sensor_config["frequency"]

        self.message["sensor"]["name"] = self.sensor_name
        self.message["event"] = "Sensor starting"
        self.message["message"] = "Sensor " + self.sensor_name + \
            " started on "+self.message["device"]["hostname"]
        self.message["sensor"]["starttimestamp"] = self.get_time_stamp()
        self.message["messagetimestamp"] = self.get_time_stamp()
        self.client.publish("management", json.dumps(self.message))
        # self.attest_validate(self.message)

    def publish_data(self, data, topic_end):
        topic = f"{self.sensor_config['prefix']}/{topic_end}"
        self.client.publish(topic, payload=data)

    def run(self):
        pass

    def attest_validate(self, json_update):
        json_object = json_update
        # Needs to specify the event type
        json_object["event"] = "validateSensor"
        json_object["message"] = "Sensor validation request"
        json_object["messagetimestamp"] = self.get_time_stamp()
        print(json_object)
        # Publish to manager for validation
        self.client.publish(f"management", json.dumps(json_object))

    def get_sensor_config(self):
        all_args = argparse.ArgumentParser()
        all_args.add_argument("-f", "--config", type=argparse.FileType("r"),
                              help="Config file to be used.")

        args = vars(all_args.parse_args())

        if args["config"] is None:
            path_to_parent = pathlib.Path(inspect.getfile(
                self.__class__)).parent.absolute()

            config_path = pathlib.Path(path_to_parent / "sensor_config.json")

            config = self.read_config_file(config_path)

            return config

        return json.loads(args["config"].read())
