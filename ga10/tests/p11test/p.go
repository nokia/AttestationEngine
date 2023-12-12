package main

import(
	"fmt"
	"reflect"

	"github.com/miekg/pkcs11/p11"	
	"github.com/miekg/pkcs11"	

)


func main() {
	fmt.Println("Starting")

	module,_ := p11.OpenModule("/usr/lib/x86_64-linux-gnu/pkcs11/yubihsm_pkcs11.so")
	//module,_ := p11.OpenModule("/usr/local/lib/softhsm/libsofthsm2.so")

	

	slots,_ := module.Slots()
	fmt.Printf("Slots %v\n",slots)

	session,_ := slots[0].OpenSession()
	fmt.Printf("Session %v\n",session)

	//err := session.Login("1234")
	err := session.Login("0001password")

	fmt.Printf("Login error %v\n",err)

	objects,err := session.FindObjects(nil)
	fmt.Printf("Number of objects %v\n",len(objects))

	fmt.Printf("All objects\n")
	for i,v := range objects {
		l,_ := v.Label()
		cka,_ := v.Value()
		attrpublic,_ := v.Attribute(pkcs11.CKO_PUBLIC_KEY)
		attrprivate,_ := v.Attribute(pkcs11.CKO_PRIVATE_KEY)

		fmt.Printf("#%v label is %v,  CKA %v PUB %v, PRIV %v\n",i,l,cka,attrpublic,string(attrprivate))
	}


	template := []*pkcs11.Attribute{ 
		pkcs11.NewAttribute(pkcs11.CKA_LABEL, "asymkey"),
		//pkcs11.NewAttribute(pkcs11.CKA_LABEL, "fred"),
	 }
	fmt.Printf("\nGetting attributes for template %v\n",template)

	filteredobjects,err := session.FindObjects(template)
	fmt.Printf("Private key is %v\n",filteredobjects)
	for i,v := range filteredobjects {
		l,_ := v.Label()
		cka,_ := v.Value()
		attrpublic,_ := v.Attribute(pkcs11.CKO_PUBLIC_KEY)
		attrprivate,_ := v.Attribute(pkcs11.CKO_PRIVATE_KEY)

		fmt.Printf("#%v label is %v,  CKA %v PUB %v, PRIV %v\n",i,l,cka,attrpublic,string(attrprivate))
	}

	privateKey := p11.PrivateKey(filteredobjects[0])
    publicKey :=  p11.PublicKey(filteredobjects[1])
	fmt.Printf("  private key %v , %v \n",privateKey, publicKey)
	fmt.Printf("  types %v , %v \n", reflect.TypeOf(privateKey),reflect.TypeOf(publicKey))

    plaintext := []byte("Croeso!")
    mechanism :=pkcs11.NewMechanism(pkcs11.CKM_RSA_PKCS,nil) 

    enc,err := publicKey.Encrypt( *mechanism,plaintext)
    fmt.Printf(" plaintext %v, %v -> %v \n",err,plaintext, enc)

    dec,err := privateKey.Decrypt(  *mechanism, enc)
    fmt.Printf("     %v -> %v \n",err,dec)



	fmt.Println("Logging out")
	session.Logout()
	fmt.Println("Closing")	
	session.Close()
	fmt.Println("Done")

}
