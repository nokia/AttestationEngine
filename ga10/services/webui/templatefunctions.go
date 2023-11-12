package webui

import (
	"fmt"
	"strconv"
	"time"

	"encoding/base64"
	"encoding/hex"

	"a10/structures"
	"a10/utilities"

	"github.com/google/go-tpm/legacy/tpm2"
)

// No idea if this works but it is supposed to be in the html files
func EpochToUTC(epoch structures.Timestamp) string {
	sec, err := strconv.ParseInt(fmt.Sprintf("%v", epoch), 10, 64)
	if err != nil {
		t := time.Unix(0, 0)
		return fmt.Sprintf("%v", t.UTC())
	}
	t := time.Unix(0, sec)
	return fmt.Sprintf("%v", t.UTC())
}

func DefaultMessage() string {
	return "Single invocation from WebUI at " + EpochToUTC(utilities.MakeTimestamp())
}

func Base64decode(u string) string {
	d, _ := base64.StdEncoding.DecodeString(u)
	return string(d)
}

func EncodeAsHexString(b []byte) string {
	return hex.EncodeToString(b)
}

func TCGAlg(h int32) string {
	return tpm2.Algorithm(h).String()
}
