package restapi

import(
	"net/http"

	"github.com/labstack/echo/v4"
	"github.com/labstack/echo/v4/middleware"

    "a10/configuration"
    "a10/logging"
)

const PREFIX=""
//const PREFIX="/v3"

func StartRESTInterface() {
    router := echo.New()

    router.HideBanner = true

    //not necessary, but I will keep this here because this is now my example of how to use middlewares
    //in echo, plus the import declaration above
    router.Use(middleware.GzipWithConfig(middleware.GzipConfig{ Level: 5,}))
    //router.Use(middleware.Logger())

    //setup endpoints
	setupStatusEndpoints(router)
	setUpOperationEndpoints(router)
	setupAuxillaryOperationEndpoints(router)
	setupAttestationEndpoints(router)	
	setUpLoggingEndpoints(router)

	//get configuration data
	port := ":"+configuration.ConfigData.Rest.Port 
	crt := configuration.ConfigData.Rest.Crt 
	key:= configuration.ConfigData.Rest.Key 
	usehttp := configuration.ConfigData.Rest.UseHTTP 

	//start the server
	if usehttp == true{ 
		logging.MakeLogEntry("SYS","startup",configuration.ConfigData.System.Name,"RESTAPI","REST API HTTP mode starting.")		
		router.Logger.Fatal(router.Start(port)) 

	} else {
		logging.MakeLogEntry("SYS","startup",configuration.ConfigData.System.Name,"RESTAPI","REST API HTTPS mode starting.")		
		router.Logger.Fatal(router.StartTLS(port,crt,key))
	
	}
}



func setUpOperationEndpoints(router *echo.Echo) {
	router.GET(PREFIX+"/elements", getElements)
	router.GET(PREFIX+"/element/:itemid", getElement)
	router.GET(PREFIX+"/elements/name/:name", getElementsByName)	
	router.POST(PREFIX+"/element", postElement)
	router.PUT(PREFIX+"/element", putElement)
	router.DELETE(PREFIX+"/element/:itemid", deleteElement)

	router.GET(PREFIX+"/policies", getPolicies)
	router.GET(PREFIX+"/policy/:itemid", getPolicy)
	router.GET(PREFIX+"/policies/name/:name", getPoliciesByName)	
	router.POST(PREFIX+"/policy", postPolicy)
	router.PUT(PREFIX+"/policy", putPolicy)
	router.DELETE(PREFIX+"/policy/:itemid", deletePolicy)

	router.GET(PREFIX+"/expectedValues", getExpectedValues)
	router.GET(PREFIX+"/expectedValue/:itemid", getExpectedValue)
	router.GET(PREFIX+"/expectedValues/name/:name", getExpectedValuesByName)	
	router.GET(PREFIX+"/expectedValues/element/:itemid", getExpectedValuesByElement)	
	router.GET(PREFIX+"/expectedValues/policy/:itemid", getExpectedValuesByPolicy)	
	router.GET(PREFIX+"/expectedValue/:eid/:pid", getExpectedValueByElementAndPolicy)

	router.POST(PREFIX+"/expectedValue", postExpectedValue)
	router.PUT(PREFIX+"/expectedValue", putExpectedValue)
	router.DELETE(PREFIX+"/expectedValue/:itemid", deleteExpectedValue)

	router.GET(PREFIX+"/claims", getClaims)
	router.GET(PREFIX+"/claim/:itemid", getClaim)
	router.GET(PREFIX+"/claims/element/:itemid", getClaimsByElementID)
	router.POST(PREFIX+"/claim", postClaim)

	router.GET(PREFIX+"/results", getResults)
	router.GET(PREFIX+"/result/:itemid", getResult)
	router.GET(PREFIX+"/results/element/:itemid", getResultsByElementID)
	router.POST(PREFIX+"/result", postResult)


	router.GET(PREFIX+"/sessions", getSessions)
	router.GET(PREFIX+"/session/:itemid", getSession)
	router.POST(PREFIX+"/session", postSession)
	router.DELETE(PREFIX+"/session/:itemid", deleteSession)

	router.PUT(PREFIX+"/session/:sid/claim/:cid", putSessionClaim)
	router.PUT(PREFIX+"/session/:sid/result/:rid", putSessionResult)

}

func setupAuxillaryOperationEndpoints(router *echo.Echo) {
	router.GET(PREFIX+"/protocols", getProtocols)
	router.GET(PREFIX+"/protocol/:name", getProtocol)

	router.GET(PREFIX+"/rules", getRules)
	router.GET(PREFIX+"/rule/:name", getRule)

	router.GET(PREFIX+"/opaqueobjects", getOpaqueObjects)
	router.GET(PREFIX+"/opaqueobject/:value", getOpaqueObjectByValue)	
	router.POST(PREFIX+"/opaqueobject", postOpaqueObject)
	router.PUT(PREFIX+"/opaqueobject", putOpaqueObject)
	router.DELETE(PREFIX+"/opaqueobject/:value", deleteOpaqueObject)


}

func setupAttestationEndpoints(router *echo.Echo) { 
	router.POST(PREFIX+"/attest", postAttest)	
	router.POST(PREFIX+"/verify", postVerify)	
}



func setupStatusEndpoints(router *echo.Echo) {
	router.GET(PREFIX+"/", homepage)
	router.GET(PREFIX+"/config", config)
	router.GET(PREFIX+"/health", health)
}



func setUpLoggingEndpoints(router *echo.Echo) {
	//other endpoint will be put here
	router.GET(PREFIX+"/log", getLogEntries)
	router.GET(PREFIX+"/log/since", getLogEntriesSince)	
}

type homepageData struct {
	Name string 		   `json:"name"`
	WelcomeMessage string  `json:"welcomeMessage"`
	Prefix string          `json:"prefix"`
}

func homepage(c echo.Context) error {
	h := homepageData{ "Nokia Attestation Engine", "Croeso, Tervetuola, Welcome", PREFIX }
	return c.JSON(http.StatusOK, h)
}

func config(c echo.Context) error {
	return c.JSON(http.StatusOK, configuration.ConfigData)
}