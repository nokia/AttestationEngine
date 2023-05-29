package operations

import(
        "errors"
        "sort"
        "golang.org/x/exp/maps"

        "a10/structures"        
        "a10/datalayer"
        "a10/logging"        
)

func CountRules() int64 {
        return datalayer.Count("rules")
}

// GetElements returns a map of itemids in the ID structure. If this structure is an empty map then no elements exist in the database.
func GetRules() []structures.Rule {
        keys := maps.Keys(datalayer.RulesDatabase)
        sort.Strings(keys)

        vals := make([]structures.Rule, 0)  // empty
        for _,k := range keys {
                vals = append(vals,datalayer.RulesDatabase[k])
        }
        return vals

}

func GetRule(n string) (structures.Rule, error) {
        r,exists := datalayer.RulesDatabase[n]

        if (exists) {
                return r,nil
        } else {
                return r,errors.New("No such rule")
        }
}

func AddRule(r structures.Rule) {
        k := r.Name
        datalayer.RulesDatabase[k] = r
        logging.MakeLogEntry("IM","add","","rule",k)        
}