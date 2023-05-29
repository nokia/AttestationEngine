package webui

import(
	"net/http"

	"github.com/labstack/echo/v4"

    "a10/operations"
)


func showOpaqueObjects(c echo.Context) error {
	os,_ := operations.GetOpaqueObjects()

	return c.Render(http.StatusOK, "opaqueobjects.html",os)
}

func showOpaqueObject(c echo.Context) error {
	o,_ := operations.GetOpaqueObjectByValue(c.Param("name"))

	return c.Render(http.StatusOK, "opaqueobject.html", o)
}