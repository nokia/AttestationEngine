package a10httprestv2

import(
	"fmt"
	"net/http"
	"encoding/json"
	"io/ioutil"
	"bytes"
	"crypto/rand"

	"a10/structures"
)

const nonceSize int = 24

func Registration() (structures.Protocol) {
	intents := []string{"tpm2/pcrs","tpm2/quote","uefi/eventlog","ima/asciilog","txt/log","sys/info"}

	return structures.Protocol{"A10HTTPRESTv2","HTTP protcol for Go based trust agents",Call,intents}
}


// THis is the function that is called by operations.attestation --- this is the entry point to the actual protocol part.
// It returns a "json" structure and a string with the body type.
// If requestFromTA returns and error, then it is encoded here and returned.
// The body type is *ERROR in these situations and the body should have a field "error": <some value>
func Call(e structures.Element, p structures.Policy, s structures.Session, aps map[string]interface{}) (map[string]interface{}, string) {
	rtn, err :=requestFromTA(e,p,s,aps)

	if err != nil {
		//rtn["error"] = structures.ClaimError{ "error", err.Error() }
		rtn["error"] = err.Error()
		
		return rtn,structures.CLAIMERROR
	} else {
		return rtn,p.Intent
	}
}

func mergeMaps(m1 map[string]interface{}, m2 map[string]interface{}) map[string]interface{} {
	merged := make(map[string]interface{})
	for k, v := range m1 {
		merged[k] = v
	}
	for key, value := range m2 {
		merged[key] = value
	}
	return merged
}

//This function performs the actual interaction with the TA
//This will be highly specific to the actual protocol and its implemented intents
func requestFromTA(e structures.Element, p structures.Policy, s structures.Session, aps map[string]interface{}) (map[string]interface{}, error) {
	
	var empty map[string]interface{} = make(map[string]interface{})  // this is an  *instantiated* empty map used for error situations
	var bodymap map[string]interface{} // this is used to store the result of the final unmarshalling  of the body received from the TA
	
	// Parameters
	//
	// Some come from the element itself, eg: UEFI.eventlog
	// Then those supplied by the policy and finally the additional parameters
	// Only certain intents supply parameters and these are dealt with on a case by case basis here
    //
	// First we construct "ips" which is the intial set of parameters
	//
	// For sanity reasons (and Go's strong typing, the parameters is a plain key,value list)
	var ips map[string]interface{} = make(map[string]interface{})

	// always supply which device to use

	// for specific intents for the a10httprestv2 
	if p.Intent=="tpm2/pcrs" {
		ips["tpm2/device"] = (e.TPM2).Device
	}

	if p.Intent=="tpm2/quote" {
		ips["tpm2/device"] = (e.TPM2).Device
		ips["tpm2/akhandle"] = (e.TPM2).AK.Handle
		nce := make([]byte,nonceSize)
		_, _ = rand.Read(nce)
		ips["tpm2/nonce"] = nce
	}

	if p.Intent=="uefi/eventlog" {
		ips["uefi/eventlog"] = (e.UEFI).Eventlog
	}

	if p.Intent=="ima/asciilog" {
		ips["ima/ASCIIlog"] = (e.IMA).ASCIILog
	}

	if p.Intent=="txt/log" {
		ips["ima/log"] = (e.TXT).Log
	}

	// merge ips with policy parameters. The policy parameters take precidence

	pps := mergeMaps(ips,p.Parameters)
	cps := mergeMaps(pps,aps)

	// Construct the call

	postbody,err := json.Marshal(cps)
	if err != nil {
		return empty,fmt.Errorf("JSON Marshalling failed: %w",err)   
	}

	url := e.Endpoint+"/"+p.Intent
	req,err := http.NewRequest("POST", url, bytes.NewBuffer(postbody))
	req.Header.Set("Content-Type","application/json")
	client := &http.Client{}
	resp,err := client.Do(req)

	if err!=nil {
		return empty,err      // err will be the error from http.client.Do
	}
	defer resp.Body.Close()

	taResponse, _ := ioutil.ReadAll(resp.Body)
	fmt.Println("*****************")
	fmt.Printf("taReponse is %v",taResponse)
	err = json.Unmarshal(taResponse,&bodymap)
	fmt.Println("bodymap")
	fmt.Printf("%v",bodymap)
	fmt.Println("*****************")

	if err != nil {
		return empty,fmt.Errorf("JSON Unmarshalling reponse from TA: %w",err)   
	}

	if resp.Status != "200 OK" {                           // is it always 200 ? This might cause issues later if the TA reponds otherwise!
		return bodymap,fmt.Errorf("TA reports error %v with response %v",resp.Status,taResponse)    
    }

	return bodymap,nil
}
