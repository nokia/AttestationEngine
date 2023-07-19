package structures

type ExpectedValue struct {
        ItemID string                           `json:"itemid",bson:"itemid"`
        Name string                             `json:"name",bson:"name"`
        Description string                      `json:"description",bson:"description"`

        ElementID string                        `json:"elementid",bson:"elementid"`
        PolicyID string                         `json:"policyid",bson:"policyid"`

        EVS map[string]interface{}              `json:"evs",bson:"evs"`
}
 