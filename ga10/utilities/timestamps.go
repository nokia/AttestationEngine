package utilities

import(
	"time"
	//"fmt"

	"a10/structures"
)


//makeTimestamp generates the number of nanoseconds since Unix Epoch in UTC.
func MakeTimestamp() structures.Timestamp {
    //t := structures.Timestamp(fmt.Sprintf("%d",time.Now().UnixNano()))
    t := structures.Timestamp(time.Now().UnixNano())

	return t
}

//returns the timestampe value for some value, eg:  2h30m
func TimeStampHoursAgo(d string) structures.Timestamp {
	now := time.Now().UnixNano() 
	
	dur,err := time.ParseDuration(d)

	if err!=nil {
		xdur,_ := time.ParseDuration("1h")
		//return structures.Timestamp(fmt.Sprintf("%d",now-xdur.Nanoseconds()))
		return structures.Timestamp(now-xdur.Nanoseconds())

	} else {
		return structures.Timestamp(now-dur.Nanoseconds())

	}
}