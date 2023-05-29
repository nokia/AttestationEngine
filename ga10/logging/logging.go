package logging

import(
	"log"
	"fmt"
	"context"
	"sync"
	"os"

	"a10/utilities"
	"a10/datalayer"
	"a10/structures"
	"a10/configuration"
)


func MakeLogEntry(ch string, op string, itemid string, itemtype string, message string){

	logentry := structures.LogEntry{ utilities.MakeID(), utilities.MakeTimestamp(), ch, op, itemid, itemtype, message, []byte{} }

	if digest,err := utilities.MakeSHA256(logentry); err != nil {
		log.Printf("WARNING: Encoding log entry failed with %w. Entry not made.",err)
	} else {
		logentry.Hash = digest

		// these can be run in parallel. We don't care about return results
		var wg sync.WaitGroup

		wg.Add(3)

		go writeToDB(&wg,logentry)
		go writeToMessaging(&wg,logentry)
		go writeToLogfile(&wg,logentry) 

		wg.Wait()
	}

}


func writeToDB(wgrp *sync.WaitGroup, l structures.LogEntry){
	_,_ = datalayer.DB.Collection("log").InsertOne(context.TODO(), l)

	wgrp.Done()
}


func writeToMessaging(wgrp *sync.WaitGroup, l structures.LogEntry){
	ch :=  fmt.Sprintf("AS/%s",l.Channel)
	_ = datalayer.MESSAGING.Publish(ch,0,false,makeCSVText(l))

	wgrp.Done()
}


func writeToLogfile(wgrp *sync.WaitGroup, l structures.LogEntry){
	f,err := os.OpenFile(configuration.ConfigData.Logging.LogFileLocation,
		os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0644)
	if err != nil {
		log.Println("Error opening log file ",configuration.ConfigData.Logging.LogFileLocation)
	}
	defer f.Close()

	if _, err := f.WriteString(makeCSVText(l)); err != nil {
		log.Println("Error writring to log file ",configuration.ConfigData.Logging.LogFileLocation)
	}

	wgrp.Done()
}


func makeCSVText(l  structures.LogEntry) string {
	return fmt.Sprintf("%s,%v,%s,%s,%s,%s,%q\n",l.ItemID, l.Timestamp, l.Channel, l.Operation, l.RefID, l.RefType, l.Message)
}