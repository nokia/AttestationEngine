package operations

import(
        "context"
        "fmt"

        "a10/structures"        
        "a10/datalayer"
        "a10/utilities"

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

func GetLogEntriesSince(duration string) ([]structures.LogEntry,error) {
        var logentries []structures.LogEntry

        ts := utilities.TimeStampHoursAgo(duration)
        fmt.Printf("Duration is %v, ts is %v\n",duration,ts)

        filter := bson.D{{"timestamp", bson.D{{"$gt",ts}}}}   // Get all since given point in time

        options := options.Find().SetSort(bson.D{{"timestamp",-1}})
        
        dbcursor,_ := datalayer.DB.Collection("log").Find(context.TODO(), filter, options)
        dbcursorerror := dbcursor.All(context.TODO(),&logentries)

        return logentries, dbcursorerror
}
