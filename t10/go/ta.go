package main


import (
        "flag"
        "fmt"
//        "reflect"

        "github.com/google/go-tpm/tpm2"
//        "github.com/google/go-tpm/tpmutil"
)



var (
  //      tpmPath = flag.String("tpm-path", "/dev/tpm0", "Path to the TPM device (character device or a Unix socket)")
        tpmPath = flag.String("tpm-path", "mssim:host=localhost,port=2321", "Path to the TPM device (character device or a Unix socket)")

//         mssim:host=localhost,port=2321

)



func main() {
    // Open the TPM
    fmt.Println("tpmath is",*tpmPath)

    rwc, err := tpm2.OpenTPM(*tpmPath)
    if err != nil {
        fmt.Errorf("can't open TPM at  %v", err)
    }

    fmt.Println("Running.",rwc)

    // Now start the REST API
    
}