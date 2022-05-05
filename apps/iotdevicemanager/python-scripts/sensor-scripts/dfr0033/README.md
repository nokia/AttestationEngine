# dfr0033 magnetic sensor

## Architecture

DFR0033 is a simple magnetic sensor that knows whether there is a magnetic object nearby or not. And it correctly tells you through digital output.

 * Supply Voltage: 3.3V to 5V
 * Size:22x30mm

## Parameters

The environment variable DFR0033_PARAMS needs to be set, for example

```sh
export DFR0033_PARAMS="{ 'rate':10, 'mqttbrokerip':'10.144.176.154', 'mqttbrokerport':'1883', 'mqttchannel':'magnet' }"
```

An example parameter set is in the file `setExampleParameters.sh`

The parameters are:

   * rate:  the rate of samples in seconds, a good value for testing is about 5
   * mqttbrokerip: MQTT broker IP-Address
   * mqttbrokerport:  usually this is 1883
   * mqttchannel: name of the channel, eg:  K226_temp