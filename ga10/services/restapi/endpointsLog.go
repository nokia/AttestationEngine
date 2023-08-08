package restapi

import(
	"log"
	"reflect"

	"net/http"
	"strconv"

	"a10/operations"
	"a10/structures"
)

import(
	"github.com/labstack/echo/v4"
)

type returnLogEntries struct {
	LogEntries  []structures.LogEntry  `json:"logentries"`
	Length    int                      `json:"length"`
	Max	      int64                    `json:"max"`
}


func getLogEntries(c echo.Context) error {
	max_query := c.QueryParam("max")
	max, err := strconv.ParseInt(max_query, 10, 64)
	if err != nil {
		max=200
	}
	log.Println("maxtype is ",reflect.TypeOf(max))


	logentries,err := operations.GetLogEntries(max)

	if err != nil {
		return c.JSON(http.StatusInternalServerError,MakeRESTErrorMessage(err))
	} else {
		rtn := returnLogEntries{ logentries, len(logentries), max }
		return c.JSON(http.StatusOK, rtn)
	}
}


type returnLogEntriesSince struct {
	LogEntries  []structures.LogEntry  `json:"logentries"`
	Length    int                      `json:"length"`
	Duration  string                    `json:"duration"`
}


func getLogEntriesSince(c echo.Context) error {
	duration_query := c.QueryParam("duration")

	logentries,err := operations.GetLogEntriesSince(duration_query)

	if err != nil {
		return c.JSON(http.StatusInternalServerError,MakeRESTErrorMessage(err))
	} else {
		rtn := returnLogEntriesSince{ logentries, len(logentries), duration_query }
		return c.JSON(http.StatusOK, rtn)
	}
}