package operations

import(
	"fmt"
	"reflect"

	"a10/structures"
	"a10/utilities"

)

type hashablePartResult struct {
        ClaimID string                         
        ClaimFooter structures.ClaimFooter                       
        SessionID structures.Session    
        ElementID string                   
        ExpectedValue structures.ExpectedValue             
        Parameters map[string]interface{}      
        Message string                         
        RuleName string                        
        VerifiedAt structures.Timestamp                  
        Result structures.ResultValue                          
}

func checkClaimError(claim structures.Claim) (structures.ResultValue, string) {
	if isClaimError(claim) == true {   // function from claims.go
		return structures.VerifyClaimErrorAttempt,fmt.Sprintf("Attempt to verify a claim error: %v",claim.ItemID)
	} else {
		return structures.UnsetResultValue,""
	}
}

func checkRuleObjectExists(rule structures.Rule) (structures.ResultValue, string) {
	if rule.CallFunction == nil {    
		return structures.RuleCallFailure,fmt.Sprintf("Requested rule %v has no call function",rule.Name)
	} else {
		return structures.UnsetResultValue,""
	}
}

func getEV(claim structures.Claim, rule structures.Rule) (structures.ExpectedValue, error) {
	fmt.Println("Step 2 - getting EV")


	// if the rule does need an EV, then we get it and return whatever comes back
	// if err is not nil then there was some error, usualy no EV for that E,P pair
	// but it could be something worse, eg: datalayer failure, but this is unlikely

	fmt.Println("  Rule needs EV is %v",rule.NeedsEV)

	if rule.NeedsEV == true {
		e := claim.Header.Element.ItemID
		p := claim.Header.Policy.ItemID
		
		fmt.Println("    e = %v",claim.Header.Element.ItemID)
		fmt.Println("    p = %v",claim.Header.Policy.ItemID)
		
		ev,err := GetExpectedValueByElementAndPolicy(e,p)

		fmt.Println("  GET EV error=%v,  ev=%v",err,ev)

		return ev,err 
	}

	// didn't need and EV, so nothing to report
	return structures.ExpectedValue{},nil
}

func Verify(claim structures.Claim, rule structures.Rule, session structures.Session, rps map[string]interface{}) (string, structures.ResultValue, error) {

	// these are default values and shouldn't occur in reality...
	var returnedRV structures.ResultValue = structures.UnsetResultValue	
	var returnedMSG string = "Verify not processed"     

	// required variables
	var ev structures.ExpectedValue 
	var eid string


	// Start ******************************************************
	// 0
	returnedRV,returnedMSG = checkClaimError(claim)

	eid = claim.Header.Element.ItemID
	fmt.Printf("Verify eid %v\n",eid)

	// if we are still unset then we proceed with the verification
	if returnedRV == structures.UnsetResultValue {
		fmt.Println("dealing with the ev")
		ev, err := getEV(claim,rule)
		if err != nil {
			fmt.Println("dealing with the ev")
	
			returnedRV = structures.MissingExpectedValue
			returnedMSG = fmt.Sprintf("Rule %v requries an expected value for e,p pair %v and %v and one was not found: %w",rule.Name,claim.Header.Element.ItemID,claim.Header.Policy.ItemID,err.Error())
		} else {

			// Now
			fmt.Printf("Calling %v with %v %v %v \n",rule.Name, ev, session, rps)
			fmt.Printf("Calling %v with %v %v %v \n",reflect.TypeOf(rule.Name),  reflect.TypeOf(ev), reflect.TypeOf(session), reflect.TypeOf(rps))

			aCALL := rule.CallFunction
			returnedRV,returnedMSG,err = aCALL( claim, rule.Name, ev, session, rps )
			if err != nil {
		
				fmt.Println("Step 3 -err")

				returnedRV = structures.VerifyCallFailure
				returnedMSG = fmt.Sprintf("Rule %v call failed with message %v and error %w",rule.Name,returnedMSG,err.Error())   // this should be the returnedMSG form the line above
			}
		}

	}   


	// Step 4 ******************************************************
	fmt.Println("Step 4")

	verifiedAt := utilities.MakeTimestamp()

	// Step 5 ******************************************************

	footer,_ := hashAndSignResult(hashablePartResult{ claim.ItemID, claim.Footer, session, eid, ev, rps, returnedMSG, rule.Name, verifiedAt, returnedRV })
	r := structures.Result{ "", claim.ItemID, claim.Footer, session, eid, ev, rps, returnedMSG, rule.Name, verifiedAt, returnedRV, footer }

	// Step 6 ******************************************************
	// This actually returns and error back which should be handled by the caller

	sid := session.ItemID
	
	rid,err := AddResult( r )   // cid a string with the claim ID
    	if err != nil {
		return "",structures.NoResult,fmt.Errorf("Error adding result, session %v might still be open: %w",sid,err)
	} 

	// Step 7 ******************************************************
	// This actually returns and error back which should be handled by the caller

	sderr := AddResultToSession(sid,rid)
	if sderr != nil {
		return rid,returnedRV,fmt.Errorf("Error adding result %v to session %v. Claim added, session may be still open: %w",rid,sid,err)
	} 

	// Step 8 ******************************************************
	
	return rid,returnedRV,nil
}




