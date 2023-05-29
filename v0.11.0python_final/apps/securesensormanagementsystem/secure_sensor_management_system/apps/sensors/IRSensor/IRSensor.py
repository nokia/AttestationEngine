from SensorManagementLibrary.BasicSensor import BasicSensor
import time
import busio
import board
import adafruit_amg88xx


class IRSensor(BasicSensor):
    # Sensor specific variables
    i2c = busio.I2C(board.SCL, board.SDA)
    amg = adafruit_amg88xx.AMG88XX(i2c)

    def __init__(self) -> None:
        super().__init__()

    def run(self):
        while True:
            mean = 0
            for column in self.amg.pixels:
                mean = sum(column) / len(self.amg.pixels)

            self.publish_data(round(mean, 2), "pixels")
            time.sleep(self.frequency)


if __name__ == "__main__":
    x = IRSensor()
    x.run()
