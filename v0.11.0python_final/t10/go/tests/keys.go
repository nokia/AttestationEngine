// Copyright 2021 Nokia
// Licensed under the BSD 3-Clause License.
// SPDX-License-Identifier: BSD-3-Clause

package main

import (
	"flag"
	"fmt"
	"os"

	"github.com/google/go-tpm/tpm"
)

func main() {
	var tpmname = flag.String("tpm", "/dev/tpm0", "The path to the TPM device to use")
	var closekey = flag.Bool("close", false, "Close (unload) all existing key handles")
	flag.Parse()

	rwc, err := tpm.OpenTPM(*tpmname)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Couldn't open the TPM file %s: %s\n", *tpmname, err)
		return
	}

	handles, err := tpm.GetKeys(rwc)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Couldn't enumerate loaded TPM keys: %s\n", err)
		return
	}

	fmt.Printf("%d keys loaded in the TPM\n", len(handles))
	for i, h := range handles {
		fmt.Printf("  (%d) Key handle %d\n", i+1, h)
		if *closekey {
			if err = tpm.CloseKey(rwc, h); err != nil {
				fmt.Fprintf(os.Stderr, "Couldn't close TPM key handle %d\n", h)
			} else {
				fmt.Printf("    Closed handle %d\n", h)
			}
		}
	}

	return
}
