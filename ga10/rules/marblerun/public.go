package marblerun

import (
	"a10/structures"
	"bytes"
	"crypto/sha256"
	"encoding/base64"
	"encoding/binary"
	"encoding/hex"
	"encoding/json"
	"encoding/pem"
	"fmt"

	"github.com/edgelesssys/ego/attestation/tcbstatus"
	"github.com/edgelesssys/ego/eclient"
)

func Registration() []structures.Rule {

	validateCoordinator := structures.Rule{"marblerun_coordinator", "Validates the claim of a cordinator", ValidateCoordinator, true}
	validateInfrastructure := structures.Rule{"marblerun_infrastructure", "Validates if a infrastructure is deployed on the coordinator", ValidateInfrastructure, true}
	validatePackage := structures.Rule{"marblerun_package", "Validate Package in manifest", ValidatePackage, true}
	validateMarble := structures.Rule{"marblerun_marble", "Validate Marble in manifest", ValidateMarble, true}

	return []structures.Rule{validateCoordinator, validateInfrastructure, validatePackage, validateMarble}
}

func ValidateCoordinator(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{}) (structures.ResultValue, string, error) {
	var marbleRunCoordinatorEV structures.MarbleRunCoordinatorEV
	err := marbleRunCoordinatorEV.Decode(ev)
	if err != nil {
		return structures.MissingExpectedValue, "expected values could not be extracted", err
	}

	data := claim.Body["data"].(map[string]interface{})
	quote := data["Quote"].(string)
	certs_pem := data["Cert"].(string)

	var certs []*pem.Block
	block, left := pem.Decode([]byte(certs_pem))
	if block == nil {
		return structures.Fail, "could not parse certs", nil
	}
	certs = append(certs, block)

	for len(left) > 0 {
		block, left = pem.Decode([]byte(left))
		if block == nil {
			return structures.Fail, "could not parse certs", nil
		}
		certs = append(certs, block)
	}

	reportBytes, err := base64.StdEncoding.DecodeString(quote)
	if err != nil {
		return structures.Fail, "base64 decoding of quote failed", err
	}

	// Validate the report itself. Requires access to PCCS
	report, err := eclient.VerifyRemoteReport(reportBytes)
	if err != nil {
		return structures.Fail, fmt.Sprintf("verification of report failed %w", err), nil
	}

	caCertRaw := certs[len(certs)-1].Bytes
	caCertHash := sha256.Sum256(caCertRaw)

	if !bytes.Equal(caCertHash[:], report.Data[:len(caCertHash)]) {
		return structures.Fail, "data in report does not match CA certificate", nil
	}

	// Check basic properties
	if marbleRunCoordinatorEV.SecurityVersion != 0 && report.SecurityVersion < marbleRunCoordinatorEV.SecurityVersion {
		return structures.Fail, "invalid security version", nil
	}

	if marbleRunCoordinatorEV.ProductID != 0 && binary.LittleEndian.Uint16(report.ProductID) != marbleRunCoordinatorEV.ProductID {
		return structures.Fail, "invalid ProductID", nil
	}

	if marbleRunCoordinatorEV.UniqueID != nil && !bytes.Equal(marbleRunCoordinatorEV.UniqueID, report.UniqueID) {
		return structures.Fail, "invalid ProductID", nil
	}

	if marbleRunCoordinatorEV.SignerID != nil && !bytes.Equal(marbleRunCoordinatorEV.SignerID, report.SignerID) {
		return structures.Fail, "invalid ProductID", nil
	}

	// Check TCB. Ignore SWHardeningNeeded because this always stays even when mitigations are already in place.
	if marbleRunCoordinatorEV.ValidateTCB && !(report.TCBStatus == tcbstatus.UpToDate || report.TCBStatus == tcbstatus.SWHardeningNeeded) {
		return structures.Fail, fmt.Sprintf("TCB is not up to date: %s", tcbstatus.Explain(report.TCBStatus)), nil
	}

	var certResult []string

	for _, cert := range certs {
		pem_cert := string(pem.EncodeToMemory(cert))
		certResult = append(certResult, pem_cert)
	}

	type result struct {
		Certs []string `json:"certs"`
	}

	res := result{
		Certs: certResult,
	}

	restJSON, err := json.Marshal(&res)
	if err != nil {
		return structures.Fail, "could not marshall result", nil
	}

	return structures.Success, string(restJSON), nil
}

func getManifest(claim structures.Claim) (map[string]map[string]interface{}, error) {
	data := claim.Body["data"].(map[string]interface{})

	manifestEncoded, ok := data["Manifest"].(string)
	if manifestEncoded == "" || !ok {
		return nil, fmt.Errorf("claim does not contain manifest")
	}

	manifestStr, err := base64.StdEncoding.DecodeString(manifestEncoded)
	if err != nil {
		return nil, fmt.Errorf("could not decode manifest")
	}
	var manifest map[string]map[string]interface{}
	err = json.Unmarshal(manifestStr, &manifest)
	if err != nil {
		return nil, fmt.Errorf("could not unmarshal manifest")
	}
	return manifest, nil
}

func ValidateInfrastructure(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{}) (structures.ResultValue, string, error) {
	var marbleRunInfrastructureEV structures.MarbleRunInfrastructureEV
	err := marbleRunInfrastructureEV.Decode(ev)
	if err != nil {
		return structures.MissingExpectedValue, "expected values could not be extracted", err
	}

	manifest, err := getManifest(claim)
	if err != nil {
		return structures.Fail, err.Error(), nil
	}

	infrastructures, ok := manifest["Infrastructures"]
	if !ok {
		return structures.Fail, "Could not find Infrastructures", nil
	}

	found := false
	for name, data := range infrastructures {
		dataMap := data.(map[string]interface{})
		entry := structures.MarbleRunInfrastructureEV{
			Name:   name,
			UEID:   dataMap["UEID"].(string),
			CPUSVN: dataMap["CPUSVN"].(string),
			PCESVN: dataMap["PCESVN"].(string),
			RootCA: dataMap["RootCA"].(string),
		}
		if entry.Equal(marbleRunInfrastructureEV) {
			found = true
		}
	}

	if !found {
		return structures.Fail, "could not find infrastructure configured on coordinator", nil
	}

	return structures.Success, fmt.Sprintf("Infrastructure %s is deployed at the coordinator", marbleRunInfrastructureEV.Name), nil
}

func ValidatePackage(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{}) (structures.ResultValue, string, error) {
	var marbleRunPackageEV structures.MarbleRunPackageEV
	err := marbleRunPackageEV.Decode(ev)
	if err != nil {
		return structures.MissingExpectedValue, "expected values could not be extracted", err
	}

	manifest, err := getManifest(claim)
	if err != nil {
		return structures.Fail, err.Error(), nil
	}

	packages, ok := manifest["Packages"]
	if !ok {
		return structures.Fail, "Could not find Packages", nil
	}

	found := false
	for name, data := range packages {
		dataMap := data.(map[string]interface{})

		var UniqueID []byte

		if val, ok := dataMap["UniqueID"]; ok {
			UniqueID, err = hex.DecodeString(val.(string))
			if err != nil {
				return structures.Fail, "UniqueID decode failed", nil
			}
		}

		var SignerID []byte
		if val, ok := dataMap["SignerID"]; ok {
			SignerID, err = hex.DecodeString(val.(string))
			if err != nil {
				return structures.Fail, "SignerID decode failed", nil
			}
		}

		ProductID := uint16(dataMap["ProductID"].(float64))
		SecurityVersion := uint(dataMap["SecurityVersion"].(float64))

		Debug := dataMap["Debug"].(bool)

		entry := structures.MarbleRunPackageEV{
			Name:            name,
			SecurityVersion: SecurityVersion,
			UniqueID:        UniqueID,
			ProductID:       ProductID,
			SignerID:        SignerID,
			Debug:           Debug,
		}

		if entry.Equal(marbleRunPackageEV) {
			found = true
		}
	}

	if !found {
		return structures.Fail, fmt.Sprintf("could not find package %s in manifest", marbleRunPackageEV.Name), nil
	}

	return structures.Success, "found package in manifest", nil
}

func ValidateMarble(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{}) (structures.ResultValue, string, error) {
	var marbleRunMarbleEV structures.MarbleRunMarbleEV
	err := marbleRunMarbleEV.Decode(ev)
	if err != nil {
		return structures.MissingExpectedValue, "expected values could not be extracted", err
	}

	manifest, err := getManifest(claim)
	if err != nil {
		return structures.Fail, err.Error(), nil
	}

	marbles, ok := manifest["Marbles"]
	if !ok {
		return structures.Fail, "Could not find Marbles", nil
	}

	found := false
	for name, data := range marbles {
		dataMap := data.(map[string]interface{})
		entry := structures.MarbleRunMarbleEV{
			Name:    name,
			Package: dataMap["Package"].(string),
		}

		if entry.Equal(marbleRunMarbleEV) {
			found = true
		}
	}

	if !found {
		return structures.Fail, fmt.Sprintf("could not find marble %s in manifest", marbleRunMarbleEV.Name), nil
	}

	res := make(map[string]interface{})
	res["message"] = "found marble in manifest"
	// Return the package to easily attest it next
	res["result"] = marbleRunMarbleEV.Package

	result, err := json.Marshal(res)
	if err != nil {
		return structures.Fail, "could not marshall result", nil
	}

	return structures.Success, string(result), nil
}
