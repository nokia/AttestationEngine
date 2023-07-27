package restapi

import(
	"log"
	"fmt"
	"net/http"

	"a10/operations"
	"a10/structures"

	"github.com/labstack/echo/v4"
)

type returnExpectedValues struct {
	ExpectedValues  []string  `json:"expectedValues"`
	Length    int       `json:"length"`
}

func getExpectedValues(c echo.Context) error {
	elems,err := operations.GetExpectedValues()

	if err != nil {
		log.Println("err=",err)
		return c.JSON(http.StatusInternalServerError,MakeRESTErrorMessage(err))
	} else {
		//Convert elems from []structures.ID into a []string
		var elems_str []string
		for _,e := range elems {
			elems_str = append(elems_str,e.ItemID)
		}

		//Marshall into JSON
		elems_struct := returnExpectedValues{ elems_str, len(elems_str) }
	
		return c.JSON(http.StatusOK, elems_struct)
	}
}

func getExpectedValue (c echo.Context) error {
	itemid := c.Param("itemid")

	elem,err := operations.GetExpectedValueByItemID(itemid)

	if err != nil {
		log.Println("err=",err)
		return c.JSON(http.StatusInternalServerError,MakeRESTErrorMessage(err))
	} else {
		return c.JSON(http.StatusOK, elem)
	}
}

func getExpectedValuesByName (c echo.Context) error {
	name := c.Param("name")

	elems,err := operations.GetExpectedValuesByName(name)

	if err != nil {
		log.Println("err=",err)
		return c.JSON(http.StatusInternalServerError,MakeRESTErrorMessage(err))
	} else {
		//Convert elems from []structures.ID into a []string
		var elems_str []string
		for _,e := range elems {
			elems_str = append(elems_str,e.ItemID)
		}

		//Marshall into JSON
		elems_struct := returnExpectedValues{ elems_str, len(elems_str) }
	
		return c.JSON(http.StatusOK, elems_struct)
	}
}

func getExpectedValuesByElement (c echo.Context) error {
	itemid := c.Param("itemid")

	elems,err := operations.GetExpectedValuesByElement(itemid)

	if err != nil {
		log.Println("err=",err)
		return c.JSON(http.StatusInternalServerError,MakeRESTErrorMessage(err))
	} else {
		//Convert elems from []structures.ID into a []string
		var elems_str []string
		for _,e := range elems {
			elems_str = append(elems_str,e.ItemID)
		}

		//Marshall into JSON
		elems_struct := returnExpectedValues{ elems_str, len(elems_str) }
	
		return c.JSON(http.StatusOK, elems_struct)
	}
}


func getExpectedValuesByPolicy (c echo.Context) error {
	itemid := c.Param("itemid")

	elems,err := operations.GetExpectedValuesByPolicy(itemid)

	if err != nil {
		log.Println("err=",err)
		return c.JSON(http.StatusInternalServerError,MakeRESTErrorMessage(err))
	} else {
		//Convert elems from []structures.ID into a []string
		var elems_str []string
		for _,e := range elems {
			elems_str = append(elems_str,e.ItemID)
		}

		//Marshall into JSON
		elems_struct := returnExpectedValues{ elems_str, len(elems_str) }
	
		return c.JSON(http.StatusOK, elems_struct)
	}
}



func getExpectedValueByElementAndPolicy (c echo.Context) error {
	eid := c.Param("eid")
	pid := c.Param("pid")

	fmt.Println("eid",eid,"pid",pid)

	elem,err := operations.GetExpectedValueByElementAndPolicy(eid,pid)

	fmt.Println("\nreturn ",elem," error",err)

	if err != nil {
		log.Println("err=",err)
		return c.JSON(http.StatusInternalServerError,MakeRESTErrorMessage(err))
	} else {
		return c.JSON(http.StatusOK, elem)
	}
}






type postExpectedValueReturn struct {
	Itemid        string   `json:"itemid"`
	Error         string   `json:"error"`
}

func postExpectedValue(c echo.Context) error {
	elem := new(structures.ExpectedValue)

	if err := c.Bind(elem); err != nil {	
		clienterr := postExpectedValueReturn{ "",err.Error() }
		return c.JSON(http.StatusBadRequest, clienterr)
	}

	res,err := operations.AddExpectedValue(*elem)

	if err!=nil {
		response := postExpectedValueReturn{ res,err.Error() }
		return c.JSON(http.StatusInternalServerError, response)
	} else {
		response := postExpectedValueReturn{ res,"" }
		return c.JSON(http.StatusCreated, response)
	}
}

func putExpectedValue(c echo.Context) error {
	elem := new(structures.ExpectedValue)

	if err := c.Bind(elem); err != nil {
		clienterr := postExpectedValueReturn{ "",err.Error() }
		return c.JSON(http.StatusBadRequest, clienterr)
	}

	if _,err:= operations.GetExpectedValueByItemID(elem.ItemID); err != nil {
		response := postExpectedValueReturn{ "",err.Error() }
		return c.JSON(http.StatusNotFound, response)
	}


	log.Println("adding elemenet")
	err := operations.UpdateExpectedValue(*elem)
	log.Println("creating response ",elem.ItemID,err)	

	if err!=nil {
		log.Println("err=",elem.ItemID)

		response := postExpectedValueReturn{ elem.ItemID,err.Error() }
		return c.JSON(http.StatusInternalServerError, response)
	} else {
		log.Println("res=",elem.ItemID)
		response := postExpectedValueReturn{ elem.ItemID,"" }
		return c.JSON(http.StatusCreated, response)
	}
}



func deleteExpectedValue (c echo.Context) error {
	itemid := c.Param("itemid")

	log.Println("got here ",itemid)
	elem,err := operations.GetExpectedValueByItemID(itemid)
	log.Println("Elem is ",elem)

	if err != nil {
		response := postExpectedValueReturn{ elem.ItemID,err.Error() }
		return c.JSON(http.StatusInternalServerError, response)
	} else {
		err = operations.DeleteExpectedValue(itemid)
		if err != nil {
			response := postExpectedValueReturn{ itemid,err.Error() }
			return c.JSON(http.StatusInternalServerError, response)
		} else {
			response := postExpectedValueReturn{ itemid,"" }			
			return c.JSON(http.StatusOK, response)
		}
	}
}
