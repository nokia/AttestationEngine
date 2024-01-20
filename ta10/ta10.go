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






// Provides the standard welcome message to stdout.
func welcomeMessage(unsafe bool) {
	fmt.Printf("\n")
	fmt.Printf("+========================================================================================\n")
	fmt.Printf("|  TA10 version - Starting\n",)
	fmt.Printf("|   + %v O/S on %v\n",runtime.GOOS,runtime.GOARCH)
	fmt.Printf("|   + version %v, build %v\n",VERSION,BUILD)	
	fmt.Printf("|   + session identifier is %v\n",RUNSESSION)
	fmt.Printf("|   + unsafe mode? %v\n",unsafe)
	fmt.Printf("+========================================================================================\n\n")
}

func exitMessage() {
	fmt.Printf("\n")
	fmt.Printf("+========================================================================================\n")
	fmt.Printf("|  TA10 version - Exiting\n",)
	fmt.Printf("|   + session identifier was %v\n",RUNSESSION)
	fmt.Printf("+========================================================================================\n\n")
}

func checkUnsafeMode(unsafe bool) {
	if unsafe==true {
		utilities.SetUnsafeMode()

		fmt.Printf("\n")
		fmt.Printf("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n")
		fmt.Printf("TA10 is running in UNSAFE file access mode.  Unsafe is set to %v\n",utilities.IsUnsafe())
		fmt.Printf("Requests for log files, eg: UEFI, IMA, that supply a non default location will happily read that file\n")
		fmt.Printf("This is a HUGE security issue. YOU HAVE BEEN WARNED\n")
		fmt.Printf("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n")		

	}
}


// This function initialises the system by calling the configuration system to read the configuration
func initialise() {
	flag.Parse()
}


// These configure the rest API


func startRESTInterface(sys,tpm,uefi,ima,txt bool, p *string ) {
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
    if uefi == true {
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
	flagSYS := flag.Bool("sys", true, "Expose the sys attestation API")
 	flagTPM := flag.Bool("tpm", true, "Expose the tpm attesation API")
 	flagUEFI := flag.Bool("uefi", true, "Expose the uefi attestation API")
 	flagIMA := flag.Bool("ima", true, "Expose the ima attestation API")
 	flagTXT := flag.Bool("txt", true, "Expose the txt attestation API")

 	flagUNSAFEFILEACCESS := flag.Bool("unsafe", false, "Allow caller to request ANY file instead of the default UEFI and IMA locations. THIS IS UNSAFE!")

 	flagPort := flag.String("port", "8530", "Run the TA on the given port. Defaults to 8530")	

 	flag.Parse()

 	fmt.Printf("\nsys %v, port %v , unsafe %v\n", flagSYS, flagPort, flagUNSAFEFILEACCESS)

	welcomeMessage(*flagUNSAFEFILEACCESS)
	checkUnsafeMode(*flagUNSAFEFILEACCESS)

	startRESTInterface(*flagSYS, *flagTPM, *flagUEFI, *flagIMA, *flagTXT, flagPort  )
	exitMessage()
}
