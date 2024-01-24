package ima

import (
	"encoding/base64"
	"fmt"
	"io/ioutil"
	"net/http"
	utilities "ta10/common"

	"github.com/labstack/echo/v4"
)

const IMALOGLOCATION string = "/sys/kernel/security/ima/ascii_runtime_measurements"

type returnASCIILog struct {
	ASCIILog        string `json:"asciilog"`
	Encoding        string `json:"encoded"`
	UnEncodedLength int    `json:"unencodedlength"`
	EncodedLength   int    `json:"encodedlength"`
}

func GetEventLogLocation(loc string) string {
	fmt.Printf("IMA Log requested from %v, unsafe mode is %v, giving: ", loc, utilities.IsUnsafe())

	if utilities.IsUnsafe() == true {
		fmt.Printf("%v\n", loc)
		return loc
	} else {
		fmt.Printf("%v\n", IMALOGLOCATION)
		return IMALOGLOCATION
	}
}

func ASCIILog(c echo.Context) error {
	fmt.Println("ima ascii called")

	var postbody map[string]interface{}
	var rtnbody = make(map[string]interface{})

	if err := c.Bind(&postbody); err != nil {
		rtnbody["postbody"] = err.Error()
		return c.JSON(http.StatusBadRequest, rtnbody)
	}

	u := GetEventLogLocation(fmt.Sprintf("%v", postbody["ima/ASCIIlog"]))

	fcontent, err := ioutil.ReadFile(u)
	if err != nil {
		rtnbody["file err"] = err.Error()
		return c.JSON(http.StatusInternalServerError, rtnbody)
	}
	scontent := base64.StdEncoding.EncodeToString(fcontent)

	rtn := returnASCIILog{scontent, "base64", len(fcontent), len(scontent)}
	return c.JSON(http.StatusOK, rtn)
}
