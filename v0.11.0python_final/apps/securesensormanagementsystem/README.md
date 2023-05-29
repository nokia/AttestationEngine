# Intro ([LINK TO REPO](https://github.com/teemvil/iot.git))

This is repository of a innovation project done for Nokia. The idea is to build a framework which allows rapid development of different kinds of sensor management systems. The sensor could ultimately be anything.

The data aquisiton functionality is being abstracted so that the system doesn't need to know what kind of a sensor has been attached to it.

The system handles all communication via MQTT messages.

Below are the installation and use instructions. For detailed documentation about the structure etc. please see the documentation folder.

# Installation

The system consists of three parts: 1) Device and Sensor part 2) ManagementAttestor part 3) Management UI part. Each part can be run on separete devices. In fact, it is NOT recommended to run sensors and ManagementAttestor on the same device. The only requirement is that each part is using the same MQTT broker, so that they can communicate with each other.

## 1. Installing and running the Device and Sensor part

1.  Installing the necessary files

    a) Go to the folder /opt/ on the terminal

    b) Clone the repository using git: `sudo git clone https://github.com/teemvil/iot.git`. This downloads all the necessary library files.

    c) Navigate your way to the install folder at `iot/secure_sensor_management_system/install` and run the installation script:

    ```
    sudo python3 install.py
    ```

    The install script creates iot.devices.service and iot.sensors.service files to etc/systemd/system.

    The install script also creates two config files, `client_config.json` and `device_config.json`, to the folder `/etc/iotDevice/`. These config files are used as MQTT client configuration and as a specific device configuration. The device configuration file should contain the itemid of the pi on which the scripts are running on. MAKE SURE TO CHECK THAT THE MQTT CLIENT INFO AND THE ITEMID OF THE PI ARE CORRECT IN THESE FILES ONCE THEY ARE CREATED!!

    d) Enable the devices service using systemd:

    ```
    sudo systemctl enable iot.devices.service
    ```

    e) Start the devices.service using systemd:

    ```
    sudo systemctl start iot.devices.service
    ```

2.  Create sensor/implementations

    a) To install IoTLibrary as a package, go to the folder `/opt/iot/secure_sensor_management_system/` and run the command `sudo pip3 install .`

    b) Create a new folder for the sensor under `/opt/iot/secure_sensor_management_system/`

    c) Create new sensor script. Most important thing is to inherit the BasicSensor from SensorManagementLibrary.

    ```python
    from SensorManagementLibrary.BasicSensor import BasicSensor
    ```

    The easiest way to do this is to use a template sensor file from `opt/iot/secure_sensor_management_system/ExampleSensor/`

    d) Create configuration file for the sensor and name it as `sensor_config.json`. This file should be stored in the same folder as your sensor script. The file should include three fields like this:

    ```json
    {
      "name": "EXAMPLE RNGsensor",
      "frequency": 1,
      "prefix": "EXAMPLE"
    }
    ```

    The fields denote:

    ```
    name: The name of the sensor
    frequency: The interval time between sending data values, in seconds
    prefix: The sensor's indentifying code for the MQTT data channel
    ```

    Fill the values in according to your sensor's needs.

3.  Run the newly created script:

    You can run the sensor straight from the terminal thusly:

    ```bash
    python3 YourNewSensor.py
    ```

    To make the sensor start at device startup, replace the sensor's path with your sensor's path in the sensor service file `iot.sensors.service` in the folder `/etc/systemd/system/`
    Change the line where it asks for ExecStart:

    ```
        ExecStart=python3 /opt/iot/secure_sensor_management_system/ExampleSensor/RNGSensor.py
    ```

    Finally enable the sensors service using systemd:

    ```
    sudo systemctl enable iot.sensors.service
    ```

    Then Start the sensors service using systemd:

    ```
    sudo systemctl start iot.sensors.service

    ```

    If you have another sensor, that you want to run at the same time, you can create a new service file at `/etc/systemd/system/`. Copy the contents of the `iot.sensors.service` and then change the ExecStart path:

    ```
    ExecStart=python3 /opt/iot/secure_sensor_management_system/YourSensor/YourSensor.py
    ```

    Then enable and start that new service.

## 2. Installing and running the ManagementAttestor part

1.  a) Go to the folder /opt/ on the terminal

    b) Clone the repository using git: `sudo git clone https://github.com/teemvil/iot.git`. This downloads all the necessary library files.

    c) Navigate your way to the install folder at `iot/secure_sensor_management_system/install` and run the installation script:

    ```
    sudo python3 install.py
    ```

    The script creates two config files, `client_config.json` and `device_config.json` to the folder `/etc/iotDevice/`.These are used as MQTT client configuration and as a specific device configuration. MAKE SURE TO CHECK THAT THE MQTT CLIENT INFO IS CORRECT IN THE client_config.json FILE ONCE IT IS CREATED!!

    d) To install IoTLibrary as a package, go to the folder `/opt/iot/secure_sensor_management_system/` and run the command `sudo pip3 install .`

    e) Navigate your way to the ManagementAttestor folder at `/opt/iot/secure_sensor_management_system/ManagementAttestor/` and add the correct ip and port addresses the file `manager_config.json`. The ip and port that you want to put there are the ones used by the attestation engine, which you should already have running somewhere.

    f) You can now run the Manager with the command `sudo python3 Manager.py`

## 3. Installing and running the Management UI part

1.  a) Go to the folder /opt/ on the terminal

    b) Clone the repository using git: `sudo git clone https://github.com/teemvil/iot.git`. This downloads all the necessary library files.

    c) Check the MQTT configuration by going to the folder `/opt/iot/website/server`, where you will find a file named `server_config.json`. MAKE SURE THIS FILE HAS THE CORRECT MQTT CLIENT INFO!!

    d) Run `npm install`.

    e) Then navigate your way back to the website folder at `/opt/iot/website/` and start the server using the command `npm start`

    IF THIS DOES NOT WORK, MAKE SURE YOU HAVE NPM INSTALLED ON YOUR SYSTEM. You can install it with the command `sudo apt install npm nodejs`


    f) You should now be able to access the management UI by going to the address `http://localhost:3000`, if you are running the server on localhost. If the server is running on a separate device with a specific ip address then use `http://<ip_address>:3000`. If you want to change the port, go to `/opt/iot/website/server/server_config.json` and change the RESTport value.


By: [Joonas Soininen](https://github.com/Joonas88), [Jani Peltonen](https://github.com/Reldin), [Teemu Viljanen](https://github.com/teemvil) and [Matias Vainio](https://github.com/matiasvainio)