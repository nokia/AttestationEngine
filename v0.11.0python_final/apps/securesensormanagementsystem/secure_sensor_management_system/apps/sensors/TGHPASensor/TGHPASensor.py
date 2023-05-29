from SensorManagementLibrary.BasicSensor import BasicSensor
import time
import board
import adafruit_bme680


class tghpaSensor(BasicSensor):
    i2c = board.I2C()  # uses board.SCL and board.SDA
    bme680 = adafruit_bme680.Adafruit_BME680_I2C(i2c, debug=False)
    bme680.sea_level_pressure = 1025.9
    temperature_offset = -1

    def __init__(self) -> None:
        super().__init__()

    def run(self):

        while True:
            temperature = self.bme680.temperature + self.temperature_offset
            self.publish_data(round(temperature, 1), "temperature")
            #print("\nTemperature: %0.1f C" % (self.bme680.temperature + self.temperature_offset))
            gas = self.bme680.gas
            self.publish_data(round((gas/1000), 1), "gas")
            #print("Gas: %d ohm" % self.bme680.gas)
            humidity = self.bme680.relative_humidity
            self.publish_data(round(humidity, 1), "humidity")
            #print("Humidity: %0.1f %%" % self.bme680.relative_humidity)
            pressure = self.bme680.pressure
            self.publish_data(round(pressure, 1), "pressure")
            #print("Pressure: %0.3f hPa" % self.bme680.pressure)
            altitude = self.bme680.altitude
            self.publish_data(round(altitude, 1), "altitude")
            #print("Altitude = %0.2f meters" % self.bme680.altitude)
            time.sleep(self.frequency)

if __name__ == "__main__":
    x = tghpaSensor()
    x.run()