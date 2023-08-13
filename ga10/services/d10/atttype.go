package main

//
// Attestation Script Structure
//

type AttestationScript struct {
	Name			string           	`yaml:"name"`
	Description  	string           	`yaml:"description"`
	Collections		[]Collection		`yaml:"collections"`
	Templates		[]Template			`yaml:"templates"`
}

//
// Collection Structure
//

type Collection struct {
	Name			string           	`yaml:"name"`
	Apply			string 				`yaml:"apply"`
	Include			ElementSelector   	`yaml:"include"`
	Exclude			ElementSelector   	`yaml:"exclude"`
}

type ElementSelector struct {
   Names        	[]string            `yaml:"names"`
   Tags         	[]string            `yaml:"tags"`
   ItemIDs      	[]string            `yaml:"itemids"`
}

//
// Template Structure
//

type Template struct {
   Name            string            	`yaml:"name"`
   Decision        string           	`yaml:"decision"`
   Attestations    []Attestation   		`yaml:"attestations"`
}

type Attestation struct {
   Policy            string            	`yaml:"policy"`
   Parameters        string            	`yaml:"parameters"`
   Verifications     []VerificationStr  `yaml:"verifications"`
}

type VerificationStr struct {
   Rule              string            `yaml:"rule"`
   Parameters        string            `yaml:"parameters"`
   Out    			 string            `yaml:"out"`
}
