package rules

import (

	"a10/structures"
	"a10/operations"

	"a10/rules/nullrule"
	"a10/rules/tpm2rules"
	"a10/rules/testcontainerrules"

)


func RegisterRules() {
	registerListOfRules(nullrule.Registration())
	registerListOfRules(tpm2rules.Registration())
	registerListOfRules(testcontainerrules.Registration())

}


func registerListOfRules(rs []structures.Rule) {
	for _,r := range rs {
		operations.AddRule(r)
	}
}
