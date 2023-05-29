package restapi

import(
	"fmt"
	"net/http"
	"a10/operations"
	
	"github.com/labstack/echo/v4"
)




type protocolExternal struct  {
        Name string                            `json:"name",bson:"name"`   
        Description string                     `json:"description",bson:"description"`   
        Intents []string                         `json:"intents",bson:"intents"`   
}


type returnProtocols struct {
	Protocols  []protocolExternal  				`json:"protocols"`
	Length    int      							`json:"length"`
}



func getProtocols(c echo.Context) error {
	ps := operations.GetProtocols()

	//Convert elems from []structures.Protocol into protocolExternal
	var ps_str []protocolExternal
	for _,p := range ps {
		ps_str  = append(ps_str, protocolExternal{ p.Name, p.Description, p.Intents} )
	}

	ps_rtn := returnProtocols{ ps_str, len(ps_str) }

	return c.JSON(http.StatusOK, ps_rtn)
}

func getProtocol(c echo.Context) error {
	p,err := operations.GetProtocol(c.Param("name"))

	if err!=nil {
		return c.JSON(http.StatusNotFound,fmt.Errorf("No such protocol, %w",err))
	}  else {
		extp := protocolExternal{ p.Name, p.Description, p.Intents}
		return c.JSON(http.StatusOK,extp)
	}
}

