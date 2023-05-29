package webui

import(
	"net/http"
	"github.com/labstack/echo/v4"

    "a10/operations"
    "a10/structures"
)

type claimsummary struct {
        ItemID string                            
        BodyType string                       
}

type resultsummary struct{
        ItemID string                           
        Result structures.ResultValue                      
 		RuleName string                         
        VerifiedAt structures.Timestamp                   
}


func showSessions(c echo.Context) error {
	s,_ := operations.GetSessionsAll()

	return c.Render(http.StatusOK, "sessions.html",s)
}




type claimanalytics struct {
	Valid int
	Errs int
}

type resultanalytics struct {
	Pass int
	Fail int
	Verifyfail int
	Verifycallattempt int
	Noresult int 
	Missineexpectedvalue int
	Rulecallfailure int
	Unsetresultvalue int
}

type sessionsummary struct {
	S     structures.Session
	CS    []claimsummary
	RS    []resultsummary
	CA    claimanalytics
	RA    resultanalytics
}


func showSession(c echo.Context) error {
	s,_ := operations.GetSessionByItemID(c.Param("itemid"))

	cs := make([]claimsummary,0)
	for _,i := range s.ClaimList {
		cl,_ := operations.GetClaimByItemID(i)
		cs = append(cs, claimsummary{ cl.ItemID, cl.BodyType })
	}

	rs := make([]resultsummary,0)
	for _,i := range s.ResultList {
		rl,_ := operations.GetResultByItemID(i)		
		rs = append(rs, resultsummary{ rl.ItemID, rl.Result, rl.RuleName, rl.VerifiedAt })
	}	

	sstr := sessionsummary{ s, cs, rs, genclaimanalytics(cs), genresultanalytics(rs) }
	return c.Render(http.StatusOK, "session.html", sstr)
}


func genclaimanalytics( cs []claimsummary ) claimanalytics{
	var valid int = 0
	var errs int = 0

	for _,c := range cs {
		if c.BodyType == "*ERROR" {
			errs ++
		} else {
			valid ++
		}
	}
	
	return claimanalytics{ valid, errs }
}

func genresultanalytics( rs []resultsummary ) resultanalytics{
	var pass int = 0
	var fail int = 0
	var verifyfail int = 0
	var verifycallattempt int = 0
	var noresult int = 0
	var missineexpectedvalue int = 0
	var rulecallfailure int = 0
	var unsetresultvalue int = 0
	
	for _,r := range rs {
		switch r.Result {
			case structures.Success : 
			pass ++
			case structures.Fail : 
			fail ++
			case structures.VerifyCallFailure : 
			verifyfail ++
			case structures.VerifyClaimErrorAttempt:
			verifycallattempt ++
			case structures.NoResult : 
			noresult ++	
			case structures.MissingExpectedValue :
			missineexpectedvalue ++
			case structures.RuleCallFailure :
			rulecallfailure ++
			case structures.UnsetResultValue :
			unsetresultvalue ++
		}
		
	}

	return resultanalytics{ pass,fail,verifyfail,verifycallattempt,noresult,missineexpectedvalue,rulecallfailure,unsetresultvalue }
}

      