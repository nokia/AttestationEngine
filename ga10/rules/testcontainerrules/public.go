package testcontainerrules

import(
	"a10/structures"
	"fmt"
	"reflect"
)


func Registration() []structures.Rule {

	ruleN := structures.Rule{ "testcontainer_LIFE","The rule returns success if the nunber is 42", CallruleN, false}
	ruleX := structures.Rule{ "testcontainer_43","The rule returns success if the nunber is 43 - which will not happen", CallruleX, false}
	ruleF := structures.Rule{ "testcontainer_FOOBAR","The rule returns success if the response to foo is bar", CallruleF, false}

	return []structures.Rule{ ruleN, ruleX, ruleF }
}

func CallruleN(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{})  (structures.ResultValue, string, error)  {

	number, ok := (claim.Body)["aNumber"]
	if !ok {
		return structures.VerifyCallFailure, "Missing body",nil
	}



	if number==int32(42) {
		return structures.Success, "Now what is the question?",nil
	} else {
		return structures.Fail, "Earth 2 required",nil
	}

}



func CallruleX(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{})  (structures.ResultValue, string, error)  {

	number, ok := (claim.Body)["aNumber"]
	if !ok {
		return structures.VerifyCallFailure, "Missing body",nil
	}

	if number==int32(43) {
		return structures.Success, "Correctly got the wrong answer",nil
	} else {
		return structures.Fail, "Earth 2 with more mice required",nil
	}
}



func CallruleF(claim structures.Claim, rule string, ev structures.ExpectedValue, session structures.Session, parameter map[string]interface{})  (structures.ResultValue, string, error)  {
	
	foobar, ok := (claim.Body)["foo"]
	if !ok {
		return structures.VerifyCallFailure, "Missing body",nil
	}

	fmt.Printf("\n\nNumber is %v type is %v == %v %v\n\n",foobar,reflect.TypeOf(foobar), foobar=="bar",reflect.TypeOf("bar"))


	if foobar=="bar" {
		return structures.Success, "Correctly got the wrong answer",nil
	} else {
		return structures.Fail, "Earth 2 with more mice required",nil
	}
}
