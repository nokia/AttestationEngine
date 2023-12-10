package testcontainerprotocol

import(
	"fmt"
	"crypto/rand"

	"a10/structures"
)

const nonceSize int = 32

func Registration() (structures.Protocol) {
	intents := []string{"testcontainers/repoimageintegrity"}

	return structures.Protocol{"TESTCONTAINERPROTOCOL","Test protocol for containers",Call, intents}
}

func Call(e structures.Element, p structures.Policy, s structures.Session, cps map[string]interface{}) (map[string]interface{}, map[string]interface{}, string) {

	// Create a test body

	// create a nonce, because why not?
	
	nce := make([]byte, nonceSize)
	_, _ = rand.Read(nce)

	// add it to the set of parameters supplied to this function

	ips := map[string]interface{}{
		 "nonce":nce,
	}

    // **************************************
	// When calling the real "trust agent" endpoint,  ips would be passed as the parameters to whatever function is called that supports the intent
	// This is where the call code goes
    // **************************************

	// return the body - we create a test body here

	rtn := map[string]interface{}{
		 "foo":"bar",
		 "calling": fmt.Sprintf("with protocol %v I would send an intent to %v",e.Protocol,p.Intent),
		 "aNumber": 42,
	}

	// Note, if anything above fails please return an error quote, ie the final string in the return below would be *ERROR

	// rtn is the return body
	// ips is the generated parameters, ie: cps + anything else
	// stirng contains the type of the result.   If this is  *ERROR then the claim is an error claim - any later verification over that structure would fail accordingly

	return rtn,ips,"testcontainers/repoimageintegrity"
	
}