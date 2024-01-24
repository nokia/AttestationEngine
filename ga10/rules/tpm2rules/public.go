package tpm2rules

import (
	"bytes"
	"crypto/sha256"
	"encoding/base64"
	"encoding/hex"
	"fmt"
	"reflect"
	"slices"
	"strconv"

	"a10/operations"
	"a10/structures"

	"a10/utilities"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

func Registration() []structures.Rule {
	attestedPCRDigest := structures.Rule{"tpm2_attestedValue", "Checks the TPM's reported attested value against the expected value", AttestedPCRDigest, true}
	checkPCRSelection := structures.Rule{"tpm2_PCRSelection", "Checks if a quote includes the correct PCRs", checkPCRSelection, true}
	checkQuoteDigest256 := structures.Rule{"tpm2_quoteDigest256", "Checks if a claim of PCRs match the hash in the quote (sha256)", checkQuoteDigest256, false}
	ruleFirmware := structures.Rule{"tpm2_firmware", "Checks the TPM firmware version against the expected value", FirmwareRule, true}
	ruleMagic := structures.Rule{"tpm2_magicNumber", "Checks the quote magic number is 0xFF544347", MagicNumberRule, false}
	ruleIsSafe := structures.Rule{"tpm2_safe", "Checks that the value of safe is 1", IsSafe, false}
	ruleValidSignature := structures.Rule{"tpm2_validSignature", "Checks that the signature of rule is valid against the signing attestation key", ValidSignature, false}
	ruleValidNonce := structures.Rule{"tpm2_validNonce", "Checks that nonce used for the claim matches the nonce in the quote", ValidNonce, false}

	return []structures.Rule{ruleFirmware, ruleMagic, attestedPCRDigest, ruleIsSafe, ruleValidSignature, ruleValidNonce, checkQuoteDigest256, checkPCRSelection}
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

func ValidNonce(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{}) (structures.ResultValue, string, error) {
	quote, err := getQuote(claim)
	if err != nil {
		return structures.Fail, "Parsing TPM quote failed", err
	}
	quoteNonceBytes := quote.Data.ExtraData.Buffer

	claimNonceValue, ok := claim.Header.CallParameters["tpm2/nonce"]
	if !ok {
		return structures.RuleCallFailure, "claim has no nonce", nil
	}
	claimNonceBytes, ok := claimNonceValue.(primitive.Binary)
	if !ok {
		return structures.RuleCallFailure, fmt.Sprintf("Nonce is not of type Binary. It is: %s", reflect.TypeOf(claimNonceValue)), nil
	}

	if !bytes.Equal(quoteNonceBytes, claimNonceBytes.Data) {
		return structures.Fail, fmt.Sprintf("Nonce are not matching, got: \"%s\", expected: \"%s\"", hex.EncodeToString(quoteNonceBytes), hex.EncodeToString(claimNonceBytes.Data)), nil
	}

	return structures.Success, "nonce matches", nil
}

func checkPCRSelection(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{}) (structures.ResultValue, string, error) {
	quote, err := getQuote(claim)
	if err != nil {
		return structures.Fail, "Parsing TPM quote failed", err
	}

	data, err := quote.Data.Attested.Quote()
	if err != nil {
		return structures.Fail, "Parsing Attest structure into Quote failed", err
	}
	selection := utilities.TPMSPCRSelectionToList(data.PCRSelect.PCRSelections)

	evSelection, ok := ev.EVS["pcrselection"]
	if !ok {
		return structures.MissingExpectedValue, "pcrselection not given", err
	}
	var evSelectionList []int
	for _, v := range evSelection.(primitive.A) {
		index, err := strconv.Atoi(v.(string))
		if err != nil {
			return structures.Fail, "pcrselection contains non integer strings", err
		}
		evSelectionList = append(evSelectionList, index)
	}
	if len(evSelectionList) != len(selection) {
		return structures.Fail, "not the same length", err
	}
	for _, v := range evSelectionList {
		if !slices.Contains(selection, v) {
			return structures.Fail, fmt.Sprintf("Index %d is missing in quote", v), err
		}
	}
	return structures.Success, "", err
}

func checkQuoteDigest256(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{}) (structures.ResultValue, string, error) {
	quote, err := getQuote(claim)
	if err != nil {
		return structures.Fail, "Parsing TPM quote failed", err
	}
	pcrsClaimID := parameter["pcrscid"].(string)

	pcrsClaim, err := operations.GetClaimByItemID(pcrsClaimID)
	if err != nil {
		return structures.Fail, "Could not get PCRs claim", err
	}

	data, err := quote.Data.Attested.Quote()
	if err != nil {
		return structures.Fail, "Parsing Attest structure into Quote failed", err
	}
	digest := data.PCRDigest.Buffer
	selection := utilities.TPMSPCRSelectionToList(data.PCRSelect.PCRSelections)

	sha256Entries := make(map[string]string)
	for k, v := range pcrsClaim.Body["sha256"].(map[string]interface{}) {
		sha256Entries[k] = v.(string)
	}

	hash := sha256.New()
	for _, pcrIndex := range selection {
		pcrIndexS := fmt.Sprintf("%d", pcrIndex)

		entry, ok := sha256Entries[pcrIndexS]
		if !ok {
			return structures.Fail, fmt.Sprintf("PCR index missing in PCR claim: %s", entry), err
		}
		entryBytes, err := hex.DecodeString(entry)
		if err != nil {
			return structures.Fail, "Entry not valid hex", err
		}
		hash.Write(entryBytes)
	}

	digestPCRs := hash.Sum([]byte{})
	if !bytes.Equal(digestPCRs, digest) {
		return structures.Fail, "PCRs and hash in quote do not match", err
	}

	return structures.Success, "PCRs and hash in quote match", err
}

// Constructs AttestableData struct with signature
// TODO find way to cache this in the session object
func getQuote(claim structures.Claim) (*utilities.AttestableData, error) {
	quoteData, ok := (claim.Body)["quote"]
	if !ok {
		return nil, fmt.Errorf("claim does not contain quote")

	}
	quoteStr := quoteData.(string)
	quoteBytes, err := base64.StdEncoding.DecodeString(quoteStr)
	if err != nil {
		return nil, fmt.Errorf("could not base64 decode quote")
	}
	var signatureBytes []byte
	signatureData, ok := (claim.Body)["signature"]
	if !ok {
		return nil, fmt.Errorf("claim does not contain a signature")
	}
	signatureStr := signatureData.(string)
	signatureBytes, err = base64.StdEncoding.DecodeString(signatureStr)
	if err != nil {
		return nil, fmt.Errorf("could not base64 decode signature")
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
