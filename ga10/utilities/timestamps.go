package utilities

import(
	"time"
	"fmt"

	"a10/structures"
)


//makeTimestamp generates the number of nanoseconds since Unix Epoch in UTC.
func MakeTimestamp() structures.Timestamp {
    t := structures.Timestamp(fmt.Sprintf("%v",time.Now().UnixNano()))
	return t
}