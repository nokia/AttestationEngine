package bps

import(
   "fmt"

   "a10/operations"
)

func GetCollection(n string) (Collection, error){

   c,err := CollectionsDB[n]
   if err==false {
      return c,fmt.Errorf("No such collection %v",n)
      }

      return c, nil
}

func GetTemplate(n string) (Template, error){

   t,err := TemplatesDB[n]
   if err==false {
      return t,fmt.Errorf("No such template %v",n)
      }

      return t, nil
}


// Attests a single element
func AttestSingleElement(eid string, templatename string) (Decision, error) {
   
   return Decision{"something"}, nil
}

// Returns the list of element IDs
func EvaluateCollection(colname string) ( []string, error ) {
   emptystring := make([]string ,0)

   c,err := GetCollection(colname)
   if err != nil {
      return emptystring,err
   }

   for _,i := range c.Include.ItemIDs {
      fmt.Printf("   itemid %v",i)
      e,_ := operations.GetElementByItemID(i)
      fmt.Printf("      eid %v",e)

   }
   for _,t := range c.Include.Tags {
      fmt.Printf("   tag %v",t)
      es,_:= operations.GetElementsByTag(t)
      fmt.Printf("      es %v",len(es))
   }
   for _,n := range c.Include.Names {
      fmt.Printf("   name %v",n)
      es,_:= operations.GetElementsByName(n)
      fmt.Printf("      es %v",len(es))
   }

   return emptystring,err
}


// Does the full thing
func AttestCollection(colname string) (Decision, error) {
   
   return Decision{"something"}, nil
}