package sysrules

import(
	"a10/structures"
)


func Registration() []structures.Rule {

	ruleS := structures.Rule{ "sys_taRunningSafely","Checks the the TA is NOT in unsafe mode of operation", Callrulesafe, false}
	

	return []structures.Rule{ ruleS }
}

func Callrulesafe(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{})  (structures.ResultValue, string, error)  {

	unsafevalue, ok := claim.Body["unsafe"]
	if !ok {
		return structures.RuleCallFailure, "TA not of correct type, or not reporting unsafe parameter value", nil
	}

	if unsafevalue == true {
		return structures.Fail, "TA operating in UNSAFE mode", nil
	} else {
		return structures.Success, "TA operating in safe mode", nil
	}
}
