package bps

import(
	"net/http"
	"fmt"

	"github.com/labstack/echo/v4"
	"github.com/labstack/echo/v4/middleware"

    "a10/configuration"
    "a10/logging"
)

const PREFIX=""
//const PREFIX="/v3"

func StartBPS() {
    router := echo.New()

    router.HideBanner = true

    //not necessary, but I will keep this here because this is now my example of how to use middlewares
    //in echo, plus the import declaration above
    router.Use(middleware.GzipWithConfig(middleware.GzipConfig{ Level: 5,}))
    //router.Use(middleware.Logger())

    //setup endpoints
  
	setupStatusEndpoints(router)

	/*
	setUpOperationEndpoints(router)
	setupAuxillaryOperationEndpoints(router)
	setupAttestationEndpoints(router)	
	setUpLoggingEndpoints(router)
	*/

	//get configuration data
	port := ":"+configuration.ConfigData.BPS.Port 
	crt := configuration.ConfigData.BPS.Crt 
	key:= configuration.ConfigData.BPS.Key 
	usehttp := configuration.ConfigData.BPS.UseHTTP 
	path := configuration.ConfigData.BPS.Path


	// Initialisation

	err := LoadFiles(path)

	fmt.Printf("Errors %w\n",err)
	fmt.Printf("Loaded %v \n",len(CollectionsDB))

	//start the server
	if usehttp == true{ 
		logging.MakeLogEntry("SYS","startup",configuration.ConfigData.System.Name,"BPS","BPS API HTTP mode starting.")		
		router.Logger.Fatal(router.Start(port)) 

	} else {
		logging.MakeLogEntry("SYS","startup",configuration.ConfigData.System.Name,"BPS","BPS API HTTPS mode starting.")		
		router.Logger.Fatal(router.StartTLS(port,crt,key))
	
	}
}




func setupStatusEndpoints(router *echo.Echo) {
	router.GET(PREFIX+"/", homepage)
	router.GET(PREFIX+"/config", config)
}




type bpsHealthData struct {
	Name string 		   `json:"name"`
	WelcomeMessage string  `json:"welcomeMessage"`
	Prefix string          `json:"prefix"`
}

func homepage(c echo.Context) error {
	h := bpsHealthData{ "Nokia Attestation Engine Base Policy System", "Croeso! Tere! Dia Dhuit!", PREFIX }
	return c.JSON(http.StatusOK, h)
}

func config(c echo.Context) error {
	return c.JSON(http.StatusOK, configuration.ConfigData.BPS)
}