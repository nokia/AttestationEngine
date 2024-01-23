package keylime

import (
	"a10/configuration"
	"a10/structures"
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
)

func Registration() []structures.Rule {
	validateMB := structures.Rule{"keylime_mb", "Checks TPM Measured Boot Log against Keylime", ValidateMB, true}
	return []structures.Rule{validateMB}
}

type MBRequest struct {
	AgentID           string   `json:"agent_id"`
	HashAlg           string   `json:"hash_alg"`
	MBRefState        string   `json:"mb_refstate"`
	MBMeasurementList string   `json:"mb_measurement_list"`
	PCRsInQuote       []string `json:"pcrs_inquote"`
}

type MBResponse struct {
	Failure        string              `json:"failure"`
	MBPCRHashes    map[string]string   `json:"mb_pcrs_hashes"`
	BootAggregates map[string][]string `json:"boot_aggregates"`
}

func postMBRequest(mb MBRequest) (*MBResponse, error) {
	url := configuration.ConfigData.Keylime.ApiUrl + "/mb/validate"
	data, err := json.Marshal(mb)
	if err != nil {
		return nil, err
	}

	resp, err := http.Post(url, "application/json", bytes.NewBuffer(data))
	if err != nil {
		return nil, err
	}
	respBytes, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, err
	}
	var res MBResponse
	err = json.Unmarshal(respBytes, &res)
	if err != nil {
		return nil, err
	}
	return &res, nil
}

func ValidateMB(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{}) (structures.ResultValue, string, error) {
	imaLogEncoded, ok := claim.Body["eventlog"]
	if !ok {
		return structures.Fail, "eventlog cannot be found in claim", nil
	}
	fmt.Println(parameter)
	// Check if required parameters are there
	for _, name := range []string{"hash_alg", "pcrs_inquote"} {
		_, ok := parameter[name]
		if !ok {
			return structures.Fail, fmt.Sprintf("parameter is missing %s", name), nil
		}
	}
	hashAlg := parameter["hash_alg"].(string)
	var pcrsInQuote []string
	for _, entry := range parameter["pcrs_inquote"].([]interface{}) {
		pcrsInQuote = append(pcrsInQuote, entry.(string))
	}

	var decoded structures.KeylimeMBEV

	err := decoded.Decode(ev)
	if err != nil {
		return structures.Fail, "Decoding of EV failed", nil
	}

	req := MBRequest{
		AgentID:           claim.Header.Element.Name,
		HashAlg:           hashAlg,
		MBMeasurementList: imaLogEncoded.(string),
		PCRsInQuote:       pcrsInQuote,
		MBRefState:        decoded.MBRefstate,
	}
	resp, err := postMBRequest(req)
	if err != nil {
		return structures.Fail, "Decoding of EV failed", nil
	}
	if resp.Failure != "" {
		return structures.Fail, "Measured Boot log not valid", nil
	}
	data, err := json.Marshal(resp)
	if err != nil {
		return structures.Fail, "Failed to encode response", nil
	}
	return structures.Success, string(data), nil
}
