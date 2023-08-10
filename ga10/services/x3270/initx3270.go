package x3270

import(
	_ "net/http"

	"os"
	"net"
	"fmt"

    "a10/configuration"
    "a10/logging"

	"github.com/racingmars/go3270"

)




func init() {
	// put the go3270 library in debug mode
	go3270.Debug = os.Stderr
}

func StartX3270() {

	//get configuration data
	port := configuration.ConfigData.X3270.Port 
	//crt := configuration.ConfigData.Rest.Crt 
	//key:= configuration.ConfigData.Rest.Key 
	//usehttp := configuration.ConfigData.Rest.UseHTTP 

	//start the server
	ln, err := net.Listen("tcp", ":"+port)
	if err != nil {
		logging.MakeLogEntry("SYS","startup",configuration.ConfigData.System.Name,"GA10","X3270 service failed to start.")
		fmt.Printf("X3270 failed to start service")
		panic(err)
	}

	logging.MakeLogEntry("SYS","startup",configuration.ConfigData.System.Name,"GA10","X3270 service started.")
	fmt.Printf("X3270 service listening on port %v\n",port)
	for {
		conn, err := ln.Accept()
		if err != nil {
			logging.MakeLogEntry("SYS","x3270",configuration.ConfigData.System.Name,"GA10","X3270 failed to accept connnection")
			fmt.Printf("X3270 failed to accept connnection")
			panic(err)
		}
		go handle(conn)
	}

}

// handle is the handler for individual user connections.
func handle(conn net.Conn) {
	defer conn.Close()

	// Always begin new connection by negotiating the telnet options
	go3270.NegotiateTelnet(conn)

	fieldValues := make(map[string]string)


		response, err := go3270.HandleScreen(
			titlescreen,                   // the screen to display
			titlescreenrules,                  // the rules to enforce
			fieldValues,                   // any field values we wish to supply
			[]go3270.AID{go3270.AIDEnter}, // the AID keys we support
			[]go3270.AID{go3270.AIDPF3},   // keys that are "exit" keys
			"errormsg",                    // the field to write error message into
			4, 20,                         // the row and column to place the cursor
			conn)
		if err != nil {
			fmt.Printf("X3270 handle screen error %w",err)			
			fmt.Println(err)
			return
		}

	fmt.Printf("Connection closed %v \n",response)
}