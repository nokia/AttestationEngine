package marblerun

import (
	"a10/operations"
	"a10/structures"
	"bytes"
	"crypto/ecdsa"
	"crypto/sha256"
	"crypto/x509"
	"encoding/base64"
	"encoding/binary"
	"encoding/hex"
	"encoding/json"
	"encoding/pem"
	"fmt"
	"log"

	"github.com/edgelesssys/ego/attestation/tcbstatus"
	"github.com/edgelesssys/ego/eclient"
)

func Registration() []structures.Rule {

	validateCoordinator := structures.Rule{"marblerun_coordinator", "Validates the claim of a cordinator", ValidateCoordinator, true}
	validateInfrastructure := structures.Rule{"marblerun_infrastructure", "Validates if a infrastructure is deployed on the coordinator", ValidateInfrastructure, true}
	validatePackage := structures.Rule{"marblerun_package", "Validate Package in manifest", ValidatePackage, true}
	validateMarble := structures.Rule{"marblerun_marble", "Validate Marble in manifest", ValidateMarble, true}
	validateMarbleInstance := structures.Rule{"marblerun_marbleinstance", "Validate if identity of instance is valid", ValidateMarbleInstance, false}

	return []structures.Rule{validateCoordinator, validateInfrastructure, validatePackage, validateMarble, validateMarbleInstance}
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
	infName, ok := parameter["infrastructure"]
	if !ok {
		return structures.MissingExpectedValue, "expected values could not be extracted", nil
	}
	var marbleRunInfrastructureEV structures.MarbleRunInfrastructureEV
	err := marbleRunInfrastructureEV.Decode(ev, infName.(string))
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

	infInManifest, ok := infrastructures[infName.(string)]
	if !ok {
		return structures.Fail, "could not find infrastructure configured on coordinator", nil
	}

	dataMap := infInManifest.(map[string]interface{})
	entry := structures.MarbleRunInfrastructureEV{
		UEID:   dataMap["UEID"].(string),
		CPUSVN: dataMap["CPUSVN"].(string),
		PCESVN: dataMap["PCESVN"].(string),
		RootCA: dataMap["RootCA"].(string),
	}

	if !entry.Equal(marbleRunInfrastructureEV) {
		return structures.Fail, "Infrastructure does not match EV", nil
	}

	return structures.Success, fmt.Sprintf("Infrastructure %s is deployed at the coordinator", infName.(string)), nil
}

func ValidatePackage(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{}) (structures.ResultValue, string, error) {
	pkgName, ok := parameter["package"]
	if !ok {
		return structures.MissingExpectedValue, "expected values could not be extracted", nil
	}

	var marbleRunPackageEV structures.MarbleRunPackageEV
	err := marbleRunPackageEV.Decode(ev, pkgName.(string))
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

	packageInManifest, ok := packages[pkgName.(string)]
	if !ok {
		return structures.Fail, "Could not find package", nil
	}

	dataMap := packageInManifest.(map[string]interface{})

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
		SecurityVersion: SecurityVersion,
		UniqueID:        UniqueID,
		ProductID:       ProductID,
		SignerID:        SignerID,
		Debug:           Debug,
	}

	if !entry.Equal(marbleRunPackageEV) {
		return structures.Fail, fmt.Sprintf("Packages are not equal"), nil
	}

	return structures.Success, "found package in manifest", nil
}

func ValidateMarble(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{}) (structures.ResultValue, string, error) {
	log.Printf("parameters", parameter)
	marbleName, ok := parameter["marble"]
	if !ok {
		return structures.MissingExpectedValue, "No marble specified in parameter", nil
	}

	var marbleRunMarbleEV structures.MarbleRunMarbleEV
	err := marbleRunMarbleEV.Decode(ev, marbleName.(string))
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
			Package: dataMap["Package"].(string),
		}

		if name == marbleName.(string) && entry.Equal(marbleRunMarbleEV) {
			found = true
		}
	}

	if !found {
		return structures.Fail, fmt.Sprintf("could not find marble %s in manifest", marbleName.(string)), nil
	}

	res := make(map[string]interface{})
	res["message"] = "found marble in manifest"
	// Return the package to easily attest it next
	res["package"] = marbleRunMarbleEV.Package

	result, err := json.Marshal(res)
	if err != nil {
		return structures.Fail, "could not marshall result", nil
	}

	return structures.Success, string(result), nil
}

type MarbleInstanceResult struct {
	Marble         string
	Infrastructure string
	UUID           string
	CertSignature  string
}

type RequestData struct {
	Nonce       string // base64 encoded
	Certificate string // base64 encoded ASN1 format
}

func ValidateMarbleInstance(claim structures.Claim, rule string, _ structures.ExpectedValue, session structures.Session, parameter map[string]interface{}) (structures.ResultValue, string, error) {
	element := claim.Header.Element

	coordinatorIDRaw, ok := claim.Header.Policy.Parameters["coordinatorID"]
	if !ok {
		return structures.RuleCallFailure, "coordinator id is missing a a parameter", nil
	}
	coordinatorID := coordinatorIDRaw.(string)

	coordinator, err := operations.GetElementByItemID(coordinatorID)
	if err != nil {
		return structures.Fail, "couldn't find coordinator element", nil
	}

	var requestData RequestData
	err = json.Unmarshal([]byte(element.MRMarbleInstance.RequestData), &requestData)
	if err != nil {
		return structures.Fail, "couldn't unmarshal request data", nil
	}

	// check if the request is actually signed with the certificate provided
	log.Println("cert encoded", requestData.Certificate)
	decodedCert, err := base64.StdEncoding.DecodeString(requestData.Certificate)
	if err != nil {
		return structures.Fail, "couldn't decode cert", nil
	}

	cert, err := x509.ParseCertificate(decodedCert)
	if err != nil {
		log.Println("Parse cert error", err)
		return structures.Fail, "couldn't parse cert", nil
	}

	// Check signature
	dataHash := sha256.Sum256([]byte(element.MRMarbleInstance.RequestData))

	signature, err := base64.StdEncoding.DecodeString(element.MRMarbleInstance.RequestSignature)
	if err != nil {
		return structures.Fail, "couldn't decode signature", nil
	}

	if !ecdsa.VerifyASN1(cert.PublicKey.(*ecdsa.PublicKey), dataHash[:], signature) {
		return structures.Fail, "signature does not match request data", nil
	}

	// Check if cert was issued from coordinator
	if len(coordinator.MRCoordinator.Certs) != 2 {
		return structures.Fail, "coordinator no or too many certs, expected only root and intermediate", nil
	}

	var parsedCerts []*x509.Certificate
	for _, cert := range coordinator.MRCoordinator.Certs {
		block, rest := pem.Decode([]byte(cert))
		if len(rest) != 0 {
			return structures.Fail, "expected only one certificate in each entry", nil
		}
		parsedCert, err := x509.ParseCertificate(block.Bytes)
		if err != nil {
			return structures.Fail, "failed to parse certificate", nil
		}
		parsedCerts = append(parsedCerts, parsedCert)
	}

	// intermediate cert
	iPool := x509.NewCertPool()
	iPool.AddCert(parsedCerts[0])
	// root cert
	rPool := x509.NewCertPool()
	rPool.AddCert(parsedCerts[1])

	opts := x509.VerifyOptions{
		Roots:         iPool,
		Intermediates: iPool,
	}

	if cert.Subject.CommonName != element.Name {
		return structures.Fail, "UUID does not match name in NAE", err
	}

	if _, err := cert.Verify(opts); err != nil {
		return structures.Fail, "validation of the certificate failed", err
	}

	// Check nonces (directly compare the base64 values)
	if requestData.Nonce != element.MRMarbleInstance.ExpectedNonce {
		return structures.Fail, "nonces do not match", nil
	}

	// extract information about the instance from the cert
	result := MarbleInstanceResult{
		Marble:         cert.Subject.Country[0],
		UUID:           cert.Subject.CommonName,
		Infrastructure: cert.Subject.Locality[0],
		CertSignature:  string(cert.Signature),
	}

	resultStr, err := json.Marshal(result)
	if err != nil {
		return structures.Fail, "couldn't marshall result", nil
	}
	return structures.Success, string(resultStr), nil
}
