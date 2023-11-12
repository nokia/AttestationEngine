package nullprotocol

import(
	"fmt"
	"log"
	"crypto/rand"

	"a10/structures"
)

const nonceSize int = 32

func Registration() (structures.Protocol) {
	intents := []string{"*/*"}

	return structures.Protocol{"A10NULLPROTOCOL","Testing protocol, always returns a test claim",Call, intents}
}

func Call(e structures.Element, p structures.Policy, s structures.Session, cps map[string]interface{}) (map[string]interface{}, map[string]interface{}, string) {

	// Create a test body

	rtn := map[string]interface{}{
		 "foo":"bar",
		 "calling": fmt.Sprintf("with protocol %v I would send an intent to %v",e.Protocol,p.Intent),
		 "aNumber": 42,
	}
	
	nce := make([]byte, nonceSize)
	_, _ = rand.Read(nce)

	ips := map[string]interface{}{
		 "nonce":nce,
	}

	// Check if the policy intent was null/null, if so then return with the bodytype being set to null/test
	// or error if the above is false.
	//
	// Claim bodytype should be set to error and a ClaimError structure returned in an error field

	if p.Intent=="null/null" {
		log.Println(" null call worked ")
		rtn["worked"] = true
		return rtn,ips,"null/test"
	} else {
		log.Println(" null call bad error ")	
		rtn["error"] = "Error here"
		return rtn,ips,structures.CLAIMERROR
	}
}