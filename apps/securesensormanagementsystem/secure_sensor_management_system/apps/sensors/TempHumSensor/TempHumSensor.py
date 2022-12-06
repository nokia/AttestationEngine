from SensorManagementSystem.BasicSensor import BasicSensor
import time
import board
import adafruit_si7021


class TempHumSensor(BasicSensor):

    tsl = adafruit_si7021.SI7021(board.I2C())

    def __init__(self) -> None:
        super().__init__()

    def run(self):
        while True:
            print("temperature: " + str(self.tsl.temperature))
            print("humidity: " + str(self.tsl.relative_humidity))

            # For now, the data_topic_end fields need to be written manually,
            # because the sensor delivers two separate values
            self.publish_data(self.tsl.temperature, "temp")
            self.publish_data(self.tsl.relative_humidity, "hum")
            time.sleep(self.frequency)


if __name__ == "__main__":
    x = TempHumSensor()
    x.run()
