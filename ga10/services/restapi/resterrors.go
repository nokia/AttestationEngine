package restapi

import(
	"fmt"
)

type restErrorMessage struct {
	ErrorMessage string     `json:"error",bson:"error"`
}

func MakeRESTErrorMessage(e error) restErrorMessage{
	return restErrorMessage{ fmt.Sprintf("%v",e.Error())  }
}