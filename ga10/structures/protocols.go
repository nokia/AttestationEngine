package structures

//This is the type of the protocol functions
//
// In useage it takes the element, policy, session and a "json" structure of parameters
// Return is "json" and a string containing any error messages
type ProtocolCall func(Element, Policy, Session, map[string]interface{}) (map[string]interface{}, map[string]interface{}, string) 


type Protocol struct  {
        Name string                            
        Description string                        
        CallFunction ProtocolCall
        Intents []string
}

