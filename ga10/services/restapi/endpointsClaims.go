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

type returnClaims struct {
	Claims  []string    `json:"claims"`
	Length    int       `json:"length"`
}

func getClaims(c echo.Context) error {
	claims,err := operations.GetClaims()

	if err != nil {
		log.Println("err=",err)
		return c.JSON(http.StatusInternalServerError,MakeRESTErrorMessage(err))
	} else {
		//Convert elems from []structures.ID into a []string
		var claims_str []string
		for _,e := range claims {
			claims_str = append(claims_str,e.ItemID)
		}

		//Marshall into JSON
		claims_struct := returnClaims{ claims_str, len(claims_str) }
	
		return c.JSON(http.StatusOK, claims_struct)
	}
}

func getClaim (c echo.Context) error {
	itemid := c.Param("itemid")
	claim,err := operations.GetClaimByItemID(itemid)

	if err != nil {
		return c.JSON(http.StatusInternalServerError,MakeRESTErrorMessage(err))
	} else {
		return c.JSON(http.StatusOK, claim)
	}
}

// GetClaimsByElementID
func getClaimsByElementID (c echo.Context) error {
	itemid := c.Param("itemid")
	max_query := c.QueryParam("max")
	max, err := strconv.ParseInt(max_query, 10, 64)
	if err != nil {
		max=200
	}	

	claims,err := operations.GetClaimsByElementID(itemid, max)

	if err != nil {
		log.Println("err=",err)
		return c.JSON(http.StatusInternalServerError,MakeRESTErrorMessage(err))
	} else {
		//Convert elems from []structures.ID into a []string
		var claims_str []string
		for _,e := range claims {
			claims_str = append(claims_str,e.ItemID)
		}

		//Marshall into JSON
		claims_struct := returnClaims{ claims_str, len(claims_str) }
	
		return c.JSON(http.StatusOK, claims_struct)
	}
}

type postClaimReturn struct {
	Itemid        string   `json:"itemid"`
	Error         string   `json:"error"`
}

func postClaim(c echo.Context) error {
	elem := new(structures.Claim)

	if err := c.Bind(elem); err != nil {	
		clienterr := postClaimReturn{ "",err.Error() }
		return c.JSON(http.StatusBadRequest, clienterr)
	}

	res,err := operations.AddClaim(*elem)

	if err!=nil {
		response := postElementReturn{ res,err.Error() }
		return c.JSON(http.StatusInternalServerError, response)
	} else {
		response := postElementReturn{ res,"" }
		return c.JSON(http.StatusCreated, response)
	}
}