package restapi

import(
	"log"
	"net/http"

	"a10/operations"
	"a10/structures"

	"github.com/labstack/echo/v4"
)

type returnElements struct {
	Elements  []string  `json:"elements"`
	Length    int       `json:"length"`
}

func getElements(c echo.Context) error {
	elems,err := operations.GetElements()

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
		elems_struct := returnElements{ elems_str, len(elems_str) }
	
		return c.JSON(http.StatusOK, elems_struct)
	}
}

func getElement (c echo.Context) error {
	itemid := c.Param("itemid")

	elem,err := operations.GetElementByItemID(itemid)

	if err != nil {
		log.Println("err=",err)
		return c.JSON(http.StatusInternalServerError,MakeRESTErrorMessage(err))
	} else {
		return c.JSON(http.StatusOK, elem)
	}
}

func getElementsByName (c echo.Context) error {
	name := c.Param("name")

	elems,err := operations.GetElementsByName(name)

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
		elems_struct := returnElements{ elems_str, len(elems_str) }
	
		return c.JSON(http.StatusOK, elems_struct)
	}
}



type postElementReturn struct {
	Itemid        string   `json:"itemid"`
	Error         string   `json:"error"`
}

func postElement(c echo.Context) error {
	elem := new(structures.Element)

	if err := c.Bind(elem); err != nil {	
		clienterr := postElementReturn{ "",err.Error() }
		return c.JSON(http.StatusBadRequest, clienterr)
	}

	res,err := operations.AddElement(*elem)

	if err!=nil {
		response := postElementReturn{ res,err.Error() }
		return c.JSON(http.StatusInternalServerError, response)
	} else {
		response := postElementReturn{ res,"" }
		return c.JSON(http.StatusCreated, response)
	}
}






func putElement(c echo.Context) error {
	elem := new(structures.Element)

	if err := c.Bind(elem); err != nil {
		clienterr := postElementReturn{ "",err.Error() }
		return c.JSON(http.StatusBadRequest, clienterr)
	}

	if _,err:= operations.GetElementByItemID(elem.ItemID); err != nil {
		response := postElementReturn{ "",err.Error() }
		return c.JSON(http.StatusNotFound, response)
	}


	log.Println("adding elemenet")
	err := operations.UpdateElement(*elem)
	log.Println("creating response ",elem.ItemID,err)	

	if err!=nil {
		log.Println("err=",elem.ItemID)

		response := postElementReturn{ elem.ItemID,err.Error() }
		return c.JSON(http.StatusInternalServerError, response)
	} else {
		log.Println("res=",elem.ItemID)
		response := postElementReturn{ elem.ItemID,"" }
		return c.JSON(http.StatusCreated, response)
	}
}



func deleteElement (c echo.Context) error {
	itemid := c.Param("itemid")

	log.Println("got here ",itemid)
	elem,err := operations.GetElementByItemID(itemid)
	log.Println("Elem is ",elem)

	if err != nil {
		response := postElementReturn{ elem.ItemID,err.Error() }
		return c.JSON(http.StatusInternalServerError, response)
	} else {
		err = operations.DeleteElement(itemid)
		if err != nil {
			response := postElementReturn{ itemid,err.Error() }
			return c.JSON(http.StatusInternalServerError, response)
		} else {
			response := postElementReturn{ itemid,"" }			
			return c.JSON(http.StatusOK, response)
		}
	}
}