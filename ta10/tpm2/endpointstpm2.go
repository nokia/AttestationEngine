//go:build !windows

package tpm2

import (
	"encoding/base64"
	"encoding/hex"
	"fmt"
	"net/http"
	"strconv"
	"strings"

	"github.com/labstack/echo/v4"

	// this needs to be updated
	"github.com/google/go-tpm/legacy/tpm2"
	"github.com/google/go-tpm/tpmutil"
)

type tpm2taErrorReturn struct {
	TPM2taError string `json:"tpm2taerror"`
}

type pcrValue map[int]string

type quoteReturn struct {
	Quote     []byte `json:"quote"`
	Signature []byte `json:"signature"`
}

var bankNames = map[tpm2.Algorithm]string{
	tpm2.AlgSHA1:   "sha1",
	tpm2.AlgSHA256: "sha256",
	tpm2.AlgSHA384: "sha384",
	tpm2.AlgSHA512: "sha512",
}

var bankValues = map[string]tpm2.Algorithm{
	"sha1":   tpm2.AlgSHA1,
	"sha256": tpm2.AlgSHA256,
	"sha384": tpm2.AlgSHA384,
	"sha512": tpm2.AlgSHA512,
}

var pcrbanks = []tpm2.Algorithm{tpm2.AlgSHA1, tpm2.AlgSHA256, tpm2.AlgSHA384, tpm2.AlgSHA512}

// PCRs needs to be supplied the following parameters in the POST body
//
// tpm				 string ... which TPM to use
func PCRs(c echo.Context) error {
	fmt.Println("tpm2 pcrs called")

	// Obtain the parameters
	ps := new(map[string]interface{})

	if err := c.Bind(&ps); err != nil {
		rtn := tpm2taErrorReturn{fmt.Sprintf("Could not decode parameters %w", err.Error())}
		return c.JSON(http.StatusUnprocessableEntity, rtn)
	}

	params := *ps

	// Here we parse the tpm2 device
	// We have a default of /dev/tpm0
	tpm2device := params["tpm2/device"].(string)
	fmt.Printf("TPM2Device is %v \n",tpm2device)

	rwc, err := OpenTPM(tpm2device)
	if err != nil {
		rtn := tpm2taErrorReturn{fmt.Sprintf("no TPM %w", err.Error())}
		fmt.Printf("no TPM ERROR is %v \n",err.Error() )

		return c.JSON(http.StatusInternalServerError, rtn)
	}
	defer rwc.Close()

	fmt.Printf("TPM readwriteio object is %v\n",rwc)

	banks := make(map[string]pcrValue)

	for _, b := range pcrbanks {
		pcrvs := make(map[int]string)

		for i := 0; i <= 23; i++ {
			fmt.Printf("Reading back %v, pcr %v --> ",b,i)
			pcrv, pcre := tpm2.ReadPCR(rwc, i, b)
			fmt.Printf(" hex %v err %w\n",pcrv,pcre)
			if pcre == nil {
				pcrvs[i] = hex.EncodeToString(pcrv)
			}
		}
		banks[bankNames[b]] = pcrvs
	}
	return c.JSON(http.StatusOK, banks)
}

// Quote needs to be supplied the following parameters in the POST body
//
// pcrSelection      []int8
// akhandle          string (converts to hex) where the sigining key is
// tpm				 string ... which TPM to use
// nonce             []int8 the nonce
func Quote(c echo.Context) error {
	fmt.Println("tpm2 quote called...")

	// Obtain the parameters
	ps := new(map[string]interface{})

	if err := c.Bind(&ps); err != nil {
		rtn := tpm2taErrorReturn{fmt.Sprintf("Could not decode parameters %v", err.Error())}
		return c.JSON(http.StatusUnprocessableEntity, rtn)
	}

	fmt.Println("got the parameters...")

	params := *ps

	// Here we parse the pcrSelection to obtain the []int structure for the pcrselections
	s := strings.Split(params["pcrSelection"].(string), ",")
	pcrsel := make([]int, len(s), len(s))
	for i, r := range s {
		v64, err := strconv.ParseUint(r, 10, 8)

		if err != nil {
			pcrsel[i] = 0
		} else {
			pcrsel[i] = int(v64)
		}
	}

	// Here we parse the bank
	b := params["bank"].(string)
	pcrbank := bankValues[b]

	// Here we parse the nonce
	// If none then one will be generated
	nonce := params["tpm2/nonce"].(string)

	nonceBytes, err := base64.StdEncoding.DecodeString(nonce)
	if err != nil {
		rtn := tpm2taErrorReturn{fmt.Sprintf("Could not base64 decode nonce", err.Error())}
		return c.JSON(http.StatusInternalServerError, rtn)
	}

	// Here we parse the akhandle
	// This is a bit ugly but...that's the way go does things
	// Strip the 0x, parse it as a Uint in base 16 with size 32 - returns a unit64, convert to a uint32 and then create the TPM handle
	akh := strings.Replace(params["tpm2/akhandle"].(string), "0x", "", -1)
	h, err := strconv.ParseUint(akh, 16, 32)
	if err != nil {
		rtn := tpm2taErrorReturn{fmt.Sprintf("Unable to parse AK handle %w", err.Error())}
		return c.JSON(http.StatusUnprocessableEntity, rtn)
	}
	h32 := uint32(h) // this is safe because we only create a 32bit unsigned value above.
	handle := tpmutil.Handle(h32)

	// Here we parse the tpm2 device
	// We have a default of /dev/tpm0
	tpm2device := params["tpm2/device"].(string)

	// Here we commuicate with the TPM
	// Default if /dev/tpm0

	fmt.Println("Opening device")

	rwc, err := OpenTPM(tpm2device)
	if err != nil {
		rtn := tpm2taErrorReturn{fmt.Sprintf("no TPM %w", err.Error())}
		return c.JSON(http.StatusInternalServerError, rtn)
	}
	defer rwc.Close()

	fmt.Println("Quoting")

	// Here we obtain the Quote
	att, sig, err := tpm2.Quote(
		rwc,
		handle,
		"",
		"",
		nonceBytes,
		tpm2.PCRSelection{pcrbank, pcrsel},
		tpm2.AlgNull)

	fmt.Println("Got the quote...processing errors %v", err)

	if err != nil {
		rtn := tpm2taErrorReturn{fmt.Sprintf("Error obtaining quote %v", err.Error())}
		return c.JSON(http.StatusInternalServerError, rtn)
	}
	if sig == nil {
		rtn := tpm2taErrorReturn{fmt.Sprintf("No signature in quote %v", err.Error())}
		return c.JSON(http.StatusInternalServerError, rtn)
	}
	if att == nil {
		rtn := tpm2taErrorReturn{fmt.Sprintf("No quote received %v", err.Error())}
		return c.JSON(http.StatusInternalServerError, rtn)
	}

	fmt.Sprintf("Decoding attestation data")

	attestationdata, err := tpm2.DecodeAttestationData(att)

	if attestationdata == nil {
		rtn := tpm2taErrorReturn{fmt.Sprintf("Error decoding attestation data %v", err.Error())}
		return c.JSON(http.StatusInternalServerError, rtn)
	}

	fmt.Println("Att and Sig are %v and %v", att, sig)

	sigBytes, _ := sig.Encode()
	qr := quoteReturn{att, sigBytes}

	fmt.Printf("qr is %v", qr)

	return c.JSON(http.StatusOK, qr)
}
