package webui

import(
	"net/http"
	"strconv"

	"github.com/labstack/echo/v4"

    "a10/operations"
    "a10/structures"
)

type logstr struct {
	LogEntries    []structures.LogEntry
	Count         int64
	Amount		  int64
}

func showLog(c echo.Context) error {
	max_query := c.QueryParam("max")
	max, err := strconv.ParseInt(max_query, 10, 64)
	if err != nil {
		max=200
	}

	es,_ := operations.GetLogEntries(max)
	nl   := operations.CountLogEntries()

	ls := logstr{ es, nl, max}

	return c.Render(http.StatusOK, "log.html",ls)
}



