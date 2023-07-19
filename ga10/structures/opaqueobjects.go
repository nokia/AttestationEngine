package structures

type OpaqueObject struct {
        Value string                          `json:"value",bson:"value"`        
        Type string                           `json:"type",bson:"type"`                
        ShortDescription string               `json:"shortdescription",bson:"shortdescription"`                
        LongDescription string                `json:"longdescription",bson:"longdescription"`        
}        
