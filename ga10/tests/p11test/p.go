package main

import(
	"fmt"

	"github.com/miekg/pkcs11/p11"	
	"github.com/miekg/pkcs11"	

)


func main() {
	fmt.Println("Starting")

	module,_ := p11.OpenModule("/usr/lib/x86_64-linux-gnu/pkcs11/yubihsm_pkcs11.so")
	slots,_ := module.Slots()
	fmt.Printf("Slots %v\n",slots)

	session,_ := slots[0].OpenSession()
	fmt.Printf("Session %v\n",session)

	err := session.Login("0001password")
	fmt.Printf("Login error %v\n",err)

	objects,err := session.FindObjects(nil)
	fmt.Printf("Number of objects %v\n",len(objects))

	fmt.Printf("All objects\n")
	for i,v := range objects {
		l,_ := v.Label()
		//cka,_ := v.Value()
		attrpublic,_ := v.Attribute(pkcs11.CKO_PUBLIC_KEY)
		attrprivate,_ := v.Attribute(pkcs11.CKO_PRIVATE_KEY)

		fmt.Printf("#%v label is %v,  PUB %v, PRIV %v\n",i,l,attrpublic,string(attrprivate))
	}


	template := []*pkcs11.Attribute{ 
		//pkcs11.NewAttribute(pkcs11.CKA_LABEL, "rsa2048_ian"),
		pkcs11.NewAttribute(pkcs11.CKO_PUBLIC_KEY, nil),
		//pkcs11.NewAttribute(pkcs11.CKA_ENCRYPT, nil),

	 }
	fmt.Printf("Getting attributes for template %v\n",template)

	privatekey,err := session.FindObjects(template)
	fmt.Printf("Private key is %v\n",privatekey)



	fmt.Println("Logging out")
	session.Logout()
	fmt.Println("Closing")	
	session.Close()
	fmt.Println("Done")

}
