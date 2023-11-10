//go:build !windows

package tpm2

import(
	"fmt"
	"io"
	"github.com/google/go-tpm/legacy/tpm2"
)


func OpenTPM(path string) (io.ReadWriteCloser,error) {
	fmt.Sprintf("TPM Device path >>> %v <<< passed as parameter. This is a Unix build and it will be utilised.")
	rwc,err :=tpm2.OpenTPM()

	return rwc,err
}
