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

	return []structures.Rule{ ruleFirmware, ruleMagic, attestedPCRDigest, ruleIsSafe }
}



func IsSafe(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{})  (structures.ResultValue, string, error)  {
	// Type conversation here are a bit ugly, but at least strongly typed!!!
	// Note that the type of claimedSafe is float64  ... this is the way Golang unmarshalls JSON so there.

	if _, ok := (claim.Body)["ClockInfo"]; !ok {
	 	return structures.VerifyCallFailure,"Missing ClockInfo in claim body",nil
	}

	cli := (claim.Body)["ClockInfo"]
	claimedSafe := cli.(map[string]interface{})["Safe"]

	if claimedSafe==1.0 {
	 	return structures.Success,"",nil
	 } else {
	 	return structures.Fail,"Uncommanded device/TPM shutdown",nil
	 }
	
}


func AttestedPCRDigest(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{})  (structures.ResultValue, string, error)  {
	// Type conversation here are a bit ugly, but at least strongly typed!!!

	if _, ok := (claim.Body)["AttestedQuoteInfo"]; !ok {
	 	return structures.VerifyCallFailure,"Missing AttestedQuoteInfo in claim body",nil
	}

	aqi := (claim.Body)["AttestedQuoteInfo"]
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
	
	if _, ok := (claim.Body)["FirmwareVersion"]; !ok {
	 	return structures.VerifyCallFailure,"Missing FirmwareVersion in claim body",nil
	}

	claimedFirmware := fmt.Sprintf("%.0f",(claim.Body)["FirmwareVersion"])
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
	
	if _, ok := (claim.Body)["Magic"]; !ok {
	 	return structures.VerifyCallFailure,"Missing Magic in claim body",nil
	}

	magic := fmt.Sprintf("%.0f",(claim.Body)["Magic"])
	fmt.Printf("Magic is %v\n",magic)
	fmt.Printf("But claim body is %v\n",(claim.Body)["Magic"])

	// annoyingly this is in decical not hex, but 4283712327_10 == FF544 357_16

	if magic=="4283712327" {
	 	return structures.Success,"",nil
	 } else {
	 	return structures.Fail,"TPM magic number and/or TPMS_ATTEST type wrong",nil
	 }
	
	
}