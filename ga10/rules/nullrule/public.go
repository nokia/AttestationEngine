package nullrule

import(
	"a10/structures"
)


func Registration() []structures.Rule {

	ruleS := structures.Rule{ "null_success","A rule that always returns success", CallruleS, false}
	ruleF := structures.Rule{ "null_fail","A rule that always returns fail", CallruleF, false}

	ruleV := structures.Rule{ "null_verifycallfail","A rule that always returns verifycallfail error", CallruleV, false}
	ruleN := structures.Rule{ "null_noresult","A rule that always returns noresult error", CallruleN, false}
	ruleE := structures.Rule{ "null_verifycallerrorattempt","A rule that always returns verifycallerrorattempt error", CallruleE, false}

	ruleMEV := structures.Rule{ "null_missingEV","A rule that always returns missing expected value error", CallruleMEV, false}
	ruleRCF := structures.Rule{ "null_rulecallfailure","A rule that always returns rule call failure error", CallruleRCF, false}
	ruleU   := structures.Rule{ "null_unsetresultvalue","A rule that always returns unste result value error", CallruleU, false}


	return []structures.Rule{ ruleS, ruleF, ruleV, ruleN, ruleE, ruleMEV, ruleRCF, ruleU }
}

func CallruleS(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{})  (structures.ResultValue, string, error)  {
	return structures.Success,"This a demonstration of a success result",nil
}

func CallruleF(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{})  (structures.ResultValue, string, error)  {
	return structures.Fail,"This a demonstration of a fail result",nil
}

func CallruleE(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{})  (structures.ResultValue, string, error)  {
	return structures.VerifyClaimErrorAttempt,"This a demonstration of verifycallerrorattempt",nil
}

func CallruleV(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{})  (structures.ResultValue, string, error)  {
	return structures.VerifyCallFailure,"This a demonstration of a verify call fail result",nil
}

func CallruleN(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{})  (structures.ResultValue, string, error)  {
	return structures.NoResult,"This a demonstration of no result",nil
}



func CallruleMEV(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{})  (structures.ResultValue, string, error)  {
	return structures.MissingExpectedValue,"This a demonstration of missing expected value",nil
}
func CallruleRCF(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{})  (structures.ResultValue, string, error)  {
	return structures.RuleCallFailure,"This a demonstration of rule call failure",nil
}
func CallruleU(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{})  (structures.ResultValue, string, error)  {
	return structures.UnsetResultValue,"This a demonstration of unset result value",nil
}


