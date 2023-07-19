package structures



type LogEntry struct {
        ItemID string                          `json:"itemid",bson:"itemid"`   
        Timestamp Timestamp                    `json:"timestamp",bson:"timestamp"`   
        Channel string                         `json:"channel",bson:"channel"`   
        Operation string                       `json:"operation",bson:"operation"`   
        RefID string                           `json:"refid",bson:"refid"`   
        RefType string                         `json:"reftype",bson:"reftype"`   
        Message string                         `json:"message",bson:"message"`   
        Hash []byte                            `json:"hash",bson:"hash"`  
}
