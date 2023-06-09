package webui

import(
	"fmt"
	"os"
	"net/http"

	"github.com/labstack/echo/v4"

	"a10/configuration"
	"a10/operations"
)

type homepagestructure struct {
	Nes  	int 
	Nps		int
	Nevs	int
	Ncs		int
	Nrs		int
	Nprs    int 
	Nhs     int 
	Nses    int64
	Nrus    int 
	Nlog	int64
	Szlog	int64
	Cfg     *configuration.ConfigurationStruct
}

func homepage(c echo.Context) error {
	var hps homepagestructure

	es,_ := operations.GetElements()
	ps,_ := operations.GetPolicies()
	evs,_ := operations.GetExpectedValues()
	cs,_ := operations.GetClaims()
	rs,_ := operations.GetResults()
	nprs := operations.GetProtocols()
	nhs,_ := operations.GetOpaqueObjects()
	nrus := operations.GetRules()

	hps.Nes = len(es)
	hps.Nps = len(ps)
	hps.Nevs = len(evs)
	hps.Ncs = len(cs)
	hps.Nrs = len(rs)

	hps.Nprs = len(nprs)
	hps.Nhs = len(nhs)	
	hps.Nrus = len(nrus)

	hps.Nlog = operations.CountLogEntries()
	hps.Nses = operations.CountSessions()

	lsz,lerr := os.Stat(configuration.ConfigData.Logging.LogFileLocation)
	if lerr != nil {
		hps.Szlog = -1
	} else {
		hps.Szlog = lsz.Size()
	}

	hps.Cfg = configuration.ConfigData

	fmt.Printf("hps is %v\n",hps)

	return c.Render(http.StatusOK, "home.html",hps)
}



func helppage(c echo.Context) error {
	return c.Render(http.StatusOK, "help.html", nil)
}

func aboutpage(c echo.Context) error {
	return c.Render(http.StatusOK, "about.html", nil)
}