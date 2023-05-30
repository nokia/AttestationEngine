package uefi

import(
	"net/http"
	"fmt"
	"io/ioutil"
	"encoding/base64"

	"github.com/labstack/echo/v4"
)

type returnEventLog struct {
	EventLog    string       `json:"eventlog"`
	Encoding   string        `json:"encoded"`
	UnEncodedLength int    `json:"unencodedlength"`
	EncodedLength int      `json:"encodedlength"`
}

func Eventlog(c echo.Context) error {
	fmt.Println("eventlog called")

	var postbody  map[string]interface{}
	var rtnbody = make( map[string]interface{} )

	if err := c.Bind(&postbody); err != nil {	
		rtnbody["postbody"] = err.Error() 
		return c.JSON(http.StatusUnprocessableEntity, rtnbody)
	}

	u := fmt.Sprintf("%v",postbody["uefi/eventlog"])
	
	fcontent,err := ioutil.ReadFile(u)
	if err != nil {
		rtnbody["file err"]=err.Error() 
		return c.JSON(http.StatusInternalServerError, rtnbody)
	}
	scontent := base64.StdEncoding.EncodeToString(fcontent)

	rtn := returnEventLog{ scontent, "base64", len(fcontent), len(scontent) }
	return c.JSON(http.StatusOK, rtn)
}
