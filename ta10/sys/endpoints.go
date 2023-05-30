package sys

import(
	"net/http"
	"runtime"
	"fmt"

	"github.com/labstack/echo/v4"
)

type sysinfoReturn struct {
	OS    string       `json:"os"`
	Arch  string       `json:"arch"`
	NCPU	int        `json:"ncpu"`
}

func Sysinfo(c echo.Context) error {
	fmt.Println("sysinfo called")

	n := runtime.NumCPU()
	s := sysinfoReturn{ runtime.GOOS, runtime.GOARCH, n }
	
	return c.JSON(http.StatusOK, s)
}
