from SensorManagementSystem.BasicSensor import BasicSensor
import time
# Import your sensor dependencies
import board
import busio
import adafruit_vl53l0x


class ToFSensor(BasicSensor):
    # Sensor specific variables
    i2c = busio.I2C(board.SCL, board.SDA)
    vl53 = adafruit_vl53l0x.VL53L0X(i2c)
    vl53.measurement_timing_budget = 33000

    def __init__(self) -> None:
        super().__init__()

    def run(self):
        # Write your code here inside a while-loop etc.
        with self.vl53.continuous_mode():
            while True:
                if (self.vl53.data_ready):
                    time.sleep(self.frequency)
                    if (self.vl53.range < 1000):
                        # This method publishes the data to the UI
                        self.publish_data(self.vl53.range, "tof")


if __name__ == "__main__":
    x = ToFSensor()
    x.run()
