package structures

import (
	"bytes"
	"encoding/hex"
	"encoding/json"
	"fmt"
)

type ExpectedValue struct {
	ItemID      string `json:"itemid",bson:"itemid"`
	Name        string `json:"name",bson:"name"`
	Description string `json:"description",bson:"description"`

	ElementID string `json:"elementid",bson:"elementid"`
	PolicyID  string `json:"policyid",bson:"policyid"`

	EVS map[string]interface{} `json:"evs",bson:"evs"`
}

type KeylimeMBEV struct {
	MBRefstate string
}

type KeylimeIMAEV struct {
	RuntimePolicy string
}

type MarbleRunCoordinatorEV struct {
	SecurityVersion uint
	UniqueID        []byte
	SignerID        []byte
	ProductID       uint16
	Debug           bool
	ValidateTCB     bool
}

type MarbleRunPackageEV struct {
	SecurityVersion uint
	UniqueID        []byte
	SignerID        []byte
	ProductID       uint16
	Debug           bool
}

type MarbleRunMarbleEV struct {
	Package string
	// TODO this is currently missing the other fields, but for a PoC fine for now
}

type MarbleRunInfrastructureEV struct {
	UEID   string `json:"ueid"`
	CPUSVN string `json:"cpusvn"`
	PCESVN string `json:"pcesvn"`
	RootCA string `json:"rootca"`
}

func (e *KeylimeMBEV) Decode(ev ExpectedValue) error {
	e.MBRefstate = ev.EVS["mb_refstate"].(string)

	return nil
}

func (e *KeylimeIMAEV) Decode(ev ExpectedValue) error {
	e.RuntimePolicy = ev.EVS["runtime_policy"].(string)

	return nil
}

func (e *MarbleRunInfrastructureEV) Equal(other MarbleRunInfrastructureEV) bool {
	return e.UEID == other.UEID &&
		e.CPUSVN == other.CPUSVN &&
		e.PCESVN == other.PCESVN &&
		e.RootCA == other.RootCA
}

func (e *MarbleRunInfrastructureEV) Decode(ev ExpectedValue, name string) error {
	properties := ev.EVS["Infrastructures"].(map[string]interface{})
	// TODO replace this hack
	inf, ok := properties[name]
	if !ok {
		return fmt.Errorf("Not in EV")
	}
	s, err := json.Marshal(inf)
	if err != nil {
		return err
	}
	err = json.Unmarshal(s, e)
	if err != nil {
		return err
	}
	return nil

}

func (e *MarbleRunCoordinatorEV) Decode(ev ExpectedValue) error {
	properties := ev.EVS["coordinator"].(map[string]interface{})

	SecurityVersionRaw, ok := properties["SecurityVersion"]
	if !ok {
		return fmt.Errorf("SecurityVersion is missing in EV")
	}
	SecurityVersion := uint(SecurityVersionRaw.(float64))

	UniqueIDS, ok_unique := properties["UniqueID"]
	SignerIDS, ok_signer := properties["SignerID"]
	if !ok_signer && !ok_unique {
		return fmt.Errorf("UniqueID or SignerID need to be specified")
	}

	var UniqueID []byte
	var err error
	if ok_unique {
		UniqueID, err = hex.DecodeString(UniqueIDS.(string))
		if err != nil {
			return fmt.Errorf("UniqueID decode failed")
		}
	}

	var SignerID []byte
	SignerID, err = hex.DecodeString(SignerIDS.(string))
	if err != nil {
		return fmt.Errorf("UniqueID decode failed")
	}

	ProductIDRaw, ok := properties["ProductID"]
	if !ok {
		return fmt.Errorf("ProductID is missing")
	}
	ProductID := uint16(ProductIDRaw.(float64))
	DebugRaw, ok := properties["Debug"]
	Debug := false
	if ok {
		Debug = DebugRaw.(bool)
	}

	ValidateTCB := true
	ValidateTCBRaw, ok := properties["ValidateTCB"]
	if ok {
		ValidateTCB = ValidateTCBRaw.(bool)
	}

	e.SecurityVersion = SecurityVersion
	e.UniqueID = UniqueID
	e.ProductID = ProductID
	e.SignerID = SignerID
	e.Debug = Debug
	e.ValidateTCB = ValidateTCB

	return nil
}

func (e *MarbleRunPackageEV) Decode(ev ExpectedValue, name string) error {
	properties := ev.EVS["Packages"].(map[string]interface{})

	pkg, ok := properties[name]
	if !ok {
		return fmt.Errorf("Not in EV")
	}

	pkgMap := pkg.(map[string]interface{})

	if value, ok := pkgMap["SecurityVersion"]; ok {
		e.SecurityVersion = uint(value.(float64))
	}

	if value, ok := pkgMap["UniqueID"]; ok {
		UniqueID, err := hex.DecodeString(value.(string))
		if err != nil {
			return fmt.Errorf("UniqueID decode failed")
		}
		e.UniqueID = UniqueID
	}

	if value, ok := pkgMap["SignerID"]; ok {
		SignerID, err := hex.DecodeString(value.(string))
		if err != nil {
			return fmt.Errorf("SignerID decode failed")
		}
		e.SignerID = SignerID
	}

	if value, ok := pkgMap["ProductID"]; ok {
		e.ProductID = uint16(value.(float64))
	}

	if value, ok := pkgMap["Debug"]; ok {
		e.Debug = value.(bool)
	}

	return nil
}

func (e *MarbleRunPackageEV) Equal(other MarbleRunPackageEV) bool {
	return e.SecurityVersion == other.SecurityVersion &&
		bytes.Equal(e.UniqueID, other.UniqueID) &&
		bytes.Equal(e.SignerID, other.SignerID) &&
		e.Debug == other.Debug
}

func (e *MarbleRunMarbleEV) Decode(ev ExpectedValue, name string) error {
	properties := ev.EVS["Marbles"].(map[string]interface{})

	marble, ok := properties[name]
	if !ok {
		return fmt.Errorf("Not in EV")
	}
	marbleMap := marble.(map[string]interface{})

	packageRaw, ok := marbleMap["Package"]
	if !ok {
		return fmt.Errorf("Package is missing in EV")
	}
	packageStr := packageRaw.(string)

	e.Package = packageStr

	return nil
}

func (e *MarbleRunMarbleEV) Equal(other MarbleRunMarbleEV) bool {
	return e.Package == other.Package
}
