package keylime

import (
	"a10/configuration"
	"a10/operations"
	"a10/structures"
	"bytes"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"strings"
)

func Registration() []structures.Rule {
	validateMB := structures.Rule{"keylime_mb", "Checks TPM Measured Boot Log against Keylime", ValidateMB, true}
	validateIMA := structures.Rule{"keylime_ima", "Checks TPM Measured Boot Log against Keylime", ValidateIMA, true}
	return []structures.Rule{validateMB, validateIMA}
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

	if resp.StatusCode != 200 {
		return nil, fmt.Errorf("got invalid response %s", string(respBytes))
	}

	var res MBResponse
	err = json.Unmarshal(respBytes, &res)
	if err != nil {
		return nil, err
	}
	return &res, nil
}

func ValidateMB(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{}) (structures.ResultValue, string, error) {
	mbLogEncoded, ok := claim.Body["eventlog"]
	if !ok {
		return structures.Fail, "eventlog cannot be found in claim", nil
	}
	// Check if required parameters are there
	for _, name := range []string{"hash_alg", "pcrs_inquote", "pcrscid"} {
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

	pcrsClaimId := parameter["pcrscid"].(string)
	pcrsClaim, err := operations.GetClaimByItemID(pcrsClaimId)
	if err != nil {
		return structures.Fail, "Could not get PCRs claim", nil
	}
	pcrs := pcrsClaim.Body[hashAlg].(map[string]interface{})
	var decoded structures.KeylimeMBEV

	err = decoded.Decode(ev)
	if err != nil {
		return structures.Fail, "Decoding of EV failed", nil
	}

	req := MBRequest{
		AgentID:           claim.Header.Element.Name,
		HashAlg:           hashAlg,
		MBMeasurementList: mbLogEncoded.(string),
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

	// Check if the PCRs also match
	for k, v := range resp.MBPCRHashes {
		// HACK: demo machines firmware has a miss match for PCR0, that cannot explained via the UEFI eventlog
		if k == "0" {
			continue
		}
		other, ok := pcrs[k]
		if !ok {
			return structures.Fail, fmt.Sprintf("PCR %s is not included output from Keylime", k), nil
		}
		// Keylime trims leading 0s
		data := other.(string)
		data = strings.TrimLeft(data, "0")
		if data != v {
			return structures.Fail, fmt.Sprintf("Mismatch for PCR %s. Got: %s, expected %s", k, other, v), nil
		}
	}

	return structures.Success, string(data), nil
}

type IMARequest struct {
	AgentID            string `json:"agent_id"`
	HashAlg            string `json:"hash_alg"`
	IMAMeasurementList string `json:"ima_measurement_list"`
	RuntimePolicy      string `json:"runtime_policy"`
	PCRVal             string `json:"pcrval"`
}

type IMAResponse struct {
	Failure string `json:"failure"`
	Context string `json:"context"`
}

func postIMARequest(ima IMARequest) (*IMAResponse, error) {
	url := configuration.ConfigData.Keylime.ApiUrl + "/ima/validate"
	data, err := json.Marshal(ima)
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

	if resp.StatusCode != 200 {
		return nil, fmt.Errorf("got invalid response %s", string(respBytes))
	}

	var res IMAResponse
	err = json.Unmarshal(respBytes, &res)
	if err != nil {
		return nil, err
	}
	return &res, nil
}

func ValidateIMA(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{}) (structures.ResultValue, string, error) {
	ima, ok := claim.Body["asciilog"]
	if !ok {
		return structures.Fail, "IMA log cannot be found in claim", nil
	}

	imaDecoded, err := base64.StdEncoding.DecodeString(ima.(string))
	if err != nil {
		return structures.Fail, "Cannot base64 decode IMA log", nil
	}

	// Check if required parameters are there
	for _, name := range []string{"hash_alg", "pcrscid"} {
		_, ok := parameter[name]
		if !ok {
			return structures.Fail, fmt.Sprintf("parameter is missing %s", name), nil
		}
	}

	hashAlg := parameter["hash_alg"].(string)
	pcrsClaimId := parameter["pcrscid"].(string)
	pcrsClaim, err := operations.GetClaimByItemID(pcrsClaimId)
	if err != nil {
		return structures.Fail, "Could not get PCR claim", nil
	}

	pcrs := pcrsClaim.Body[hashAlg].(map[string]interface{})
	pcr10 := pcrs["10"].(string)

	var runtimePolicy structures.KeylimeIMAEV
	err = runtimePolicy.Decode(ev)
	if err != nil {
		return structures.Fail, "Could not decode EV", nil
	}

	req := IMARequest{
		AgentID:            claim.Header.Element.Name,
		HashAlg:            hashAlg,
		PCRVal:             pcr10,
		IMAMeasurementList: string(imaDecoded),
		RuntimePolicy:      runtimePolicy.RuntimePolicy,
	}

	resp, err := postIMARequest(req)
	if err != nil {
		return structures.Fail, fmt.Sprintf("Communication with Keylime failed %w", err), nil
	}

	if resp.Failure != "" {
		return structures.Fail, fmt.Sprintf("IMA attestation failed: %s, context: %s", resp.Failure, resp.Context), nil
	}

	return structures.Success, "", nil
}
