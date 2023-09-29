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

func isClaimError(c structures.Claim) bool {
        return c.BodyType == structures.CLAIMERROR
}

func CountClaims() int64 {
        return datalayer.Count("claims")
}

// AddElement is a function that takes and element structure that has a BLANK Itemid field (empty string) and stores that
// element in some database
// Successful storage returns the itemid for that element and a nil error. 
// An error is returned if an item id is given as part of the input structure.
func AddClaim(e structures.Claim) (string, error) {
        if (e.ItemID != "") {
                return "", ErrorItemIDIncluded
        } else {
                e.ItemID = utilities.MakeID()
                _,dberr := datalayer.DB.Collection("claims").InsertOne(context.TODO(), e)
                logging.MakeLogEntry("C","add",e.ItemID,"claim","")
                return e.ItemID, dberr                
        }
}


// GetElements returns a map of itemids in the ID structure. If this structure is an empty map then no elements exist in the database.
func GetClaims() ([]structures.ID, error) {
        var claims []structures.ID

        filter := bson.D{ {} }   // Get all
        options := options.Find().SetProjection(bson.D{{"itemid",1}})
        dbcursor,_ := datalayer.DB.Collection("claims").Find(context.TODO(), filter,options)
        dbcursorerror := dbcursor.All(context.TODO(),&claims)

        return claims, dbcursorerror
}

func GetClaimsAll() ([]structures.Claim, error) {
        var claims []structures.Claim

        filter := bson.D{ {} }   // Get all
        options := options.Find().SetSort(bson.D{{"header.timing.requested",-1}})

        dbcursor,_ := datalayer.DB.Collection("claims").Find(context.TODO(), filter, options)
        dbcursorerror := dbcursor.All(context.TODO(),&claims)

        fmt.Printf("\n********\nClaims\n error %w \n claims %v \n***  \n",claims,dbcursorerror)

        return claims, dbcursorerror
}

func GetClaimsByElementID(eid string, maximumAmount int64) ([]structures.Claim, error) {
        var claims []structures.Claim

        if maximumAmount<1 {
                return claims, fmt.Errorf("Maximum amount must be a positive number")
        }

        fmt.Println("Getting for claim by element eid = %v\n",eid)
        filter := bson.D{ {"header.element.itemid",eid } }     // TODO search for itemIDs only
        options := options.Find().SetSort(bson.D{{"header.timing.requested",-1}}).SetLimit(maximumAmount)
        dbcursor,_ := datalayer.DB.Collection("claims").Find(context.TODO(), filter, options)
        dbcursorerror := dbcursor.All(context.TODO(),&claims)

        return claims, dbcursorerror
}


// GetElementByItemID returns a single element or error
func GetClaimByItemID(itemid string) (structures.Claim, error) {
        var claim structures.Claim

        // discard the cursor, it will be an empty entry if nothing exists
        filter := bson.D{ {"itemid", itemid} }
        dbcursorerror := datalayer.DB.Collection("claims").FindOne(context.TODO(), filter).Decode(&claim)

        if claim.ItemID == "" {
                return structures.Claim{}, ErrorItemNotFound
        } else {
                return claim, dbcursorerror
        }
}

