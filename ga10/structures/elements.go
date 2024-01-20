package structures

type Element struct {
	ItemID      string   `json:"itemid,omitempty",bson:"itemid,omitempty"`
	Name        string   `json:"name",bson:"name"`
	Description string   `json:"description",bson:"description"`
	Endpoint    string   `json:"endpoint",bson:"endpoint"`
	Protocol    string   `json:"protocol",bson:"protocol"`
	Tags        []string `json:"tags",bson:"tags"`

	Sshkey           SSHKEY           `json:"sshkey,omitempty",bson:"sshkey,omitempty"`
	TPM2             TPM2             `json:"tpm2,omitempty",bson:"tpm2,omitempty"`
	UEFI             UEFI             `json:"uefi,omitempty",bson:"uefi,omitempty"`
	IMA              IMA              `json:"ima,omitempty",bson:"ima,omitempty"`
	TXT              TXT              `json:"txt,omitempty",bson:"txt,omitempty"`
	Host			 HostMachine      `json:"host,omitempty",bson:"host,omitempty"`
	MRCoordinator    MRCoordinator    `json:"mrcoordinator,omitempty" bson:"mrcoordinator,omitempty"`
	MRMarbleInstance MRMarbleInstance `json:"mrmarbleinstance,omitempty" bson:"mrmarbleinstance,omitempty"`
}

type HostMachine struct {
	OS          string `json:"os",bson:"os"`
	Arch        string `json:"arch",bson:"arch"`	
	Hostname    string `json:"hostname",bson:"hostname"`
}

type SSHKEY struct {
	Key      string `json:"key",bson:"key"`
	Timeout  int16  `json:"timeout",bson:"timeout"`
	Username string `json:"username",bson:"username"`
}

type UEFI struct {
	Eventlog string `json:"eventlog",bson:"eventlog"`
}

type IMA struct {
	ASCIILog string `json:"asciilog",bson:"asciilog"`
}

type TXT struct {
	Log string `json:"log",bson:"log"`
}

type TPM2 struct {
	Device       string `json:"device",bson:"device"`
	EKCertHandle string `json:"ekcerthandle",bson:"ekcerthandle"`
	EK           TPMKey `json:"ek",bson:"ek"`
	AK           TPMKey `json:"ak",bson:"ak"`
}

type TPMKey struct {
	Handle string `json:"handle",bson:"handle"`
	// Public portion of the key marshalled as TPM2BPublic
	Public string `json:"public",bson:"public"`
}

type MRCoordinator struct {
	Certs []string `json:"certs" bson:"certs"`
}

type MRMarbleInstance struct {
	ExpectedNonce    string `json:"expectednonce" bson:"expectednonce"`
	RequestData      string `json:"requestdata" bson:"requestdata"`
	RequestSignature string `json:"requestsignature" bson:"requestsignature"`
}
