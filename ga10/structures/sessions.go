package structures


type Session struct {
        ItemID string                           `json:"itemid",bson:"itemid"`        
        Timing SessionTiming                    `json:"timing",bson:"timing"`

        ClaimList []string                      `json:"claimlist",bson:"claimlist"` 
        ResultList []string                     `json:"resultlist",bson:"resultlist"` 

        Message string                           `json:"message",bson:"message"`

        Footer SessionFooter                     `json:"footer",bson:"footer"`
}
 

type SessionSummary struct {
        ItemID string                           `json:"itemid",bson:"itemid"`        
        Timing SessionTiming                    `json:"timing",bson:"timing"`
}

  
type SessionTiming struct {
        Opened Timestamp              `json:"opened",bson:"opened"`
        Closed Timestamp              `json:"closed",bson:"closed"`
}

 
type SessionFooter struct {
        Hash []byte                              `json:"hash",bson:"hash"`
        Signature []byte                         `json:"signature",bson:"signature"`       
}
