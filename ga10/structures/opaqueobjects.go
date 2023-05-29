package structures

type OpaqueObject struct {
        Value string                          `json:"value",bson:"value"`        
        Type string                           `json:"type",bson:"type"`                
        ShortDescription string               `json:"shortDescription",bson:"shortDescription"`                
        LongDescription string                `json:"longDescription",bson:"longDescription"`        
}        
