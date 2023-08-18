package utilities

import(
	"encoding/gob"
	"fmt"
	"bytes"
	"crypto/sha256"
)

func init(){

	gob.Register([]interface{}{})	
	gob.Register(map[string]interface{}{})

}

func MakeSHA256(i any) ([]uint8,error) {

	var b bytes.Buffer

	e := gob.NewEncoder(&b)
	if err := e.Encode(i); err != nil {
		return []uint8{},fmt.Errorf("hash encoding Failed with error %w", err)
	}

	h := sha256.New()
	h.Write(b.Bytes())
	digest := h.Sum(nil)

	return digest,nil
}