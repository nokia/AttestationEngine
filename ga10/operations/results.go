// This package contains the operations for managing elements in a system
// It provides
package operations

import(
        "fmt"
        "context"

        "a10/structures"        
        "a10/utilities"
        "a10/datalayer"
        "a10/logging"

        "go.mongodb.org/mongo-driver/mongo/options"
        "go.mongodb.org/mongo-driver/bson"

)

func CountResults() int64 {
        return datalayer.Count("results")
}

// AddElement is a function that takes and element structure that has a BLANK Itemid field (empty string) and stores that
// element in some database
// Successful storage returns the itemid for that element and a nil error. 
// An error is returned if an item id is given as part of the input structure.
func AddResult(e structures.Result) (string, error) {
        if (e.ItemID != "") {
                return "", ErrorItemIDIncluded
        } else {
                e.ItemID = utilities.MakeID()
                _,dberr := datalayer.DB.Collection("results").InsertOne(context.TODO(), e)
                logging.MakeLogEntry("R","add",e.ItemID,"result",fmt.Sprintf("%v",e.Result) )
                return e.ItemID, dberr                
        }
}


// GetElements returns a map of itemids in the ID structure. If this structure is an empty map then no elements exist in the database.
func GetResults() ([]structures.ID, error) {
        var results []structures.ID

        filter := bson.D{ {} }   // Get all
        options := options.Find().SetProjection(bson.D{{"itemid",1}})
        dbcursor,_ := datalayer.DB.Collection("results").Find(context.TODO(), filter,options)
        dbcursorerror := dbcursor.All(context.TODO(),&results)

        return results, dbcursorerror
}


func GetResultsAll() ([]structures.Result, error) {
        var results []structures.Result

        filter := bson.D{ {} }   // Get all
        options := options.Find().SetSort(bson.D{{"verifiedat",-1}})
        dbcursor,_ := datalayer.DB.Collection("results").Find(context.TODO(), filter, options)
        dbcursorerror := dbcursor.All(context.TODO(),&results)

        return results, dbcursorerror
}


func GetResultsByElementID(eid string, maximumAmount int64) ([]structures.Result, error) {
        var Results []structures.Result

        fmt.Printf("getting results for %v\n",eid)

        filter := bson.D{ { "elementid",eid } }   // Get all   // TODO search for itemIDs only
        options := options.Find().SetSort(bson.D{{"verifiedAt",-1}}).SetLimit(maximumAmount)
        dbcursor,_ := datalayer.DB.Collection("results").Find(context.TODO(), filter, options)
        dbcursorerror := dbcursor.All(context.TODO(),&Results)

        return Results, dbcursorerror
}


// GetElementByItemID returns a single element or error
func GetResultByItemID(itemid string) (structures.Result, error) {
        var Result structures.Result

        // discard the cursor, it will be an empty entry if nothing exists
        filter := bson.D{ {"itemid", itemid} }
        dbcursorerror := datalayer.DB.Collection("results").FindOne(context.TODO(), filter).Decode(&Result)

        if Result.ItemID == "" {
                return structures.Result{}, ErrorItemNotFound
        } else {
                return Result, dbcursorerror
        }
}

