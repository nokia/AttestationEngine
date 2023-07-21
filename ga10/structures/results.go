package structures

type ResultValue int

const (
        Success ResultValue              = 0
        Fail                             = 9001
        
        VerifyCallFailure                = 9010
        VerifyClaimErrorAttempt          = 9098
        NoResult                         = 9099

        MissingExpectedValue             = 9997
        RuleCallFailure                  = 9998
        UnsetResultValue                 = 9999
)

type Result struct {
        ItemID string                           `json:"itemid",bson:"itemid"`
        ClaimID string                          `json:"claimid",bson:"claimid"`
        ClaimFooter ClaimFooter                 `json:"claimfooter",bson:"claimfooter"`        
        Session Session                         `json:"session",bson:"session"`
        ElementID string                        `json:"elementid",bson:"elementid"`        
        ExpectedValue ExpectedValue             `json:"expectedvalue",bson:"expectedvalue"`
        Parameters map[string]interface{}       `json:"parameters",bson:"parameters"`
        Message string                          `json:"message",bson:"message"`
        RuleName string                         `json:"rulename",bson:"rulename"`
        VerifiedAt Timestamp                    `json:"verifiedat",bson:"verifiedat"`
        Result ResultValue                      `json:"result",bson:"result"`
        Footer ResultFooter                     `json:"footer",bson:"footer"`
}
 
type ResultFooter struct {
        Hash []byte                              `json:"hash",bson:"hash"`
        Signature []byte                         `json:"signature",bson:"signature"`       
}
