// Attestation Engine A10
// Golang version v0.1
// The main package starts the various interfaces: REST, MQTT and links to the database system
package main

import (
   "fmt"
   "io/ioutil"

   "gopkg.in/yaml.v3"
)

var attscripts = make(map[string]AttestationScript)




func load(f string) {
   var ATSCR *AttestationScript

   fmt.Println("Atteststion script file location: ",f)

   ef, err := ioutil.ReadFile(f)
   if err != nil {
      panic(fmt.Sprintf("Atteststion script missing. Exiting with error %w",err))
   }

   err = yaml.Unmarshal(ef,&ATSCR)
   if err != nil {
      panic(fmt.Sprintf("Unable to parse Atteststion script. Exiting with error %w",err))
   }

   fmt.Println("Atteststion script read complete")

   attscripts[ATSCR.Name] = *ATSCR
}


func list() {
   fmt.Printf("There are %v scripts\n",len(attscripts))
}




func exec(a AttestationScript) {
   fmt.Printf("Executing %v\n%v",a.Name,a.Description)
   fmt.Printf(" %v collections, %v templates\n", len(a.Collections),len(a.Templates))

   for i,c := range a.Collections {
      fmt.Printf("%v - %v", i,c.Name)
   }
}








func main() {
   fmt.Println("d10")
   load("./t.attscript")
   list()
   exec( attscripts["Atteststion Script v1"] )

}
