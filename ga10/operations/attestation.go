package operations

import(
	"fmt"

	"a10/structures"
	"a10/utilities"	
)

type hashablePartClaim struct {
        BodyType string                                
        Header structures.ClaimHeader          
        Body map[string]interface{}            
}
 


//This function calls the attestation mechanism that eventually calls the TA on some client
// ItemIDs for Element, Policy and String are provided in eid, pid and sid
// A map of additional parameters in aps
func Attest(element structures.Element, policy structures.Policy, session structures.Session, aps map[string]interface{}) (string, error) {

	var body map[string]interface{} =  make(map[string]interface{})

	/* 1. create a protocol object
	   2. set the timer start
	   3. dispatch control over to the protocol object
	   4. stop timer
	   5. create claim
	   6. store claim
	   7. add claim to the session
	   8. return
	 */

	// Step 1 ******************************************************

	protocol := element.Protocol
	protocolObject,_ := GetProtocol(protocol)

	// TODO: Need to check that the protocol exists here otherwise the acall below fails with a panic

	// Step 2 ******************************************************

	claimTimerStart := utilities.MakeTimestamp()

	// Step 3 ******************************************************

	aCALL := protocolObject.CallFunction
	returnedBody, ips, bodytype := aCALL( element, policy, session, aps )

	if bodytype == "*ERROR" {
		body["ERROR"] = returnedBody
	} else {
		body = returnedBody
	}

	// Step 4 ******************************************************

	claimTimerFinish := utilities.MakeTimestamp()

	// Step 5 ******************************************************
        // NB: we have body and bodytype from above

	timing := structures.Timing{ claimTimerStart, claimTimerFinish }
	header := structures.ClaimHeader{element, policy, session, timing, aps, ips}
	footer,_ := hashAndSignClaim(hashablePartClaim{ bodytype, header, body })

	c := structures.Claim{ "", bodytype, header, body, footer }

	// Step 6
	sid := session.ItemID
	
	cid,err := AddClaim( c )   // cid a string with the claim ID
    	if err != nil {
		return "",fmt.Errorf("Error adding claim, session %v might still be open: %w",sid,err)
	} 

	fmt.Printf("Added claim %v\n",cid)

	// if AddClaim fails, we have a bigger problem, see above note
	// If there is an error here, then we need a new claim type of "internal error"
	// but of course, if we can't add a claim then we can not add an internal error claim ---just dump something on the log!
	// plus specific logging to handle this - might denote on seriously fscked TA at the other end?
	// This is kind of the same thing that happens if there protocol object ins't found

	// Step 7

	sderr := AddClaimToSession(sid,cid)
	if sderr != nil {
		return "",fmt.Errorf("Error adding claim %v to session %v. Claim added, session may be still open: %w",cid,sid,err)
	} 

	// Step 8

	return cid,nil
}


