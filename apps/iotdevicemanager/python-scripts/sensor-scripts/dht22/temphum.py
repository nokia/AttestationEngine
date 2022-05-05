import dht22temphumidity
import time
import os
import ast

print("Starting DHT22 reader")
params = ast.literal_eval(os.environ.get('DHT22TEMPHUMIDITY_SVC_PARAMS'))
print(" parameters ",params)

if (params==None):
    print("Missing parameters - set the DHT22TEMPHUMIDITY_SVC_PARAMS environment variable")
    exit(1)
s = dht22temphumidity.DHT22TempHumidity(params)
s.start()
print("Running")
try:
   while True:
     t,h=s.readValue()
     if (t,h)==None:
        print("sensor error")
     else:
        print(t,h)
        s.publish(str((t,h)))
        time.sleep(params["rate"])

except KeyboardInterrupt:
      print("Interrupted!")
s.stop()
print("Stopped")

