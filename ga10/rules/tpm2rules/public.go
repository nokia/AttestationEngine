package tpm2rules

import(
	"fmt"

	"a10/structures"
)


func Registration() []structures.Rule {

	attestedPCRDigest := structures.Rule{ "tpm2_attestedValue","Checks the TPM's reported attested value against the expected value", AttestedPCRDigest, true}
	ruleFirmware := structures.Rule{ "tpm2_firmware","Checks the TPM firmware version against the expected value", FirmwareRule, true}
	ruleMagic    := structures.Rule{ "tpm2_magicNumber","Checks the quote magic number is 0xFF544347", MagicNumberRule, false}
	ruleIsSafe    := structures.Rule{ "tpm2_safe","Checks that the value of safe is 1", IsSafe, false}
	ruleValidSignature  := structures.Rule{ "tpm2_validSignature","Checks that the signature of rule is valid against the signing attestation key", ValidSignature, false}


	return []structures.Rule{ ruleFirmware, ruleMagic, attestedPCRDigest, ruleIsSafe, ruleValidSignature }
}


//
// Convenience Functions for navigating over the claim body
//

func getQuote(claim structures.Claim) map[string]interface{} {
	q := (claim.Body)["quote"]
	return q.(map[string]interface{}) 
}

func getSignature(claim structures.Claim) map[string]interface{} {
	q := (claim.Body)["signature"]
	return q.(map[string]interface{}) 
}

//
// Here are the actual verification functiona
//

func IsSafe(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{})  (structures.ResultValue, string, error)  {
	// Type conversation here are a bit ugly, but at least strongly typed!!!
	// Note that the type of claimedSafe is float64  ... this is the way Golang unmarshalls JSON so there.

	q:=getQuote(claim)

	if _, ok := q["ClockInfo"]; !ok {
	 	return structures.VerifyCallFailure,"Missing ClockInfo in claim body",nil
	}

	cli := q["ClockInfo"]
	claimedSafe := cli.(map[string]interface{})["Safe"]

	if claimedSafe==1.0 {
	 	return structures.Success,"",nil
	 } else {
	 	return structures.Fail,"Uncommanded device/TPM shutdown",nil
	 }
	
}


func AttestedPCRDigest(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{})  (structures.ResultValue, string, error)  {

	q:=getQuote(claim)

	if _, ok :=q["AttestedQuoteInfo"]; !ok {
	 	return structures.VerifyCallFailure,"Missing AttestedQuoteInfo in claim body",nil
	}

	aqi := q["AttestedQuoteInfo"]
	claimedAV := aqi.(map[string]interface{})["PCRDigest"]

	expectedAV := (ev.EVS)["attestedValue"]

	if expectedAV==claimedAV {
	 	return structures.Success,"",nil
	 } else {
	 	msg := fmt.Sprintf("Got %v as attested value but expected %v",claimedAV,expectedAV)
	 	return structures.Fail,msg,nil
	 }
	
}





func FirmwareRule(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{})  (structures.ResultValue, string, error)  {

	// The firmware version is serialised from the JSON as a float64, so we need to convert it to string
	// annoyingly this is in decical not hex
	
	q:=getQuote(claim)


	if _, ok := q["FirmwareVersion"]; !ok {
	 	return structures.VerifyCallFailure,"Missing FirmwareVersion in claim body",nil
	}

	claimedFirmware := fmt.Sprintf("%.0f",q["FirmwareVersion"])
	expectedFirmware := (ev.EVS)["firmwareVersion"]

	if expectedFirmware==claimedFirmware {
	 	return structures.Success,"",nil
	 } else {
	 	msg := fmt.Sprintf("Got %v as firmware version but expected %v",claimedFirmware,expectedFirmware)
	 	return structures.Fail,msg,nil
	 }
	
}

func MagicNumberRule(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{})  (structures.ResultValue, string, error)  {

	// The firmware version is serialised from the JSON as a float64, so we need to convert it to string

	q:=getQuote(claim)

	if _, ok := q["Magic"]; !ok {
	 	return structures.VerifyCallFailure,"Missing Magic in claim body",nil
	}

	magic := fmt.Sprintf("%.0f",q["Magic"])
	fmt.Printf("Magic is %v\n",magic)
	fmt.Printf("But claim body is %v\n",q["Magic"])

	// annoyingly this is in decical not hex, but 4283712327_10 == FF544 357_16

	if magic=="4283712327" {
	 	return structures.Success,"",nil
	 } else {
	 	return structures.Fail,"TPM magic number and/or TPMS_ATTEST type wrong",nil
	 }

}


func ValidSignature(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{})  (structures.ResultValue, string, error)  {

	s:=getSignature(claim)

	fmt.Println("signature is %v",s)

	return structures.Success,"This rule currently always returns success",nil
}