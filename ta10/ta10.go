// Attestation Engine A10
// Golang version v0.1
// The main package starts the various interfaces: REST, MQTT and links to the database system
package main

import (
	"fmt"
	"runtime"
	"flag"

	"ta10/common"

	"github.com/labstack/echo/v4"
	"github.com/labstack/echo/v4/middleware"

	"ta10/sys"
	"ta10/uefi"
	"ta10/ima"
	"ta10/tpm2"	
)

// Version number
const VERSION string = "v0.1"
var BUILD string = "not set"
var RUNSESSION string = utilities.MakeID()

const PREFIX=""

var flagSYS = *flag.Bool("sys", true, "Expose the sys attestation API")
var flagTPM = *flag.Bool("tpm", true, "Expose the tpm attesation API")
var flagUEFI = *flag.Bool("uefi", true, "Expose the uefi attestation API")
var flagIMA = *flag.Bool("ima", true, "Expose the ima attestation API")
var flagTXT = *flag.Bool("txt", true, "Expose the txt attestation API")

var flagPort = flag.String("port", "8530", "Run the TA on the given port. Defaults to 8530")




// Provides the standard welcome message to stdout.
func welcomeMessage() {
	fmt.Printf("\n")
	fmt.Printf("+========================================================================================\n")
	fmt.Printf("|  TA10 version - Starting\n",)
	fmt.Printf("|   + %v O/S on %v\n",runtime.GOOS,runtime.GOARCH)
	fmt.Printf("|   + version %v, build %v\n",VERSION,BUILD)	
	fmt.Printf("|   + session identifier is %v\n",RUNSESSION)
	fmt.Printf("|  (C)2023 Nokia\n")	
	fmt.Printf("+========================================================================================\n\n")
}

func exitMessage() {
	fmt.Printf("\n")
	fmt.Printf("+========================================================================================\n")
	fmt.Printf("|  TA10 version - Exiting\n",)
	fmt.Printf("|   + session identifier was %v\n",RUNSESSION)
	fmt.Printf("|  (C)2023 CeffylOpi\n")	
	fmt.Printf("+========================================================================================\n\n")
}

// This function initialises the system by calling the configuration system to read the configuration
func init() {
	flag.Parse()
}


// These configure the rest API


func startRESTInterface(sys,tpm,uef,ima,txt bool, p *string ) {
    router := echo.New()
    router.HideBanner = true
   
    //not necessary, but I will keep this here because this is now my example of how to use middlewares
    //in echo, plus the import declaration above
    //
    // Of the two below, the gzip is the only useful one. The BodyDump was used for debugging
    //
    //router.Use(middleware.BodyDump(func(c echo.Context,reqBody,resBody []byte) {} ))    
    router.Use(middleware.GzipWithConfig(middleware.GzipConfig{ Level: 5,}))

    if sys == true {
    	setupSYSendpoints(router)    
    }
    if uef == true {
    	setupUEFIendpoints(router)    
    } 
 	 if ima == true {
    	setupIMAendpoints(router)    
    } 

    if tpm == true {
    	setupTPM2endpoints(router)    
    }    
    if ima == true {
    	setupIMAendpoints(router)    
    } 
/*    if txt == true {
    	setupTXTendpoints(router)    
    }	    
*/

	//get configuration data
	port := ":"+ *p
	//crt := configuration.ConfigData.Rest.Crt 
	//key:= configuration.ConfigData.Rest.Key 
	usehttp := true

	//start the server
	if usehttp == true{ 
		router.Logger.Fatal(router.Start(string(port))) 

	} else {
		//router.Logger.Fatal(router.StartTLS(port,crt,key))	
	}
}

 
func setupSYSendpoints(router *echo.Echo) {
	router.POST(PREFIX+"/sys/info", sys.Sysinfo)
}

func setupUEFIendpoints(router *echo.Echo) {
	router.POST(PREFIX+"/uefi/eventlog", uefi.Eventlog)
}

func setupIMAendpoints(router *echo.Echo) {
	router.POST(PREFIX+"/ima/asciilog", ima.ASCIILog)
}

func setupTPM2endpoints(router *echo.Echo) {
	router.POST(PREFIX+"/tpm2/pcrs", tpm2.PCRs)
	router.POST(PREFIX+"/tpm2/quote", tpm2.Quote)
}





// This starts everything...here we "go" :-)
func main() {
	welcomeMessage()
	startRESTInterface(flagSYS, flagTPM, flagUEFI, flagIMA, flagTXT, flagPort )
	exitMessage()
}
