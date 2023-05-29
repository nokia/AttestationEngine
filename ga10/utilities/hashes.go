package utilities

import(
	"encoding/gob"
	"fmt"
	"log"
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
		log.Printf("WARNING: Encoding hash failed with %v. Entry not made.",err)
		return []uint8{},fmt.Errorf("Encoding Failed")
	}

	h := sha256.New()
	h.Write(b.Bytes())
	digest := h.Sum(nil)

	return digest,nil
}