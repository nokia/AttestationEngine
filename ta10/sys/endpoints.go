package sys

import(
	"net/http"
	"runtime"
	"os"
	"fmt"

    "ta10/common"

	"github.com/labstack/echo/v4"
)

type sysinfoReturn struct {
	OS    string       `json:"os"`
	Arch  string       `json:"arch"`
	NCPU	int        `json:"ncpu"`
	Hostname string    `json:"hostname"`
	Unsafe bool        `json:"unsafe"`
	Pid   int 		   `json:"pid"`
	Ppid  int          `json:"ppid"`


}

func getHostname() string {
	hostname := "?"

	h,err := os.Hostname()
	if (err==nil) {
		hostname=h
	}

	return hostname	
}


func Sysinfo(c echo.Context) error {
	fmt.Println("sysinfo called")

	ncpus := runtime.NumCPU()


	s := sysinfoReturn{ runtime.GOOS, runtime.GOARCH, ncpus, getHostname(), utilities.IsUnsafe(), os.Getpid(), os.Getppid() }
	
	return c.JSON(http.StatusOK, s)
}
