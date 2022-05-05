# DHT22 Temperature and Humidity Service
 
## Architecture
 
NB: This will only run on devices with the Adafruit_DHT libraries - typically Raspberry Pis.
 
### Sensor Values 
Reads from the DHT22 sensor and rounds the values to 1 decimal place.

```python3
h,t = Adafruit_DHT.read_retry(self.dhtsensor,self.dhtpin)
return round(h,1), round(t,1)
```
Humidity is reported in percent and temperature in celcius.

The following temperature scales are not supported, you can write your own converters:  Fahrenheit, Kelvin, Rankine, Romer, Newton, Delisle, Reaumur.  Go read the the history of the Celcius-Centigrade naming, it is quite interesting.

## Parameters 
 
The environment variable DHT22TEMPHUMIDITY_SVC_PARAMS needs to be set, for example

```sh
export DHT22TEMPHUMIDITY_SVC_PARAMS="{ 'rate':10, 'dhtpin':4, 'ledpin':26, 'mqttbrokerip':'10.144.176.154', 'mqttbrokerport':'1883', 'mqttchannel':'k226_temp' }"
```

An example parameter set is in the file `setExampleParameters.sh`

The parameters are:

   * rate:  the rate of samples in seconds, a good value for testing is about 5
   * dhtpin:  the GPIO pin of the DHT22 sensor using the GPIO.BCM numbering
   * ledpin:  the GPIO pin of an LED flashing when a measurement is made, using the GPIO.BCM numbering
   * mqttbrokerip:  
   * mqttbrokerport:  usually this is 1883
   * mqttchannel: name of the channel, eg:  K226_temp

## Running Bare Metal

1. Set the parameters - see previous section
2. Run `python3 temphum.py`

Alternatively run `nohup python3 temphum.py &`  and then logout OR write a systemd script to start it
 
## Docker

1. Set the parameters - see previous section
2. Run `docker run ...`

or use K3S
