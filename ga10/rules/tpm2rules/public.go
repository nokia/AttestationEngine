package tpm2rules

import (
	"encoding/base64"
	"encoding/hex"
	"fmt"

	"a10/structures"

	"a10/utilities"
)

func Registration() []structures.Rule {

	attestedPCRDigest := structures.Rule{"tpm2_attestedValue", "Checks the TPM's reported attested value against the expected value", AttestedPCRDigest, true}
	ruleFirmware := structures.Rule{"tpm2_firmware", "Checks the TPM firmware version against the expected value", FirmwareRule, true}
	ruleMagic := structures.Rule{"tpm2_magicNumber", "Checks the quote magic number is 0xFF544347", MagicNumberRule, false}
	ruleIsSafe := structures.Rule{"tpm2_safe", "Checks that the value of safe is 1", IsSafe, false}
	ruleValidSignature := structures.Rule{"tpm2_validSignature", "Checks that the signature of rule is valid against the signing attestation key", ValidSignature, false}

	return []structures.Rule{ruleFirmware, ruleMagic, attestedPCRDigest, ruleIsSafe, ruleValidSignature}
}

func IsSafe(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{}) (structures.ResultValue, string, error) {
	q, err := getQuote(claim)
	if err != nil {
		return structures.Fail, "Parsing TPM quote failed", err
	}

	if !q.Data.ClockInfo.Safe {
		return structures.Fail, "Uncommanded device/TPM shutdown", nil
	}

	return structures.Success, "", nil
}

func AttestedPCRDigest(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{}) (structures.ResultValue, string, error) {
	q, err := getQuote(claim)
	if err != nil {
		return structures.Fail, "Parsing TPM quote failed", err
	}

	quoteData, err := q.Data.Attested.Quote()
	if err != nil {
		return structures.Fail, "Parsing TPM quote from Attested failed", err
	}
	claimedAV := hex.EncodeToString(quoteData.PCRDigest.Buffer)
	expectedAV := (ev.EVS)["attestedValue"]

	if expectedAV == claimedAV {
		return structures.Success, "", nil
	} else {
		msg := fmt.Sprintf("Got %v as attested value but expected %v", claimedAV, expectedAV)
		return structures.Fail, msg, nil
	}

}

func FirmwareRule(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{}) (structures.ResultValue, string, error) {
	q, err := getQuote(claim)
	if err != nil {
		return structures.Fail, "Parsing TPM quote failed", err
	}

	claimedFirmware := fmt.Sprintf("%d", q.Data.FirmwareVersion)
	expectedFirmware := (ev.EVS)["firmwareVersion"]

	if expectedFirmware == claimedFirmware {
		return structures.Success, "", nil
	} else {
		msg := fmt.Sprintf("Got %v as firmware version but expected %v", claimedFirmware, expectedFirmware)
		return structures.Fail, msg, nil
	}

}

func MagicNumberRule(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{}) (structures.ResultValue, string, error) {
	q, err := getQuote(claim)
	if err != nil {
		return structures.Fail, "Parsing TPM quote failed", err
	}

	if err := q.Data.Magic.Check(); err != nil {
		return structures.Fail, "TPM magic number and/or TPMS_ATTEST type wrong", nil
	}

	return structures.Success, "", nil
}

func ValidSignature(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{}) (structures.ResultValue, string, error) {
	quote, err := getQuote(claim)
	if err != nil {
		return structures.Fail, "Parsing TPM quote failed", err
	}

	akBytes, err := base64.StdEncoding.DecodeString(claim.Header.Element.TPM2.AK.Public)
	if err != nil {
		return structures.RuleCallFailure, "Base64 decoding the AK failed", err
	}
	akKey, err := utilities.ParseTPMKey(akBytes)
	if err != nil {
		return structures.Fail, "Parsing AK failed", err
	}

	if err := quote.VerifySignature(akKey); err != nil {
		return structures.Fail, "Validation of the quote failed", nil
	}

	return structures.Success, "Quote was validated successfully", nil
}

// Constructs AttestableData struct with optional signature
// TODO find way to cache this in the session object
func getQuote(claim structures.Claim) (*utilities.AttestableData, error) {
	quoteData, ok := (claim.Body)["quote_bytes"]
	if !ok {
		return nil, fmt.Errorf("claim does not contain quote")

	}
	quoteStr := quoteData.(string)
	quoteBytes, err := base64.StdEncoding.DecodeString(quoteStr)
	if err != nil {
		return nil, fmt.Errorf("could not base64 decode quote")
	}
	var signatureBytes []byte
	signatureData, ok := (claim.Body)["signature_bytes"]
	if ok {
		signatureStr := signatureData.(string)
		signatureBytes, err = base64.StdEncoding.DecodeString(signatureStr)
		if err != nil {
			return nil, fmt.Errorf("could not base64 decode signature")
		}
	}

	var quote utilities.AttestableData
	err = quote.Decode(quoteBytes, signatureBytes)
	if err != nil {
		return nil, fmt.Errorf("failed to unmarshal TPM structures %w", err)
	}

	if !quote.IsQuote() {
		return nil, fmt.Errorf("attestable data is not quote")
	}

	return &quote, nil
}
