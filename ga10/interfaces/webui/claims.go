package webui

import(
	"fmt"
	"net/http"

	"github.com/labstack/echo/v4"

    "a10/operations"
)


func showClaims(c echo.Context) error {
	es,_ := operations.GetClaimsAll()
	fmt.Printf("remdering element %v\n",len(es))

	return c.Render(http.StatusOK, "claims.html",es)
}

func showClaim(c echo.Context) error {
 	x,_ := operations.GetClaimByItemID(c.Param("itemid"))

 	return c.Render(http.StatusOK, "claim.html",x)
 }