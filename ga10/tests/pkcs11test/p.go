package main

import(
	"fmt"

	"github.com/miekg/pkcs11"	
)

func main() {
	fmt.Println("Starting")

	p := pkcs11.New("/usr/lib/x86_64-linux-gnu/pkcs11/yubihsm_pkcs11.so")

	//p := pkcs11.New("/usr/lib/x86_64-linux-gnu/pkcs11/gnome-keyring-pkcs11.so")

	
	fmt.Printf("PKCS11 module is %v\n",p)

	err := p.Initialize()
	fmt.Printf("PKCS11 initialisation is %v\n",err)

	defer p.Destroy()
	defer p.Finalize()

	slots, err := p.GetSlotList(true)
	fmt.Printf("err = %v , slots = %v\n",err,slots)

	fmt.Println("SLOTS =========================================")
	for i,v := range slots {
		slotinfo, err := p.GetSlotInfo(v)
		fmt.Printf("#%v err = %v, desc=%v,man=%v,flags=%v,hw=%v,fw=%v\n", i, err, slotinfo.SlotDescription, slotinfo.ManufacturerID,slotinfo.Flags,slotinfo.HardwareVersion,slotinfo.FirmwareVersion)
	}

	fmt.Println("TOKENS =========================================")
	for _,v := range slots {
		tokeninfo, err := p.GetTokenInfo(v)
		fmt.Printf("err = %v, info =%v\n", err, tokeninfo)
	}

	fmt.Println("MECHANISMS =========================================")
	mchs, err := p.GetMechanismList(slots[0])
	fmt.Printf("err = %v, mchs =%v\n", err, mchs)

	


	fmt.Println("\nOpening Session")
	session, err := p.OpenSession(slots[0], pkcs11.CKF_SERIAL_SESSION)
	fmt.Printf("Session err %v session %v\n",err,session)

	fmt.Println("\nLogging In")
	err = p.Login(session, pkcs11.CKU_USER, "0001password")
	fmt.Printf("Login err %v \n",err)

	fmt.Println("\nFinding Objects")

	template := []*pkcs11.Attribute{ 
		pkcs11.NewAttribute(pkcs11.CKA_LABEL, "rsa2048_ian"),
		pkcs11.NewAttribute(pkcs11.CKO_PRIVATE_KEY, nil),
	 }

	if e := p.FindObjectsInit(session, template); e != nil {
		fmt.Printf("Failed FindObjectsInit")
	}

	objs, b, err := p.FindObjects(session,10)
	for i,oh := range objs {
		fmt.Printf("#%v , err=%v, bools=%v, objecthandle = %v %v\n",i,err,b,oh)

		ats := []*pkcs11.Attribute{
			pkcs11.NewAttribute( pkcs11.CKA_LABEL, nil),
		}

		attr,err := p.GetAttributeValue(session, pkcs11.ObjectHandle(oh), ats)
		fmt.Printf("  +.... %v attr= %v \n",err,attr)

		//obj := pkcs11.ObjectHandle(oh)
		//fmt.Printf("  +---- %v",obj.Label())
	}

	if e:=p.FindObjectsFinal(session); e != nil {
		fmt.Printf("Failed FindObjectsFinal")

	}

	


	fmt.Println("\nClosing THings")
	p.Logout(session)
	p.CloseSession(session)


}