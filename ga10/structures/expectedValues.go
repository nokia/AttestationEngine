package structures

type ExpectedValue struct {
        ItemID string                           `json:"itemid",bson:"itemid"`
        Name string                             `json:"name",bson:"name"`
        Description string                      `json:"description",bson:"description"`

        ElementID string                        `json:"elementID",bson:"elementID"`
        PolicyID string                         `json:"policyID",bson:"policyID"`

        EVS map[string]interface{}              `json:"evs",bson:"evs"`
}
 