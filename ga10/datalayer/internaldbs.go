package datalayer

import(
    "a10/structures"        
)

var RulesDatabase map[string]structures.Rule = make(map[string]structures.Rule)
var ProtocolsDatabase map[string]structures.Protocol = make(map[string]structures.Protocol)

func initialiseInternalDBs() {
	// No initialisation required as the current two internal databases are empty
}