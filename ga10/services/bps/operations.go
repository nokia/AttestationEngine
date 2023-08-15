package bps

import(
	_ "fmt"
)

// Attests a single element
func AttestSingleElement(eid string, templname string) (Decision, error) {
   
   return Decision{"something"}, nil
}

// Returns the list of element IDs
func EvaluateCollection(colname string) ( []string, error ) {
   _,err := GetCollection(colname)

   emptystring := make([]string ,0)

   return emptystring,err
}


// Does the full thing
func AttestCollection(colname string) (Decision, error) {
   
   return Decision{"something"}, nil
}