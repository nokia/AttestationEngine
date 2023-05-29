package restapi

import(
	"fmt"

	"net/http"
	"a10/operations"

	"github.com/labstack/echo/v4"
)

type ruleExternal struct  {
        Name string                            `json:"name",bson:"name"`   
        Description string                     `json:"description",bson:"description"`   
        NeedsEV bool                           `json:"needsev",bson:"needsev"`   
}


type returnRules struct {
	Rules     []ruleExternal  		`json:"rules"`
	Length    int       			`json:"length"`
}

func getRules(c echo.Context) error {
	rs := operations.GetRules()

	//Convert rs from []structures.Rule into rulesExternal
	var rs_str []ruleExternal
	for _,r := range rs {
		rs_str  = append(rs_str, ruleExternal{ r.Name, r.Description, r.NeedsEV} )
	}

	rs_rtn := returnRules{ rs_str, len(rs_str) }

	return c.JSON(http.StatusOK, rs_rtn)
	
}

func getRule(c echo.Context) error {
	r,err := operations.GetRule(c.Param("name"))

	if err!=nil {
		return c.JSON(http.StatusNotFound,fmt.Errorf("No such rule, %w",err))
	}  else {
		extr := ruleExternal{ r.Name, r.Description, r.NeedsEV}
		return c.JSON(http.StatusOK,extr)
	}
}