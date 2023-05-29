package operations

import(
        "fmt"
        "golang.org/x/exp/maps"

        "a10/structures"        
        "a10/datalayer"
        "a10/logging"
)

func CountProtocols() int64 {
        return datalayer.Count("protocols")
}

// GetElements returns a map of itemids in the ID structure. If this structure is an empty map then no elements exist in the database.
func GetProtocols() []structures.Protocol {
        return maps.Values(datalayer.ProtocolsDatabase)
}

func GetProtocol(n string) (structures.Protocol, error) {
        r,exists := datalayer.ProtocolsDatabase[n]

        if (exists) {
                return r,nil
        } else {
                return r,fmt.Errorf("No such protocol")
        }
}

func AddProtocol(p structures.Protocol) {
        k := p.Name
        datalayer.ProtocolsDatabase[k] = p
        logging.MakeLogEntry("IM","add","","protocol",k)
}