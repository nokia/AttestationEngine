package restapi

import(
	"sync"
	"net/http"

	"a10/operations"
	
	"github.com/labstack/echo/v4"
)

type healthStructure struct {
	Counts counts    `json:"counts"`
}

type counts struct {
	Ne     		int64   `json:"ne"`
	Np          int64   `json:"Np"`
	Nev         int64   `json:"Nev"`
	Nc          int64   `json:"Nc"`
	Nr          int64   `json:"Nr"`

	Nh          int64   `json:"Nh"`
	Nrul        int64   `json:"Nrul"`
	Npol        int64   `json:"Npol"`

	Nlog        int64   `json:"Nlog"`
}

// Returns a structure detailing the health of the attestation system
// NB: there was a bit of experimental goroutines here :-)
func objectCount() counts {

	var wg sync.WaitGroup

	wg.Add(9)
	
	nlog := func() int64 {
		defer wg.Done()
		return operations.CountLogEntries()
	}()

	ne := func() int64 {
		defer wg.Done()
		return operations.CountElements()
	}()

	np := func() int64 {
		defer wg.Done()
		return operations.CountPolicies()
	}()

	nev := func() int64 {
		defer wg.Done()
		return operations.CountExpectedValues()
	}()	

	nc := func() int64 {
		defer wg.Done()
		return operations.CountClaims()
	}()

	nr := func() int64 {
		defer wg.Done()
		return operations.CountResults()
	}()

	nh := func() int64 {
		defer wg.Done()
		return operations.CountOpaqueOjects()
	}()

	nrul := func() int64 {
		defer wg.Done()
		return operations.CountRules()
	}()

	npro:= func() int64 {
		defer wg.Done()
		return operations.CountProtocols()
	}()

	wg.Wait()

	return counts{ ne,np,nev,nc,nr,nh,nrul,npro,nlog }
}




func health(c echo.Context) error {

	hstr := healthStructure{ objectCount() }

	return c.JSON(http.StatusOK, hstr)
}
