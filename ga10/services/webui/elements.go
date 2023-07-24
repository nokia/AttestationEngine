package webui

import(
	"fmt"
	"net/http"
	"encoding/json"

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

	fmt.Printf(" cparam is %v and e.ItemId is %v\n",c.Param("itemid"),e.ItemID)

	cs,_ := operations.GetClaimsByElementID(e.ItemID,10)
	rs,_ := operations.GetResultsByElementID(e.ItemID,10)

	fmt.Printf("showElement %v\n",c.Param("itemid"))

	es := elementsStructure{ e,cs,rs }

	return c.Render(http.StatusOK, "element.html",es)
}

func newElement(c echo.Context) error {
	return c.Render(http.StatusOK, "editelement.html",nil)
}


func processNewElement(c echo.Context) error {
    elemdata := c.FormValue("elementdata")

    var newelem structures.Element

    err := json.Unmarshal([]byte(elemdata), &newelem)

	if err != nil {
		 fmt.Printf("error is %v\n",err.Error())
    	 return c.Redirect(http.StatusSeeOther, "/new/element")
	}

	fmt.Printf("  fv%v\n",newelem)
	eid,err := operations.AddElement(newelem)
	fmt.Printf("  eid=%v,err=%v\n",eid,err)

 	return c.Redirect(http.StatusSeeOther, "/elements")
}