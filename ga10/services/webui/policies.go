package webui

import (
	"encoding/json"
	"fmt"
	"net/http"

	"github.com/labstack/echo/v4"

	"a10/operations"
	"a10/structures"
)

func showPolicies(c echo.Context) error {
	es, _ := operations.GetPoliciesAll()
	return c.Render(http.StatusOK, "policies.html", es)
}

func showPolicy(c echo.Context) error {
	e, _ := operations.GetPolicyByItemID(c.Param("itemid"))
	return c.Render(http.StatusOK, "policy.html", e)
}

func newPolicy(c echo.Context) error {
	return c.Render(http.StatusOK, "editpolicy.html", nil)
}

func processNewPolicy(c echo.Context) error {
	elemdata := c.FormValue("policydata")

	var newPolicy structures.Policy

	err := json.Unmarshal([]byte(elemdata), &newPolicy)

	if err != nil {
		fmt.Printf("error is %v\n", err.Error())
		return c.Redirect(http.StatusSeeOther, "/new/policy")
	}

	fmt.Printf("  fv%v\n", newPolicy)
	eid, err := operations.AddPolicy(newPolicy)
	fmt.Printf("  eid=%v,err=%v\n", eid, err)

	return c.Redirect(http.StatusSeeOther, "/policies")
}
