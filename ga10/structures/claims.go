package structures



type Claim struct {
        ItemID string                           `json:"itemid",bson:"itemid"`       
        BodyType string                         `json:"bodytype",bson:"bodytype"`        
        Header ClaimHeader                      `json:"header",bson:"header"`
        Body map[string]interface{}             `json:"body",bson:"body"`
        Footer ClaimFooter                      `json:"footer",bson:"footer"`
}
 

type ClaimHeader struct {
        Element Element                                 `json:"element",bson:"element"`
        Policy Policy                                   `json:"policy",bson:"policy"`
        Session Session                                 `json:"session",bson:"session"`
        Timing Timing                                   `json:"timing",bson:"timing"`
        AdditionalParameters map[string]interface{}     `json:"aps",bson:"aps"`
        CallParameters map[string]interface{}           `json:"cps",bson:"cps"`

}
         
type Timing struct {
        Requested   Timestamp                   `json:"requested",bson:"requested"`
        Received  Timestamp                     `json:"received",bson:"received"`
}

type ClaimFooter struct {
        Hash []byte                             `json:"hash",bson:"hash"`
        Signature []byte                        `json:"signature",bson:"signature"`       
}

const CLAIMERROR = "*ERROR"