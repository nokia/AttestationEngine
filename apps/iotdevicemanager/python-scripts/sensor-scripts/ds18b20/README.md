# ds18b20 Temperature sensor
 
## Architecture
 
Temperature with the 1-Wire interface
Dallas ds18b20 temperature sensor is one of the most common 1-wire sensors.
 
Dallas ds18b20 sensor comes in two different forms, with one looking like a transistor with three legs. 
The other form, is the waterproof version where the sensor is inside a metal tube at the end of a cable.

you need:

* A Dallas DS18B20 (either version)
* 4.7k ohm Resistor
* cables/jumper wires

## Parameters

The environment variable DS18B20_PARAMS needs to be set, for example

```sh
export DS18B20_PARAMS="{ 'rate':10, 'mqttbrokerip':'10.144.176.154', 'mqttbrokerport':'1883', 'mqttchannel':'temperaturehumidity' }"
```

An example parameter set is in the file `setExampleParameters.sh`

The parameters are:

   * rate:  the rate of samples in seconds, a good value for testing is about 5
   * mqttbrokerip: MQTT broker IP-Address
   * mqttbrokerport:  usually this is 1883
   * mqttchannel: name of the channel, eg:  K226_temp
   
