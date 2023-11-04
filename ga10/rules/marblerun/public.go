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

	return []structures.Rule{validateCoordinator}
}

type MarbleRunEV struct {
	SecurityVersion uint
	UniqueID        []byte
	SignerID        []byte
	ProductID       uint16
	Debug           bool
	ValidateTCB     bool
}

func convertEV(ev structures.ExpectedValue) (*MarbleRunEV, error) {
	properties := ev.EVS["properties"].(map[string]interface{})

	SecurityVersionRaw, ok := properties["SecurityVersion"]
	if !ok {
		return nil, fmt.Errorf("SecurityVersion is missing in EV")
	}
	SecurityVersion := uint(SecurityVersionRaw.(float64))

	UniqueIDS, ok_unique := properties["UniqueID"]
	SignerIDS, ok_signer := properties["SignerID"]
	if !ok_signer && !ok_unique {
		return nil, fmt.Errorf("UniqueID or SignerID need to be specified")
	}

	var UniqueID []byte
	var err error
	if ok_unique {
		UniqueID, err = hex.DecodeString(UniqueIDS.(string))
		if err != nil {
			return nil, fmt.Errorf("UniqueID decode failed")
		}
	}

	var SignerID []byte
	SignerID, err = hex.DecodeString(SignerIDS.(string))
	if err != nil {
		return nil, fmt.Errorf("UniqueID decode failed")
	}

	ProductIDRaw, ok := properties["ProductID"]
	if !ok {
		return nil, fmt.Errorf("ProductID is missing")
	}
	ProductID := uint16(ProductIDRaw.(float64))
	DebugRaw, ok := properties["Debug"]
	Debug := false
	if ok {
		Debug = DebugRaw.(bool)
	}

	ValidateTCB := true
	ValidateTCBRaw, ok := properties["ValidateTCB"]
	if ok {
		ValidateTCB = ValidateTCBRaw.(bool)
	}

	res := MarbleRunEV{
		SecurityVersion: SecurityVersion,
		UniqueID:        UniqueID,
		SignerID:        SignerID,
		ProductID:       ProductID,
		Debug:           Debug,
		ValidateTCB:     ValidateTCB,
	}
	return &res, nil
}

func ValidateCoordinator(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{}) (structures.ResultValue, string, error) {
	marbleRunEV, err := convertEV(ev)
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
	if marbleRunEV.SecurityVersion != 0 && report.SecurityVersion < marbleRunEV.SecurityVersion {
		return structures.Fail, "invalid security version", nil
	}

	if marbleRunEV.ProductID != 0 && binary.LittleEndian.Uint16(report.ProductID) != marbleRunEV.ProductID {
		return structures.Fail, "invalid ProductID", nil
	}

	if marbleRunEV.UniqueID != nil && !bytes.Equal(marbleRunEV.UniqueID, report.UniqueID) {
		return structures.Fail, "invalid ProductID", nil
	}

	if marbleRunEV.SignerID != nil && !bytes.Equal(marbleRunEV.SignerID, report.SignerID) {
		return structures.Fail, "invalid ProductID", nil
	}

	// Check TCB. Ignore SWHardeningNeeded because this always stays even when mitigations are already in place.
	if marbleRunEV.ValidateTCB && !(report.TCBStatus == tcbstatus.UpToDate || report.TCBStatus == tcbstatus.SWHardeningNeeded) {
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
