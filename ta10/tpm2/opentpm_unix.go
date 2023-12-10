//go:build !windows

package tpm2

import(
	"fmt"
	"io"
	"net"
	"slices"

	"github.com/google/go-tpm/legacy/tpm2"
)

var TPMDEVICES = []string{ "/dev/tpm0", "/dev/tpmrm0", "/dev/tpm1", "/dev/tpmrm1", }

func OpenTPM(path string) (io.ReadWriteCloser,error) {
	fmt.Printf("TPM Device path >>> %v <<< passed as parameter. This is a Unix build: ",path)
	
	// Check if the path is a known device, else treat it as a unix domain socket
	if slices.Contains(TPMDEVICES,path) {
		fmt.Printf("Treating it as a device\n")
		return tpm2.OpenTPM(path)
	} else {
		fmt.Printf("Treating it as a TCP Unix domain socket\n")
		return net.Dial("tcp",path)
	}
}
