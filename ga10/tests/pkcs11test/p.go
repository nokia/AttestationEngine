package main

import(
	"fmt"

	"github.com/miekg/pkcs11"	
)

func main() {
	fmt.Println("Starting")

	p := pkcs11.New("/usr/lib/x86_64-linux-gnu/pkcs11/yubihsm_pkcs11.so")
	fmt.Printf("PKCS11 module is %v\n",p)

	err := p.Initialize()
	fmt.Printf("PKCS11 initialisation is %v\n",err)

	defer p.Destroy()
	defer p.Finalize()

	slots, err := p.GetSlotList(true)
	fmt.Printf("Slots %v , %v\n",err,slots)


}