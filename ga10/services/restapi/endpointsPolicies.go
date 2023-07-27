package restapi

import(
	"log"
	"net/http"

	"a10/operations"
	"a10/structures"

	"github.com/labstack/echo/v4"
)

type returnPolicies struct {
	Policies  []string  `json:"policies"`
	Length    int       `json:"length"`
}

func getPolicies(c echo.Context) error {
	elems,err := operations.GetPolicies()

	log.Println("ps=",elems)

	if err != nil {
		log.Println("err=",err)
		return c.JSON(http.StatusInternalServerError,MakeRESTErrorMessage(err))
	} else {
		//Convert elems from []structures.ID into a []string
		var elems_str []string
		for _,e := range elems {
			log.Println("--",e)
			elems_str = append(elems_str,e.ItemID)
		}

		//Marshall into JSON
		elems_struct := returnPolicies{ elems_str, len(elems_str) }
	
		return c.JSON(http.StatusOK, elems_struct)
	}
}

func getPolicy (c echo.Context) error {
	itemid := c.Param("itemid")

	elem,err := operations.GetPolicyByItemID(itemid)

	if err != nil {
		log.Println("err=",err)
		return c.JSON(http.StatusInternalServerError,MakeRESTErrorMessage(err))
	} else {
		return c.JSON(http.StatusOK, elem)
	}
}

func getPoliciesByName (c echo.Context) error {
	name := c.Param("name")

	elems,err := operations.GetPoliciesByName(name)

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
		elems_struct := returnPolicies{ elems_str, len(elems_str) }
	
		return c.JSON(http.StatusOK, elems_struct)
	}
}



type postPolicyReturn struct {
	Itemid        string   `json:"itemid"`
	Error         string   `json:"error"`
}

func postPolicy(c echo.Context) error {
	elem := new(structures.Policy)

	if err := c.Bind(elem); err != nil {	
		clienterr := postPolicyReturn{ "",err.Error() }
		return c.JSON(http.StatusBadRequest, clienterr)
	}

	res,err := operations.AddPolicy(*elem)

	if err!=nil {
		response := postPolicyReturn{ res,err.Error() }
		return c.JSON(http.StatusInternalServerError, response)
	} else {
		response := postPolicyReturn{ res,"" }
		return c.JSON(http.StatusCreated, response)
	}
}

func putPolicy(c echo.Context) error {
	elem := new(structures.Policy)

	if err := c.Bind(elem); err != nil {
		clienterr := postPolicyReturn{ "",err.Error() }
		return c.JSON(http.StatusBadRequest, clienterr)
	}

	if _,err:= operations.GetPolicyByItemID(elem.ItemID); err != nil {
		response := postPolicyReturn{ "",err.Error() }
		return c.JSON(http.StatusNotFound, response)
	}


	log.Println("adding elemenet")
	err := operations.UpdatePolicy(*elem)
	log.Println("creating response ",elem.ItemID,err)	

	if err!=nil {
		log.Println("err=",elem.ItemID)

		response := postPolicyReturn{ elem.ItemID,err.Error() }
		return c.JSON(http.StatusInternalServerError, response)
	} else {
		log.Println("res=",elem.ItemID)
		response := postPolicyReturn{ elem.ItemID,"" }
		return c.JSON(http.StatusCreated, response)
	}
}



func deletePolicy (c echo.Context) error {
	itemid := c.Param("itemid")

	log.Println("got here ",itemid)
	elem,err := operations.GetPolicyByItemID(itemid)
	log.Println("Elem is ",elem)

	if err != nil {
		response := postPolicyReturn{ elem.ItemID,err.Error() }
		return c.JSON(http.StatusInternalServerError, response)
	} else {
		err = operations.DeletePolicy(itemid)
		if err != nil {
			response := postPolicyReturn{ itemid,err.Error() }
			return c.JSON(http.StatusInternalServerError, response)
		} else {
			response := postPolicyReturn{ itemid,"" }			
			return c.JSON(http.StatusOK, response)
		}
	}
}