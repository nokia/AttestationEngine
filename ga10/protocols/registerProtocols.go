package protocols

import (
	"a10/operations"

	"a10/protocols/a10httprestv2"
	"a10/protocols/marblerun"
	"a10/protocols/netconfprotocol"
	"a10/protocols/nullprotocol"
	"a10/protocols/testcontainerprotocol"
)

func RegisterProtocols() {
	operations.AddProtocol(a10httprestv2.Registration())
	operations.AddProtocol(nullprotocol.Registration())
	operations.AddProtocol(netconfprotocol.Registration())
	operations.AddProtocol(marblerun.Registration())
	operations.AddProtocol(testcontainerprotocol.Registration())
}
