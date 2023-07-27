package restapi

import(
	"log"
	"net/http"
	"strconv"

	"a10/operations"
	"a10/structures"
)

import(
	"github.com/labstack/echo/v4"
)

type returnResults struct {
	Results  []string    `json:"results"`
	Length    int       `json:"length"`
}

func getResults(c echo.Context) error {
	Results,err := operations.GetResults()

	if err != nil {
		log.Println("err=",err)
		return c.JSON(http.StatusInternalServerError,MakeRESTErrorMessage(err))
	} else {
		//Convert elems from []structures.ID into a []string
		var Results_str []string
		for _,e := range Results {
			Results_str = append(Results_str,e.ItemID)
		}

		//Marshall into JSON
		Results_struct := returnResults{ Results_str, len(Results_str) }
	
		return c.JSON(http.StatusOK, Results_struct)
	}
}

func getResult (c echo.Context) error {
	itemid := c.Param("itemid")
	Result,err := operations.GetResultByItemID(itemid)

	if err != nil {
		return c.JSON(http.StatusInternalServerError,MakeRESTErrorMessage(err))
	} else {
		return c.JSON(http.StatusOK, Result)
	}
}

// GetResultsByElementID
func getResultsByElementID (c echo.Context) error {
	itemid := c.Param("itemid")
	max_query := c.QueryParam("max")
	max, err := strconv.ParseInt(max_query, 10, 64)
	if err != nil {
		max=200
	}	

	Results,err := operations.GetResultsByElementID(itemid, max)

	if err != nil {
		log.Println("err=",err)
		return c.JSON(http.StatusInternalServerError,MakeRESTErrorMessage(err))
	} else {
		//Convert elems from []structures.ID into a []string
		var Results_str []string
		for _,e := range Results {
			Results_str = append(Results_str,e.ItemID)
		}

		//Marshall into JSON
		Results_struct := returnResults{ Results_str, len(Results_str) }
	
		return c.JSON(http.StatusOK, Results_struct)
	}
}

type postResultReturn struct {
	Itemid        string   `json:"itemid"`
	Error         string   `json:"error"`
}

func postResult(c echo.Context) error {
	elem := new(structures.Result)

	if err := c.Bind(elem); err != nil {	
		clienterr := postResultReturn{ "",err.Error() }
		return c.JSON(http.StatusBadRequest, clienterr)
	}

	res,err := operations.AddResult(*elem)

	if err!=nil {
		response := postElementReturn{ res,err.Error() }
		return c.JSON(http.StatusInternalServerError, response)
	} else {
		response := postElementReturn{ res,"" }
		return c.JSON(http.StatusCreated, response)
	}
}