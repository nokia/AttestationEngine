from SensorManagementLibrary.BasicSensor import BasicSensor
import random
import time


class RngSensor(BasicSensor):
    def __init__(self):
        super().__init__()

    def run(self):
        while True:
            r = random.randint(1, 10)
            self.client.publish('management', r)
            time.sleep(4)


if __name__ == '__main__':
    x = RngSensor()
    x.run()
