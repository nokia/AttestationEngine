package webui

import(
	"fmt"
	"net/http"

	"github.com/labstack/echo/v4"

    "a10/operations"
    "a10/structures"
)

type elementsStructure struct {
	E       structures.Element
	CS      []structures.Claim
	RS      []structures.Result
}

func showElements(c echo.Context) error {
	es,_ := operations.GetElementsAll()
	fmt.Printf("remdering element %v\n",len(es))

	return c.Render(http.StatusOK, "elements.html",es)
}

func showElement(c echo.Context) error {
	e,_ := operations.GetElementByItemID(c.Param("itemid"))
	cs,_ := operations.GetClaimsByElementID(e.ItemID,50)
	rs,_ := operations.GetResultsByElementID(e.ItemID,50)

	es := elementsStructure{ e,cs,rs }

	return c.Render(http.StatusOK, "element.html",es)
}

func newElement(c echo.Context) error {
	return c.Render(http.StatusOK, "editelement.html",nil)
}