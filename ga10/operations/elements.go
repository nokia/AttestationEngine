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


func CountElements() int64 {
        return datalayer.Count("elements")
}

// AddElement is a function that takes and element structure that has a BLANK Itemid field (empty string) and stores that
// element in some database
// Successful storage returns the itemid for that element and a nil error. 
// An error is returned if an item id is given as part of the input structure.
func AddElement(e structures.Element) (string, error) {
        if (e.ItemID != "") {
                return "", ErrorItemIDIncluded
        } else {
                e.ItemID = utilities.MakeID()
                _,dberr := datalayer.DB.Collection("elements").InsertOne(context.TODO(), e)
                logging.MakeLogEntry("IM","add",e.ItemID,"element","")

                return e.ItemID, dberr                
        }
}

// UpdateElement requires the complete structure, that is, it replaces the structure with the given itemid
func UpdateElement(replacement structures.Element) error {
                filter := bson.D{ {"itemid", replacement.ItemID} }
                updateresult,err := datalayer.DB.Collection("elements").ReplaceOne(context.TODO(), filter, replacement)

                if err != nil {
                       return err
                } else if updateresult.MatchedCount != 1 || updateresult.ModifiedCount != 1 {
                        return ErrorItemNotUpdated
                } else {
                        logging.MakeLogEntry("IM","update",replacement.ItemID,"element","")
                        return nil
                }            
}

// DeleteElement takes an itemid as input
func DeleteElement(itemid string) error {       
        filter := bson.D{{"itemid",itemid}}
        deleteresult, err := datalayer.DB.Collection("elements").DeleteOne(context.TODO(), filter)

        if err != nil {
                return err
        } else if deleteresult.DeletedCount != 1 {
                return ErrorItemNotDeleted
        } else {
                logging.MakeLogEntry("IM","delete",itemid,"element","")
                return nil
        }

}


// GetElements returns a map of itemids in the ID structure. If this structure is an empty map then no elements exist in the database.
func GetElements() ([]structures.ID, error) {
        var elems []structures.ID

        filter := bson.D{ {} }   // Get all
        options := options.Find().SetProjection(bson.D{{"itemid",1}}).SetSort(bson.D{{"name",1}})
        dbcursor,_ := datalayer.DB.Collection("elements").Find(context.TODO(), filter,options)
        dbcursorerror := dbcursor.All(context.TODO(),&elems)

        return elems, dbcursorerror
}

// GetElementsAll returns a map of every element. If this structure is an empty map then no elements exist in the database.
// This is only meant to be used sparingly, eg: when quering the UI to reduce load on the database
func GetElementsAll() ([]structures.Element, error) {
        var elems []structures.Element

        filter := bson.D{ {} }   // Get all
        options := options.Find().SetSort(bson.D{{"name",1}})
        dbcursor,_ := datalayer.DB.Collection("elements").Find(context.TODO(), filter,options)
        dbcursorerror := dbcursor.All(context.TODO(),&elems)


        return elems, dbcursorerror
}

// GetElementByItemID returns a single element or error
func GetElementByItemID(itemid string) (structures.Element, error) {
        var elem structures.Element

        // discard the cursor, it will be an empty entry if nothing exists
        filter := bson.D{ {"itemid", itemid} }
        dbcursorerror := datalayer.DB.Collection("elements").FindOne(context.TODO(), filter).Decode(&elem)

        if elem.ItemID == "" {
                return structures.Element{}, ErrorItemNotFound
        } else {
                return elem, dbcursorerror
        }
}

// GetElementByName returns all elements with the given name or an empty list.
func GetElementsByName(name string) ([]structures.Element, error) {
        var elems []structures.Element

        // discard the error, the dbcursor.All will deal with that case
        filter := bson.D{ {"name", name} }
        dbcursor,_ := datalayer.DB.Collection("elements").Find(context.TODO(), filter)
        dbcursorerror := dbcursor.All(context.TODO(),&elems)

        return elems, dbcursorerror
}

// GetElementByName returns all elements with the given name or an empty list.
func GetElementsByTag(tag string) ([]structures.Element, error) {
        var elems []structures.Element

        // discard the error, the dbcursor.All will deal with that case
        // not sure how this works, but if tags is an array then this
        // should perform the equivalent of   tag in tags
        // ie: set inclusion.....let's see
        
        filter := bson.D{ {"tags", tag} }
        dbcursor,_ := datalayer.DB.Collection("elements").Find(context.TODO(), filter)
        dbcursorerror := dbcursor.All(context.TODO(),&elems)

        return elems, dbcursorerror
}