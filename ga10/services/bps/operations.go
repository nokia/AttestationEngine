package bps

// Attests a single element
func AttestSingleElement(eid string, templname string) (Decision, error) {
   
   return Decision{"something"}, nil
}

// Returns the list of element IDs
func EvaluateCollection(colname string) ( []string, error ) {
   
   ls := make( []string, 0 )
   return ls, nil
}


// Does the full thing
func AttestCollection(colname string) (Decision, error) {
   
   return Decision{"something"}, nil
}