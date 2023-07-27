package restapi

import(
	"log"
	"net/http"

	"a10/operations"
	"a10/structures"

	"github.com/labstack/echo/v4"
)

type returnOpaqueObjects struct {
	Objects  []structures.OpaqueObject  
	Length    int      
}



func getOpaqueObjects(c echo.Context) error {
	elems,err := operations.GetOpaqueObjects()

	if err != nil {
		log.Println("err=",err)
		return c.JSON(http.StatusInternalServerError,MakeRESTErrorMessage(err))
	} else {
		//Convert elems from []structures.ID into a []string
		var elems_str []structures.OpaqueObject
		for _,e := range elems {
			elems_str = append(elems_str,e)
		}

		//Marshall into JSON
		elems_struct := returnOpaqueObjects{ elems_str, len(elems_str) }
	
		return c.JSON(http.StatusOK, elems_struct)
	}
}

func getOpaqueObjectByValue(c echo.Context) error {
	value := c.Param("value")

	elem,err := operations.GetOpaqueObjectByValue(value)

	if err != nil {
		log.Println("err=",err)
		return c.JSON(http.StatusInternalServerError,MakeRESTErrorMessage(err))
	} else {
		return c.JSON(http.StatusOK, elem)
	}
}



type postOpaqueObjectReturn struct {
	Itemid        string   `json:"itemid"`
	Error         string   `json:"error"`
}



func postOpaqueObject(c echo.Context) error {
	elem := new(structures.OpaqueObject)

	if err := c.Bind(elem); err != nil {	
		clienterr := postOpaqueObjectReturn{ "",err.Error() }
		return c.JSON(http.StatusBadRequest, clienterr)
	}

	res,err := operations.AddOpaqueObject(*elem)

	if err!=nil {
		response := postOpaqueObjectReturn{ res,err.Error() }
		return c.JSON(http.StatusInternalServerError, response)
	} else {
		response := postOpaqueObjectReturn{ res,"" }
		return c.JSON(http.StatusCreated, response)
	}
}


func putOpaqueObject(c echo.Context) error {
	return postOpaqueObject(c)
}



func deleteOpaqueObject(c echo.Context) error {
	value := c.Param("value")

	err := operations.DeleteOpaqueObject(value)

	if err != nil {
		response := postOpaqueObjectReturn{ value,err.Error() }
		return c.JSON(http.StatusInternalServerError, response)
	} else {
		err = operations.DeleteElement(value)
		if err != nil {
			response := postOpaqueObjectReturn{ value,err.Error() }
			return c.JSON(http.StatusInternalServerError, response)
		} else {
			response := postOpaqueObjectReturn{ value,"" }			
			return c.JSON(http.StatusOK, response)
		}
	}
}