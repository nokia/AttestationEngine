package datalayer

import(
    "context"
    "log"
    "reflect"

   //     "go.mongodb.org/mongo-driver/mongo/options"
        "go.mongodb.org/mongo-driver/bson"    
)

func Count(coll string) int64 {
	//options := options.Count()
	count, err := DB.Collection(coll).CountDocuments(context.TODO(), bson.D{})

	log.Printf("count for %v is %v %v, err is %v",coll,count,reflect.TypeOf(count),err)

    if err != nil {
        return -1
    } else {
        return count
    }
}