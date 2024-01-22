package uefi

import (
	"encoding/base64"
	"fmt"
	"io/ioutil"
	"net/http"

	utilities "ta10/common"

	"github.com/labstack/echo/v4"
)

const UEFIEVENTLOGLOCATION string = "/sys/kernel/security/tpm0/binary_bios_measurements"

type returnEventLog struct {
	EventLog        string `json:"eventlog"`
	Encoding        string `json:"encoded"`
	UnEncodedLength int    `json:"unencodedlength"`
	EncodedLength   int    `json:"encodedlength"`
}

func GetEventLogLocation(loc string) string {
	fmt.Printf("UEFI Log requested from %v, unsafe mode is %v, giving: ", loc, utilities.IsUnsafe())

	if utilities.IsUnsafe() == true {
		fmt.Printf("%v\n", loc)
		return loc
	} else {
		fmt.Printf("%v\n", UEFIEVENTLOGLOCATION)
		return UEFIEVENTLOGLOCATION
	}
}

func Eventlog(c echo.Context) error {
	fmt.Println("eventlog called")

	var postbody map[string]interface{}
	var rtnbody = make(map[string]interface{})

	if err := c.Bind(&postbody); err != nil {
		rtnbody["postbody"] = err.Error()
		return c.JSON(http.StatusUnprocessableEntity, rtnbody)
	}

	u := GetEventLogLocation(fmt.Sprintf("%v", postbody["uefi/eventlog"]))

	fcontent, err := ioutil.ReadFile(u)
	if err != nil {
		rtnbody["file err"] = err.Error()
		return c.JSON(http.StatusInternalServerError, rtnbody)
	}
	scontent := base64.StdEncoding.EncodeToString(fcontent)

	rtn := returnEventLog{scontent, "base64", len(fcontent), len(scontent)}
	return c.JSON(http.StatusOK, rtn)
}
