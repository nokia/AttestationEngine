package rules

import (
	"a10/operations"
	"a10/structures"

	"a10/rules/keylime"
	"a10/rules/marblerun"
	"a10/rules/nullrule"
	"a10/rules/sysrules"
	"a10/rules/tpm2rules"

	"a10/rules/testcontainerrules"
)

func RegisterRules() {
	registerListOfRules(nullrule.Registration())
	registerListOfRules(tpm2rules.Registration())
	registerListOfRules(marblerun.Registration())
	registerListOfRules(testcontainerrules.Registration())
	registerListOfRules(sysrules.Registration())
	registerListOfRules(keylime.Registration())
}

func registerListOfRules(rs []structures.Rule) {
	for _, r := range rs {
		operations.AddRule(r)
	}
}
