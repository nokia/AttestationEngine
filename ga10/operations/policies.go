// This package contains the operations for managing elements in a system
// It provides
package operations

import(
        "context"

        "a10/structures"        
        "a10/utilities"
        "a10/datalayer"
        "a10/logging"

        "go.mongodb.org/mongo-driver/mongo/options"
        "go.mongodb.org/mongo-driver/bson"

)

func CountPolicies() int64 {
        return datalayer.Count("policies")
}

// AddPolicy is a function that takes and element structure that has a BLANK Itemid field (empty string) and stores that
// element in some database
// Successful storage returns the itemid for that element and a nil error. 
// An error is returned if an item id is given as part of the input structure.
func AddPolicy(p structures.Policy) (string, error) {
        if (p.ItemID != "") {
                return "", ErrorItemIDIncluded
        } else {
                p.ItemID = utilities.MakeID()
                _,dberr := datalayer.DB.Collection("policies").InsertOne(context.TODO(), p)
                logging.MakeLogEntry("IM","add",p.ItemID,"policy","")

                return p.ItemID, dberr                
        }
}

// UpdateElement requires the complete structure, that is, it replaces the structure with the given itemid
func UpdatePolicy(replacement structures.Policy) error {
                filter := bson.D{ {"itemid", replacement.ItemID} }
                updateresult,err := datalayer.DB.Collection("policies").ReplaceOne(context.TODO(), filter, replacement)

                if err != nil {
                       return err
                } else if updateresult.MatchedCount != 1 || updateresult.ModifiedCount != 1 {
                        return ErrorItemNotUpdated
                } else {
                        logging.MakeLogEntry("IM","update",replacement.ItemID,"policy","")

                        return nil
                }
             
}

// DeleteElement takes an itemid as input
func DeletePolicy(itemid string) error {       
        filter := bson.D{{"itemid",itemid}}
        deleteresult, err := datalayer.DB.Collection("policies").DeleteOne(context.TODO(), filter)

        if err != nil {
                return err
        } else if deleteresult.DeletedCount != 1 {
                return ErrorItemNotDeleted
        } else {
                logging.MakeLogEntry("IM","delete",itemid,"policy","")

                return nil
        }

}


// GetElements returns a map of itemids in the ID structure. If this structure is an empty map then no elements exist in the database.
func GetPolicies() ([]structures.ID, error) {
        var elems []structures.ID

        filter := bson.D{ {} }   // Get all
        options := options.Find().SetProjection(bson.D{{"itemid",1}}).SetSort(bson.D{{"name",1}})
        dbcursor,_ := datalayer.DB.Collection("policies").Find(context.TODO(), filter,options)
        dbcursorerror := dbcursor.All(context.TODO(),&elems)

        return elems, dbcursorerror
}


func GetPoliciesAll() ([]structures.Policy, error) {
        var elems []structures.Policy

        filter := bson.D{ {} }   // Get all
        options := options.Find().SetSort(bson.D{{"name",1}})
        dbcursor,_ := datalayer.DB.Collection("policies").Find(context.TODO(), filter, options)
        dbcursorerror := dbcursor.All(context.TODO(),&elems)

        return elems, dbcursorerror
}

// GetElementByItemID returns a single element or error
func GetPolicyByItemID(itemid string) (structures.Policy, error) {
        var pol structures.Policy

        // discard the cursor, it will be an empty entry if nothing exists
        filter := bson.D{ {"itemid", itemid} }
        dbcursorerror := datalayer.DB.Collection("policies").FindOne(context.TODO(), filter).Decode(&pol)

        if pol.ItemID == "" {
                return structures.Policy{}, ErrorItemNotFound
        } else {
                return pol, dbcursorerror
        }
}

// GetElementByName returns all elements with the given name or an empty list.
func GetPoliciesByName(name string) ([]structures.Policy, error) {
        var pol []structures.Policy

        // discard the error, the dbcursor.All will deal with that case
        filter := bson.D{ {"name", name} }
        dbcursor,_ := datalayer.DB.Collection("policies").Find(context.TODO(), filter)
        dbcursorerror := dbcursor.All(context.TODO(),&pol)

        return pol, dbcursorerror
}