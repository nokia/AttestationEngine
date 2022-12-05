from SensorManagementLibrary.BasicSensor import BasicSensor
import time
import busio
import board
import adafruit_tsl2561
import paho.mqtt.client as mqtt


class LuxSensor(BasicSensor):
    # Sensor specific variables
    i2c = busio.I2C(board.SCL, board.SDA)
    tsl = adafruit_tsl2561.TSL2561(i2c)

    def __init__(self) -> None:
        super().__init__()

    def run(self):
        while True:
            self.publish_data(round(self.tsl.lux, 1), "lux")
            time.sleep(self.frequency)


if __name__ == "__main__":
    x = LuxSensor()
    x.run()
