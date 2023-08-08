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
	Duration	  string	
}

func showLog(c echo.Context) error {
	max_query := c.QueryParam("max")
	max, err := strconv.ParseInt(max_query, 10, 64)
	if err != nil {
		max=200
	}

	es,_ := operations.GetLogEntries(max)
	nl   := operations.CountLogEntries()

	ls := logstr{ es, nl, max, "always"}

	return c.Render(http.StatusOK, "log.html",ls)
}



func showLogSince(c echo.Context) error {
	duration := c.QueryParam("duration")

	es,_ := operations.GetLogEntriesSince(duration)
	nl   := int64(len(es))

	ls := logstr{ es, nl, nl, duration }

	return c.Render(http.StatusOK, "log.html",ls)
}