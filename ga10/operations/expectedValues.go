// This package contains the operations for managing ExpectedValues in a system
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

func CountExpectedValues() int64 {
        return datalayer.Count("expectedvalue")
}

// AddExpectedValue is a function that takes and ExpectedValue structure that has a BLANK Itemid field (empty string) and stores that
// ExpectedValue in some database
// Successful storage returns the itemid for that ExpectedValue and a nil error. 
// An error is returned if an item id is given as part of the input structure.
func AddExpectedValue(e structures.ExpectedValue) (string, error) {
        if (e.ItemID != "") {
                return "", ErrorItemIDIncluded
        } else {
                e.ItemID = utilities.MakeID()
                _,dberr := datalayer.DB.Collection("expectedvalues").InsertOne(context.TODO(), e)
                logging.MakeLogEntry("IM","add",e.ItemID,"expectedvalue","")

                return e.ItemID, dberr                
        }
}

// UpdateExpectedValue requires the complete structure, that is, it replaces the structure with the given itemid
func UpdateExpectedValue(replacement structures.ExpectedValue) error {
                filter := bson.D{ {"itemid", replacement.ItemID} }
                updateresult,err := datalayer.DB.Collection("expectedvalues").ReplaceOne(context.TODO(), filter, replacement)

                if err != nil {
                       return err
                } else if updateresult.MatchedCount != 1 || updateresult.ModifiedCount != 1 {
                        return ErrorItemNotUpdated
                } else {
                        logging.MakeLogEntry("IM","update",replacement.ItemID,"expectedvalue","")

                        return nil
                }
             
}

// DeleteExpectedValue takes an itemid as input
func DeleteExpectedValue(itemid string) error {       
        filter := bson.D{{"itemid",itemid}}
        deleteresult, err := datalayer.DB.Collection("expectedvalues").DeleteOne(context.TODO(), filter)

        if err != nil {
                return err
        } else if deleteresult.DeletedCount != 1 {
                return ErrorItemNotDeleted
        } else {
                logging.MakeLogEntry("IM","delete",itemid,"expectedvalue","")

                return nil
        }

}


// GetExpectedValues returns a map of itemids in the ID structure. If this structure is an empty map then no ExpectedValues exist in the database.
func GetExpectedValues() ([]structures.ID, error) {
        var elems []structures.ID

        filter := bson.D{ {} }   // Get all
        options := options.Find().SetProjection(bson.D{{"itemid",1}})
        dbcursor,_ := datalayer.DB.Collection("expectedvalues").Find(context.TODO(), filter,options)
        dbcursorerror := dbcursor.All(context.TODO(),&elems)

        return elems, dbcursorerror
}


func GetExpectedValuesAll() ([]structures.ExpectedValue, error) {
        var elems []structures.ExpectedValue

        filter := bson.D{ {} }   // Get all
        dbcursor,_ := datalayer.DB.Collection("expectedvalues").Find(context.TODO(), filter)
        dbcursorerror := dbcursor.All(context.TODO(),&elems)

        return elems, dbcursorerror
}

// GetExpectedValueByItemID returns a single ExpectedValue or error
func GetExpectedValueByItemID(itemid string) (structures.ExpectedValue, error) {
        var elem structures.ExpectedValue

        // discard the cursor, it will be an empty entry if nothing exists
        filter := bson.D{ {"itemid", itemid} }
        dbcursorerror := datalayer.DB.Collection("expectedvalues").FindOne(context.TODO(), filter).Decode(&elem)

        if elem.ItemID == "" {
                return structures.ExpectedValue{}, ErrorItemNotFound
        } else {
                return elem, dbcursorerror
        }
}

// GetExpectedValueByName returns all ExpectedValues with the given name or an empty list.
func GetExpectedValuesByName(name string) ([]structures.ExpectedValue, error) {
        var elems []structures.ExpectedValue

        // discard the error, the dbcursor.All will deal with that case
        filter := bson.D{ {"name", name} }
        dbcursor,_ := datalayer.DB.Collection("expectedvalues").Find(context.TODO(), filter)
        dbcursorerror := dbcursor.All(context.TODO(),&elems)

        return elems, dbcursorerror
}


// GetExpectedValueByName returns all ExpectedValues with the given name or an empty list.
func GetExpectedValuesByPolicy(name string) ([]structures.ExpectedValue, error) {
        var elems []structures.ExpectedValue

        // discard the error, the dbcursor.All will deal with that case
        filter := bson.D{ {"policyid", name} }
        dbcursor,_ := datalayer.DB.Collection("expectedvalues").Find(context.TODO(), filter)
        dbcursorerror := dbcursor.All(context.TODO(),&elems)

        return elems, dbcursorerror
}


// GetExpectedValueByName returns all ExpectedValues with the given name or an empty list.
func GetExpectedValuesByElement(name string) ([]structures.ExpectedValue, error) {
        var elems []structures.ExpectedValue

        // discard the error, the dbcursor.All will deal with that case
        filter := bson.D{ {"elementid", name} }
        dbcursor,_ := datalayer.DB.Collection("expectedvalues").Find(context.TODO(), filter)
        dbcursorerror := dbcursor.All(context.TODO(),&elems)

        return elems, dbcursorerror
}

// GetExpectedValueByElementAndPolicy returns all ExpectedValues with the given eid and policyid
// This should return ONLY ONE EV ... if there are more with the same eid,pid then it is whatever
// mongo returns to you ... no checking, you are on your own but you were warned NOT to have more
// than one eid,pid pair for an EV, so it is your fault.
// I *may* put in checking for this one day....but I have other things to do :-)
func GetExpectedValueByElementAndPolicy(eid string, pid string) (structures.ExpectedValue, error) {
        var elem structures.ExpectedValue

        // discard the cursor, it will be an empty entry if nothing exists
       filter := bson.D{ 
                {"$and",
                        bson.A{
                                bson.D{{"elementid", eid}},
                                bson.D{{"policyid", pid}},
                              },
                        },
         }
         dbcursorerror := datalayer.DB.Collection("expectedvalues").FindOne(context.TODO(), filter).Decode(&elem)

        if elem.ItemID == "" {
                return structures.ExpectedValue{}, ErrorItemNotFound
        } else {
                return elem, dbcursorerror
        }
}

