package operations

import(
        "context"

        "a10/structures"        
        "a10/datalayer"

        "go.mongodb.org/mongo-driver/mongo/options"
        "go.mongodb.org/mongo-driver/bson"

)

func CountLogEntries() int64 {
        return datalayer.Count("log")
}

func GetLogEntries(maximumAmount int64) ([]structures.LogEntry,error) {
        var logentries []structures.LogEntry

        filter := bson.D{ {} }   // Get all
   
        options := options.Find().SetSort(bson.D{{"timestamp",-1}}).SetLimit(maximumAmount)
        dbcursor,_ := datalayer.DB.Collection("log").Find(context.TODO(), filter, options)
        dbcursorerror := dbcursor.All(context.TODO(),&logentries)

        return logentries, dbcursorerror
}