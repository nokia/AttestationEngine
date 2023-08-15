package bps

import(
	"fmt"
   	"io/ioutil"
	"path/filepath"

   	"gopkg.in/yaml.v3"
)

var CollectionsDB map[string]Collection = make(map[string]Collection)
var TemplatesDB map[string]Template = make(map[string]Template)


func GetCollection(n string) (Collection, error){

	c,err := CollectionsDB[n]
	if err {
    	return c,fmt.Errorf("No such collection %v",n)
   	}

   	return c, nil
}


func LoadFiles(path string) error {
	colfiles,err := filepath.Glob(path+"/*.col")
	temfiles,err := filepath.Glob(path+"/*.tem")

	fmt.Printf("path=%v\ncol=%v\ntem=%v\nerr=%w\n",path,colfiles,temfiles,err)

	// clear db

	CollectionsDB = make(map[string]Collection)
	TemplatesDB = make(map[string]Template)

	// parse collections

	for _,f := range colfiles {
		fmt.Printf("Loading %v...\n",f)
		cf, err := ioutil.ReadFile(f)
   		if err != nil {
     		return fmt.Errorf("Unable to read collection file: %v with error %w",f,err)
   		}

   		var col *Collection

   		fmt.Printf(" col %v",col)

   		err = yaml.Unmarshal(cf,&col)
   		if err != nil {
      		return fmt.Errorf("Unable to parse collection file: %v with error %w",cf,err)
   		}

   		fmt.Printf("Adding %v.\n",col.Name)
   		if _,ok := CollectionsDB[col.Name]; ok {
      		return fmt.Errorf("Duplicated collection name %v from file: %v",col.Name,cf)
   		}
		CollectionsDB[col.Name] = *col
	}



	// parse templates

	for _,f := range temfiles {
		fmt.Printf("Loading %v...\n",f)
		tf, err := ioutil.ReadFile(f)
   		if err != nil {
     		return fmt.Errorf("Unable to read template file: %v with error %w",f,err)
   		}

   		var tem *Template

   		fmt.Printf(" tem %v",tem)

   		err = yaml.Unmarshal(tf,&tem)
   		if err != nil {
      		return fmt.Errorf("Unable to parse template file: %v with error %w",tf,err)
   		}

   		fmt.Printf("Adding %v.\n",tem.Name)
   		if _,ok := TemplatesDB[tem.Name]; ok {
      		return fmt.Errorf("Duplicated template name %v from file: %v",tem.Name,tf)
   		}
		TemplatesDB[tem.Name] = *tem
	}

	return nil
}

