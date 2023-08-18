package bps

import(
   "fmt"
   "golang.org/x/exp/slices"

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
   e,err  := operations.GetElementByItemID(eid)
   t,err  := GetTemplate(templatename)
   
   fmt.Printf("e,t,err %v,%v,%v\n",e,t,err)
   return Decision{"something"}, nil
}

// Returns the list of element IDs
func removeDuplicates(strSlice []string) []string {
    allKeys := make(map[string]bool)
    list := []string{}
    for _, item := range strSlice {
        if _, value := allKeys[item]; !value {
            allKeys[item] = true
            list = append(list, item)
        }
    }
    fmt.Printf("unique slice is %v\n",list)
    return list
}

func subtractSet(set1 []string, set2 []string) []string{
   var outset []string 

   for _,i := range set1 {
      if !slices.Contains(set2,i) {
         outset = append(outset,i)
      }
   }

   fmt.Printf("substracted unique slice is %v\n",outset)
   return outset
}

func obtainElementSelectionSet(s ElementSelector) ([]string, error) {
   var itemids []string

   for _,i := range s.ItemIDs {
      fmt.Printf("   itemid %v\n",i)
      e,_ := operations.GetElementByItemID(i)
      itemids = append(itemids, e.ItemID)
      fmt.Printf("      eid %v\n",e)
   }
   for _,t := range s.Tags {
      fmt.Printf("   tag %v\n",t)
      es,_:= operations.GetElementsByTag(t)
      for _,el := range es {
         itemids = append(itemids, el.ItemID)      
      }
      fmt.Printf("      es %v\n",len(es))
   }
   for _,n := range s.Names {
      fmt.Printf("   name %v\n",n)
      es,_:= operations.GetElementsByName(n)
      for _,el := range es {
         itemids = append(itemids, el.ItemID)      
      }      
      fmt.Printf("      es %v\n",len(es))
   }

   return removeDuplicates(itemids), nil

}


func EvaluateCollection(colname string) ( []string, error ) {
   emptystring := make([]string ,0)

   c,err := GetCollection(colname)
   if err != nil {
      return emptystring,err
   }

   includeset, err := obtainElementSelectionSet(c.Include)
   if err!=nil {
      return emptystring, err
   }
   excludeset, err := obtainElementSelectionSet(c.Exclude)
   if err!=nil {
      return emptystring, err
   }

   fmt.Printf("include set=%v\nexclude set=%v\n",includeset,excludeset)

   selectedset := subtractSet(includeset,excludeset)

   fmt.Printf("selected set=%v\n",selectedset)
   return selectedset,err
}


// Does the full thing
func AttestCollection(colname string) (Decision, error) {
   
   return Decision{"something"}, nil
}