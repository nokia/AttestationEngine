package webui

import(
	"net/http"

	"github.com/labstack/echo/v4"

    "a10/operations"
    "a10/structures"
)


type resultsstr struct {
	ItemID       string
	RuleName     string
	VerifiedAt   structures.Timestamp
	Result       structures.ResultValue
	EVEName      string
	EVEID		string
	EVPName		string
	EVPID		string
	EV_Name	     string
	EV_ItemID    string
	ClaimID      string
	SessionID    string
	Message	     string
	Footer		structures.ResultFooter
}

func showResults(c echo.Context) error {

	rsstr := []resultsstr{}


	rs,_ := operations.GetResultsAll()
	for _,j := range rs {
		ev,_ := operations.GetExpectedValueByItemID(j.ExpectedValue.ItemID)
		e,_ := operations.GetElementByItemID(ev.ElementID)		
		p,_ := operations.GetPolicyByItemID(ev.PolicyID)		

		rsstr = append( rsstr, resultsstr{ j.ItemID, j.RuleName,j.VerifiedAt,j.Result,e.Name,e.ItemID,p.Name,p.ItemID,ev.Name,ev.ItemID,j.ClaimID,j.Session.ItemID,j.Message,j.Footer  } )
	}


	return c.Render(http.StatusOK, "results.html", rsstr)
}



type resultsstrext struct {
	R           resultsstr
}

func showResult(c echo.Context) error {
	r,_ := operations.GetResultByItemID(c.Param("itemid"))

	ev,_ := operations.GetExpectedValueByItemID(r.ExpectedValue.ItemID)
	e,_ := operations.GetElementByItemID(ev.ElementID)		
	p,_ := operations.GetPolicyByItemID(ev.PolicyID)	

	rsstr := resultsstr{ r.ItemID, r.RuleName, r.VerifiedAt, r.Result,e.Name,e.ItemID,p.Name,p.ItemID,ev.Name,ev.ItemID,r.ClaimID,r.Session.ItemID,r.Message,r.Footer  }
	rsstrext := resultsstrext{ rsstr }

 	return c.Render(http.StatusOK, "result.html", rsstrext)
}
