// This package contains the operations for managing elements in a system
// It provides
package operations

import(
        "fmt"
        "context"

        "a10/structures"        
        "a10/datalayer"
        "a10/logging"

        "go.mongodb.org/mongo-driver/mongo/options"
        "go.mongodb.org/mongo-driver/bson"

)

func CountOpaqueOjects() int64 {
        return datalayer.Count("hashes")
}

func AddOpaqueObject(h structures.OpaqueObject) (string, error) {
        options := options.Update().SetUpsert(true)
        filter := bson.D{{"value",h.Value}}
        //update := bson.D{{ "$set", bson.D{{ h }}}}
        _,dberr := datalayer.DB.Collection("opaqueobjects").UpdateOne(context.TODO(), filter, h, options)
        msg := fmt.Sprintf("%s,%s",h.Type,h.ShortDescription)
        logging.MakeLogEntry("IM","add",h.Value,"object",msg)
        return h.Value, dberr                

}

func UpdateOpaqueObject(replacement structures.OpaqueObject) (string, error) {
        return AddOpaqueObject(replacement)
}

func DeleteOpaqueObject(v string) error {       
        filter := bson.D{{"value",v}}
        deleteresult, err := datalayer.DB.Collection("opaqueobjects").DeleteOne(context.TODO(), filter)

        if err != nil {
                return err
        } else if deleteresult.DeletedCount != 1 {
                return ErrorItemNotDeleted
        } else {
                logging.MakeLogEntry("IM","delete",v,"object","")
                return nil
        }
}


func GetOpaqueObjects() ([]structures.OpaqueObject, error) {
        var elems []structures.OpaqueObject

        filter := bson.D{ {} }   // Get all
        dbcursor,_ := datalayer.DB.Collection("opaqueobjects").Find(context.TODO(), filter)
        dbcursorerror := dbcursor.All(context.TODO(),&elems)

        return elems, dbcursorerror
}



// GetElementByItemID returns a single element or error
func GetOpaqueObjectByValue(v string) (structures.OpaqueObject, error) {
        var elem structures.OpaqueObject

        // discard the cursor, it will be an empty entry if nothing exists
        filter := bson.D{ {"value", v} }
        dbcursorerror := datalayer.DB.Collection("opaqueobjects").FindOne(context.TODO(), filter).Decode(&elem)

        if elem.Value == "" {
                return structures.OpaqueObject{}, ErrorItemNotFound
        } else {
                return elem, dbcursorerror
        }
}
