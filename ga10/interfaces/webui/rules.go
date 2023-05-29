package webui

import(
	"net/http"

	"github.com/labstack/echo/v4"

    "a10/operations"
)


func showRules(c echo.Context) error {
	rs := operations.GetRules()

	return c.Render(http.StatusOK, "rules.html",rs)
}

