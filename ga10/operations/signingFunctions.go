package operations

import(
	"fmt"

	"crypto"
	"crypto/rsa"
	"crypto/rand"

	"a10/structures"
	"a10/utilities"	
)

var publickey *rsa.PublicKey
var privatekey *rsa.PrivateKey

func init() {
	fmt.Printf("generating private, public key pair for claim signing - just for this session so no chance to verify later. THese keys MUST be external\n")

	privatekey,_ = rsa.GenerateKey(rand.Reader, 2048)
	publickey = &privatekey.PublicKey
}



func hashAndSignClaim(h hashablePartClaim) (structures.ClaimFooter,error) {
	digest,err := utilities.MakeSHA256(h)
    	if err != nil {
    		return structures.ClaimFooter{ []byte{}, []byte{} }, fmt.Errorf("hashAndSignfailed with %v",err)
	} 

	signature, _ := rsa.SignPSS(rand.Reader, privatekey, crypto.SHA256, digest, nil)

	err = rsa.VerifyPSS(publickey, crypto.SHA256, digest, signature, nil)
	if err!=nil {
    		return structures.ClaimFooter{ []byte{}, []byte{} }, fmt.Errorf("Verification of hashAndSignfailed with %v",err)
	} 

	return structures.ClaimFooter{ digest, signature }, nil
}





func hashAndSignResult(h hashablePartResult) (structures.ResultFooter,error) {
	digest,err := utilities.MakeSHA256(h)
    	if err != nil {
    		return structures.ResultFooter{ []byte{}, []byte{} }, fmt.Errorf("hashAndSignfailed with %v",err)
	} 

	signature, _ := rsa.SignPSS(rand.Reader, privatekey, crypto.SHA256, digest, nil)

	err = rsa.VerifyPSS(publickey, crypto.SHA256, digest, signature, nil)
	if err!=nil {
    		return structures.ResultFooter{ []byte{}, []byte{} }, fmt.Errorf("Verification of hashAndSignfailed with %v",err)
	} 

	return structures.ResultFooter{ digest, signature }, nil
}




func hashAndSignSession(h hashablePartSession) (structures.SessionFooter,error) {
	digest,err := utilities.MakeSHA256(h)
    	if err != nil {
    		return structures.SessionFooter{ []byte{}, []byte{} }, fmt.Errorf("hashAndSignfailed with %v",err)
	} 

	signature, _ := rsa.SignPSS(rand.Reader, privatekey, crypto.SHA256, digest, nil)

	err = rsa.VerifyPSS(publickey, crypto.SHA256, digest, signature, nil)
	if err!=nil {
    		return structures.SessionFooter{ []byte{}, []byte{} }, fmt.Errorf("Verification of hashAndSignfailed with %v",err)
	} 

	return structures.SessionFooter{ digest, signature }, nil
}