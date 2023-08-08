// This package contains the operations for managing elements in a system
// It provides
package operations

import(
        "context"
        "fmt"
        "a10/structures"        
        "a10/utilities"
        "a10/datalayer"
        "a10/logging"
        "a10/configuration"

        "go.mongodb.org/mongo-driver/mongo/options"
        "go.mongodb.org/mongo-driver/bson"

)

func CountSessions() int64 {
        count, err := datalayer.DB.Collection("sessions").EstimatedDocumentCount(context.TODO())
        if err != nil {
                return -1
        } else {
                return count
        }
}

// AddElement is a function that takes and element structure that has a BLANK Itemid field (empty string) and stores that
// element in some database
// Successful storage returns the itemid for that element and a nil error. 
// An error is returned if an item id is given as part of the input structure.
func OpenSession(msg string) (string, error) {
        // construct the session object
        t := structures.SessionTiming{ utilities.MakeTimestamp(), 0 }
        s := structures.Session{ utilities.MakeID(), t, []string{}, []string{}, msg, structures.SessionFooter{}}
        // write to database
        _,dberr := datalayer.DB.Collection("sessions").InsertOne(context.TODO(), s)
        logging.MakeLogEntry("S","open",s.ItemID,"session",msg)

        return s.ItemID, dberr                
}


type hashablePartSession struct {
        ItemID string                                   
        Timing structures.SessionTiming                   
        ClaimList []string                    
        ResultList []string                     
        Message string                          
}

// We close the session and sign it
func CloseSession(itemid string) error {

                //Get the session, return error if not found
                session,err := GetSessionByItemID(itemid)
                if err != nil {
                        return fmt.Errorf("Element not found %v : %v",itemid,err)
                }

                //Check if the session isn't already closed
                if session.Timing.Closed != 0 {
                        return fmt.Errorf("Session already closed %v : %v",itemid,err)
                }

                //Update the session
                session.Timing.Closed = utilities.MakeTimestamp()

                // hash and sign the session
                footer,_ := hashAndSignSession(hashablePartSession{ session.ItemID, session.Timing, session.ClaimList, session.ResultList, session.Message })
                session.Footer = footer

                //Write to database
                filter := bson.D{ {"itemid", itemid} }
                closedSession,err := datalayer.DB.Collection("sessions").ReplaceOne(context.TODO(), filter, session)

                if err != nil {
                       return fmt.Errorf("Database error in close session: %v",err)
                } else if closedSession.MatchedCount != 1 || closedSession.ModifiedCount != 1 {
                        return fmt.Errorf("Session not updated: %v",err)
                } else {
                        logging.MakeLogEntry("S","close",itemid,"session","")
                        return nil
                }            
}



func AddClaimToSession(sid string, cid string) error {
        //Get the session, return error if not found
        session,err := GetSessionByItemID(sid)
        if err != nil {
                return fmt.Errorf("Element not found %v : %v",sid,err)
        }

        //ensure that the claim exists
        _,err = GetClaimByItemID(cid)
        if err != nil {
                return fmt.Errorf("Claim %v (for session) not found %v : %v",cid,sid,err)
        }

        //Update the session
        session.ClaimList = append(session.ClaimList, cid)
        
        //Write to database
        filter := bson.D{ {"itemid", sid} }
        updatedsession,err := datalayer.DB.Collection("sessions").ReplaceOne(context.TODO(), filter, session)

        if err != nil {
               return fmt.Errorf("Database error in add claim to session: %w",err)
        } else if updatedsession.MatchedCount != 1 || updatedsession.ModifiedCount != 1 {
                return fmt.Errorf("Session not updated: %w",err)
        } else {
                if configuration.ConfigData.Logging.SessionUpdateLogging == true {
                        logging.MakeLogEntry("S","update",sid,"session",fmt.Sprintf("claim,%s",cid))
                }
                return nil
         }             
}


func AddResultToSession(sid string, rid string) error {
        //Get the session, return error if not found
        session,err := GetSessionByItemID(sid)
        if err != nil {
                return  fmt.Errorf("Element not found %v : %v",sid,err)
        }

        //ensure that the claim exists
        _,err = GetResultByItemID(rid)
        if err != nil {
                return fmt.Errorf("Result %v (for session) not found %v : %v",rid,sid,err)
        }

        //Update the session
        session.ResultList = append(session.ResultList, rid)
        
        //Write to database
        filter := bson.D{ {"itemid", sid} }
        updatedsession,err := datalayer.DB.Collection("sessions").ReplaceOne(context.TODO(), filter, session)

        if err != nil {
                return fmt.Errorf("Database returned an error %w",err)
        } else if updatedsession.MatchedCount != 1 || updatedsession.ModifiedCount != 1 {
                 return fmt.Errorf("Session not updated. Error if any is %w",err)
        } else {
                if configuration.ConfigData.Logging.SessionUpdateLogging == true {
                        logging.MakeLogEntry("S","update",sid,"session",fmt.Sprintf("result,%s",rid))
                }
                return nil
         }             
}



// GetElementByItemID returns a single element or error
func GetSessionByItemID(itemid string) (structures.Session, error) {
        var elem structures.Session

        // discard the cursor, it will be an empty entry if nothing exists
        filter := bson.D{ {"itemid", itemid} }
        dbcursorerror := datalayer.DB.Collection("sessions").FindOne(context.TODO(), filter).Decode(&elem)

        if elem.ItemID == "" {
                return structures.Session{}, ErrorItemNotFound
        } else {
                return elem, dbcursorerror
        }
}


// GetElements returns a map of itemids in the ID structure. If this structure is an empty map then no elements exist in the database.
func GetSessions() ([]structures.SessionSummary, error) {
        var elems []structures.SessionSummary

        filter := bson.D{ {} }   // Get all
        options := options.Find().SetProjection(bson.D{{"itemid",1},{"timing.opened",1},{"timing.closed",1}})
        dbcursor,_ := datalayer.DB.Collection("sessions").Find(context.TODO(), filter,options)
        dbcursorerror := dbcursor.All(context.TODO(),&elems)

        return elems, dbcursorerror

}

func GetSessionsAll() ([]structures.Session, error) {
        var elems []structures.Session

        filter := bson.D{ {} }   // Get all
        options := options.Find().SetSort(bson.D{{"timing.opened",-1}})

        dbcursor,_ := datalayer.DB.Collection("sessions").Find(context.TODO(), filter,options)
        dbcursorerror := dbcursor.All(context.TODO(),&elems)

        return elems, dbcursorerror

}