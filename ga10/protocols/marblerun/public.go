package marblerun

import (
	"crypto/tls"
	"encoding/json"
	"fmt"
	"io"
	"net/http"

	"a10/structures"
)

func Registration() structures.Protocol {
	intents := []string{"marblerun/quote", "marblerun/updatelogs"}

	return structures.Protocol{"A10MARBLERUNPROTOCOL", "Protocol to generate quote from MarbleRun", Call, intents}
}

func Call(e structures.Element, p structures.Policy, s structures.Session, aps map[string]interface{}) (map[string]interface{}, string) {
	rtn, err := requestFromMarbleRun(e, p, s, aps)

	if err != nil {
		//rtn["error"] = structures.ClaimError{ "error", err.Error() }
		rtn["error"] = err.Error()

		return rtn, structures.CLAIMERROR
	} else {
		return rtn, p.Intent
	}
}

func requestFromMarbleRun(e structures.Element, p structures.Policy, s structures.Session, cps map[string]interface{}) (map[string]interface{}, error) {
	var empty map[string]interface{} = make(map[string]interface{}) // this is an  *instantiated* empty map used for error situations
	var bodymap map[string]interface{}                              // this is used to store the result of the final unmarshalling  of the body received from the TA

	suffix := ""
	if p.Intent == "marblerun/quote" {
		suffix = "quote"
	} else if p.Intent == "marblerun/updatelogs" {
		suffix = "update"
	} else {
		return empty, fmt.Errorf("intent not supported %s", p.Intent)
	}

	url := e.Endpoint + "/" + suffix
	tr := &http.Transport{
		TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
	}
	client := &http.Client{Transport: tr}
	resp, err := client.Get(url)
	if err != nil {
		return empty, err
	}
	defer resp.Body.Close()
	quoteResponse, _ := io.ReadAll(resp.Body)
	err = json.Unmarshal(quoteResponse, &bodymap)

	if err != nil {
		return empty, fmt.Errorf("JSON Unmarshalling reponse from TA: %w", err)
	}

	if resp.Status != "200 OK" {
		return bodymap, fmt.Errorf("MarbleRun reports error %v with response %v", resp.Status, quoteResponse)
	}

	return bodymap, nil
}
