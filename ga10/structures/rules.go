package structures

//This is the type of the protocol functions
//
// In useage it takes the element, policy, session and a "json" structure of parameters
// Return is "json" and a string containing a message from the rule and error in case everything fails
type RuleCall func(Claim, string, ExpectedValue, Session, map[string]interface{})  (ResultValue, string, error)

type Rule struct {
        Name string                            
        Description string                     
        CallFunction RuleCall
        NeedsEV bool
}


