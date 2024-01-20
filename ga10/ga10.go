// Attestation Engine A10
// Golang version v0.1
// The main package starts the various interfaces: REST, MQTT and links to the database system
package main

import (
	"flag"
	"fmt"
	"runtime"
	"sync"

	"a10/configuration"
	"a10/datalayer"
	"a10/logging"
	"a10/protocols"
	"a10/rules"
	"a10/utilities"

	"a10/services/restapi"
	"a10/services/webui"
	"a10/services/x3270"

)

// Version number
const VERSION string = "v1.0rc1"

// the BUILD value can be set during compilation.
var BUILD string = "not set"

// and we generate a unique identifier for this whole run session
var RUNSESSION string = utilities.MakeID()

// Command line flags
var flagREST = flag.Bool("startREST", true, "Start the REST API, defaults to true")
var flagWEB = flag.Bool("startWebUI", true, "Start the HTML Web UI, defaults to true")
var flagX3270 = flag.Bool("startx3270", true, "Start the X3270 UI, defaults to true")

var configFile = flag.String("config", "./config.yaml", "Location and name of the configuration file")


// Provides the standard welcome message to stdout.
func welcomeMessage() {
	fmt.Printf("\n")
	fmt.Printf("+========================================================================================\n")
	fmt.Printf("|  GA10 version\n")
	fmt.Printf("|   + %v O/S on %v\n", runtime.GOOS, runtime.GOARCH)
	fmt.Printf("|   + version %v, build %v\n", VERSION, BUILD)
	fmt.Printf("|   + runing with name %v\n", configuration.ConfigData.System.Name)
	fmt.Printf("|   + session identifier is %v\n", RUNSESSION)
	fmt.Printf("+========================================================================================\n\n")
}




// This starts everything...here we "go" <- great pun! :-)
func main() {
	// we need to see what is on the command line and process the configuration file
	// If this fails, we panic
	flag.Parse()
	configuration.SetupConfiguration(*configFile)

	// now we know where things are, we can initialise the datalayer, ie: database, messaging etc
	// if this fails, we panic
	datalayer.InitialiseDatalayer()

	// Ok, we're up...let's log this.
	msg := fmt.Sprintf("Starting: %v, build %v, OS %v, ARCH %v", VERSION, BUILD, runtime.GOOS, runtime.GOARCH)
	logging.MakeLogEntry("SYS", "startup/INIT", RUNSESSION, configuration.ConfigData.System.Name, msg)

	welcomeMessage()

	// initialise the internal parts of the system, ie: rules and protocols. 
	// If the datalayer have come up properly, but some other external error has occured, eg: authorisation etc, 
	// then we will get a panic from these below.

	protocols.RegisterProtocols()
	rules.RegisterRules()

	// and if this has gone well...

	msg = fmt.Sprintf("DB,MQTT,Rules initialised. Starting services: web %v, rest %v, x3720 %v", *flagWEB, *flagREST, *flagX3270)
	logging.MakeLogEntry("SYS", "startup", RUNSESSION, configuration.ConfigData.System.Name, msg)

	// Start (or not) the various internal services
	// As these run as threads, we put them in a wait group
	// Need to implement a proper graceful shutdown mechanism
	//
	// If any of these internal services fail to start, then the system will panic

	var wg sync.WaitGroup

	if *flagX3270 == true {
		wg.Add(1)
		go x3270.StartX3270()
	}
	if *flagREST == true {
		wg.Add(1)
		go restapi.StartRESTInterface()
	}
	if *flagWEB == true {
		wg.Add(1)
		go webui.StartWebUI()
	}


	wg.Wait()
	// ...and exit here (if graceful!) which does not happen in the current version

	logging.MakeLogEntry("SYS", "shutdown", configuration.ConfigData.System.Name, "GA10 "+VERSION, "Clean shutdown sequence completed. System is now stopped")
	fmt.Println("+=== Exiting. ================================================")

}
