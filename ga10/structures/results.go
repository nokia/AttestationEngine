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
        ClaimID string                          `json:"claimID",bson:"name"`
        ClaimFooter ClaimFooter                 `json:"claimFooter",bson:"claimFooter"`        
        Session Session                         `json:"session",bson:"description"`
        ExpectedValue ExpectedValue             `json:"expectedValue",bson:"intent"`
        Parameters map[string]interface{}       `json:"parameters",bson:"parameters"`
        Message string                          `json:"message",bson:"description"`
        RuleName string                         `json:"ruleName",bson:"description"`
        VerifiedAt Timestamp                    `json:"verifiedAt",bson:"description"`
        Result ResultValue                      `json:"result",bson:"result"`
        Footer ResultFooter                     `json:"footer",bson:"footer"`
}
 
type ResultFooter struct {
        Hash []byte                              `json:"hash",bson:"hash"`
        Signature []byte                         `json:"signature",bson:"signature"`       
}
