package structures

type Policy struct {
        ItemID string                           `json:"itemid",bson:"itemid"`
        Name string                             `json:"name",bson:"name"`
        Description string                      `json:"description",bson:"description"`
        Intent string                           `json:"intent",bson:"intent"`
        Parameters map[string]interface{}       `json:"parameters",bson:"parameters"`
}
 