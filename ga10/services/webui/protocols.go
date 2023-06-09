package webui

import(
	"net/http"

	"github.com/labstack/echo/v4"

    "a10/operations"
)


func showProtocols(c echo.Context) error {
	ps := operations.GetProtocols()

	return c.Render(http.StatusOK, "protocols.html",ps)
}

