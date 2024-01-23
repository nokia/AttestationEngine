package configuration

import (
	"fmt"
	"io/ioutil"

	"gopkg.in/yaml.v3"
)

// The configuration structure also gives the format of the YAML config file
//
// For example
//#Some general naming
//system:
//  name: ASVR_GO_1                             -- name of the ASVR system. Can be anything sensible
//
//#MongoDB Configuration
//database:
//  connection: mongodb://192.168.1.203:27017   -- connection string for the mongodb
//  name: test1                                 -- name of the database to use
//
//#MQTT Configuration
//messaging:
//  broker: 192.168.1.203                       -- IP address of the MQTT broker
//  port: 1883                                  -- Port to use, typically 1883
//
//#REST Interface Configuration
//rest:
//  port: 8520                                  -- Port to use for the REST API, default is 8520
//  crt: temporary.crt                          -- File containing the certificate to use for the HTTPS server
//  key: temporary.key                          -- File containing the private key for the HTTPS server
//  usehttp: true                               -- Use HTTP (true) instead of HTTPS. Default is false

type ConfigurationStruct struct {
	System struct {
		Name string
	}
	Database struct {
		Connection string
		Name       string
	}
	Messaging struct {
		Broker   string
		Port     uint16
		ClientID string
	}
	Rest struct {
		Port    string
		Crt     string
		Key     string
		UseHTTP bool
	}
	Web struct {
		Port    string
		Crt     string
		Key     string
		UseHTTP bool
	}
	X3270 struct {
		Port string
	}
	Logging struct {
		LogFileLocation      string
		SessionUpdateLogging bool
	}
	Keylime struct {
		ApiUrl string
	}
}

// The exported variable for accessing the configuration structure
var ConfigData *ConfigurationStruct

// The function that reads the configuraiton file and sets up the configuration structure
//
// If the file is unavailable or in unparsable then this function will panic and exit.
// There is no need to continue if the configuration is borked.
func SetupConfiguration(f string) {
	fmt.Println("GA10: Configuration file location: ", f)

	configFile, err := ioutil.ReadFile(f)
	if err != nil {
		panic(fmt.Sprintf("Configuration file missing. Exiting with error ", err))
	}

	err = yaml.Unmarshal(configFile, &ConfigData)
	if err != nil {
		panic(fmt.Sprintf("Unable to parse configuration file. Exiting with error ", err))
	}
}
