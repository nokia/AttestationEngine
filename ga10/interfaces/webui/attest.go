package webui

import(
	"fmt"
	"net/http"

	"github.com/labstack/echo/v4"

    "a10/operations"
    "a10/structures"
)


type atteststr struct {
	ES    []structures.Element
	PS    []structures.Policy
	RS    []structures.Rule
}

func showAttest(c echo.Context) error {
	es,_ := operations.GetElementsAll()
	ps,_ := operations.GetPoliciesAll()
	rs:= operations.GetRules()

	as := atteststr{ es,ps,rs }

	return c.Render(http.StatusOK, "attest.html",as)
}


type attestrequest struct {   
	Eid     string      			`form:"eid"`
	Pid     string					`form:"pid"`
	Rn     []string					`form:"rn"`
	Av    string					`form:"av"`
	Pps   string 					`form:"pps"`
	Rps   string					`form:"rps"`
	Msg	    string					`form:"msg"`
}

type multipleresultsummary struct {
	ItemID string
	RuleName string
	Message string
	Result structures.ResultValue
	EVID	string
}

func processAttest(c echo.Context) error {
	var attreq attestrequest

	err := c.Bind(&attreq); if err != nil {
		fmt.Printf("Error in binding %v",err.Error())
	}

	fmt.Printf("ATTREQ %v\n",attreq)
	// Get the objects
	e,_ := operations.GetElementByItemID(attreq.Eid)
	p,_ := operations.GetPolicyByItemID(attreq.Pid)

	// Open a session
	sid,_ := operations.OpenSession(attreq.Msg)
	s,_ := operations.GetSessionByItemID(sid)

	// Call attest
	// https://stackoverflow.com/questions/47400358/go-converting-json-string-to-mapstringinterface
	 // should be pps here, but we need to covert the pps from string to map[string]interface{}  
	empty := make(map[string]interface{})

	// Call attest
 	cid,err := operations.Attest(e, p, s, empty)  
 	
 	fmt.Printf("return after attests %v %v\n",err,cid)

 	//Check if we are attesting only
 	// if so, close the session and redirect to the claim page
 	if attreq.Av=="AO" {
 		 // Close the session now and return - we are not verifying
 		_ = operations.CloseSession(sid)
 		
 		return c.Redirect(http.StatusSeeOther, "/claim/"+cid)
 	}

 	// And now we continue with the verification
 	// first we need the claim just generated

 	cl,_ := operations.GetClaimByItemID(cid)

 	// And perform the verifications  <--- note the plural
 	// This bit needs to be parallelised

 	for _,rn := range attreq.Rn {
  		r,_ := operations.GetRule(rn)
 		fmt.Printf("** verify %v\n",r)

 		rid,rv,err := operations.Verify(cl,r,s,empty)		// rps conversion goes here
	 	fmt.Printf("results of verify %v - %v - %v\n",err,rv,rid)

 	}
 	// end of parallelism
 
 	
 	// Close the session 
 	_ = operations.CloseSession(sid)

 	// Now we render the multiple (or even just one!) results

 	return c.Redirect(http.StatusSeeOther, "/session/"+sid)
}