package webui

import(
	"net/http"

	"github.com/labstack/echo/v4"

    "a10/operations"
)


func showPolicies(c echo.Context) error {
	es,_ := operations.GetPoliciesAll()
	return c.Render(http.StatusOK, "policies.html",es)
}

func showPolicy(c echo.Context) error {
	e,_ := operations.GetPolicyByItemID(c.Param("itemid"))
	return c.Render(http.StatusOK, "policy.html",e)
}


func newPolicy(c echo.Context) error {
	return c.Render(http.StatusOK, "editpolicy.html",nil)
}