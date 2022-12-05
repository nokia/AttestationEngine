import paho.mqtt.client as mqtt
import paho.mqtt.subscribe as subscribe
import json
import ast
import threading
import argparse
import queue
from queue import Queue
import csv
from datetime import datetime
import math
import requests
from pathlib import Path


# Read config file
def read_config():
    p = Path(__file__).with_name('aggregation_config.json')
    with p.open('r') as f:
        return json.loads(f.read())


IP = read_config()["MQTThost"]
PORT = read_config()["MQTTport"]
url = read_config()["PicRestPath"]

# Starts listening to changes in the topic given to the client.subscribe() function.
def on_connect(client, userdata, flags, rc):
    print("connected with result code " + str(rc))

    client.subscribe("alert")
    client.subscribe("management")
    client.subscribe("LuxSensor/lux")
    client.subscribe("IRSsensor/temperature")
    client.subscribe("IRSensor/pixels")
    client.subscribe("TofSensor/tof")

ap = argparse.ArgumentParser(description='Displays the log file in real-time')
args = ap.parse_args()

# Create separate ques for each sensors' data
lux_q = queue.LifoQueue()
tof_q = queue.LifoQueue()
temp_q = queue.LifoQueue()
pix_q = queue.LifoQueue()


# Create new file 
def createNewFile(name):
    try:
        file=open(name, "w")
        writer=csv.writer(file)
        # Create header row
        writer.writerow(["lux", "tof", "temp", "pix", "time"])
        file.close()
    except:
        print("Error: File creation failed")

# Always create new file when program starts
t = datetime.now()
filename="test-"+str(t.strftime('%m-%d-%Y_%H-%M-%S'))+".csv"
createNewFile(filename)

# Takes a picture if it is light enough(lux_status), warm enough(ir_status), and someone is close(tof_status) 
def takePicture():
    global url
    works = False
    if lux_status and ir_status and tof_status:
        response = requests.get(url)
        print(response)
        client.publish("alert", payload=("PICTURE TAKEN!"))
        works = True
    return works


# Handles incoming alert messages
def handleAlert(payload):
    print("alert detected: " + payload)

# Handles incoming management messages
def handleManagement(payload):
    print("management message detected: " + payload)

# Datacounter for data handling
dataCount = 0
# Default statuses is false
lux_status = False
tof_status = False
ir_status = False
# Handles incoming data payloads
def handleData(topic, payload):
    global dataCount
    global lux_q
    global tof_q
    global pix_q
    global temp_q
    global lux_status
    global tof_status
    global ir_status
    dataSaveFq = 30 #default value (when dark, cold and no presence detected)

    # Increases data saving frequency if it is light and warm
    if lux_status:
        dataSaveFq = 20
    if lux_status and ir_status:
        dataSaveFq = 5

    # Handle data and put the data in appropriate queue
    if topic == "LuxSensor/lux":
        lux_q.put(payload, "lux")

        # Check the status if it's light or not and compare to previous status
        change = True
        status = bool(float(payload) > 46)
        if status != lux_status:
            change = True
        else:
            change = False

        if change:
            if status:
                client.publish("alert", payload=json.dumps({"name": "iotp015", "message": "Status: Light"}))
                print("Status: Light")
            else:
                client.publish("alert", payload=json.dumps({"name": "iotp015", "message": "Status: Dark"}))
                print("Satus: Dark")

        lux_status = status

    if topic == "data/iotpi014/sensor/ir/temperature":
        temp_q.put(payload, "temp")
    
    if topic == "IRSensor/pixels":
        pix_q.put(payload, "pix")    

        # Check the status if it's warm or not and compare to previous status
        change = True
        status = bool(float(payload) > 24)
        if status != ir_status:
            change = True
        else:
            change = False

        if change:
            if status:
                client.publish("alert", payload=json.dumps({"name": "iotp014", "message": "Status: HOT"}))
                print("Status: HOT")
            else:
                client.publish("alert", payload=json.dumps({"name": "iotp014", "message": "Status: COLD"}))
                print("Satus: COLD")
        
        ir_status=status

    if topic == "TofSensor/tof":
        tof_q.put(payload, "tof")

        # Check the status if there's someone close or not and compare to previous status
        change = True
        status = bool(float(payload) > 800)
        if status != tof_status:
            change = True
        else:
            change = False

        if change:
            if status:
                client.publish("alert", payload=json.dumps({"name": "iotp016", "message": "Status: presence detected"}))
                print("Status: presence")
            else:
                client.publish("alert", payload=json.dumps({"name": "iotp016", "message": "Status: presence not detected"}))
                print("Satus: NO presence")
        
        tof_status=status
    
    dataCount = dataCount +1

    # Calls the data save function for every given data points gathered
    if dataCount>dataSaveFq:
        dataCount=0
        getFromQueues()
    

pic_status = True
# Save counter for queue process
saveCount=0
# Saves data from the queues to a csv file
def getFromQueues():
    # Get the values from the queues
    if lux_q.empty():
        lq = 0
    else:
        lq=lux_q.get()
    
    if tof_q.empty():
        tq = 0
    else:
        tq=tof_q.get()

    if temp_q.empty():
        teq = 0
    else:
        teq=temp_q.get()

    if pix_q.empty():
        pixq = 0
    else:
        pixq=temp_q.get()

    # Because get() pulls the value out of the queue, they have to be put back in,
    # just in case there are no new values before the next pull. 
    # If not done, the queue would go backwards in time.
    # Is this what would be called "kludge"?
    lux_q.put(lq)
    tof_q.put(tq)
    temp_q.put(teq)
    pix_q.put(pixq)

    print("From lux queue: " + str(lq) + "; From tof queue: " + str(tq) + "; from temp queue: " + str(teq) + "; from pix queue: " + str(pixq))
    
    # Write the data as a new row to file
    global filename
    global saveCount
    data =[int(lq), int(tq), int(teq), int(pixq), str(datetime.now())]
    try:
        file = open(filename, "a")
        writer = csv.writer(file)
        writer.writerow(data)
        file.close()
    except:
        print("Error: File opening failed")

    saveCount = saveCount+1
   
    global pic_status

    # Picture is taken only if pic_status is True, 
    # so the camera doesn't just snap photos all the time when there is someone close.
    # Taking a pic changes pic_status to false and it's turned back to True after 100 saved datapoints.
    if saveCount > 100:
        pic_status = True
    if pic_status:
        if takePicture():    
            # Creates a new file if picture is taken
            t = datetime.now()
            filename="test-"+str(t.strftime('%m-%d-%Y_%H-%M-%S'))+".csv"
            createNewFile(filename)
            pic_status = False
            saveCount = 0
    

# Sends message for appropriate sub-routines for handling
def processMessage(payload, topic):
    print(topic+" "+str(payload))   

    if topic == "alert":
        handleAlert(payload)

    else:
        if topic == "management":
            handleManagement(payload)
        else:
            handleData(topic, payload)


# Starts a processing thread everytime a message is received
def on_message(client, userdata, msg):
    x = threading.Thread(target=processMessage, args=(msg.payload, msg.topic))
    x.start()




client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message


client.connect(IP, PORT, 60)

client.loop_forever()

# Sends default statuses: dark, no presence and cold
client.publish("alert", payload=json.dumps({"name": "iotp015", "message": "Status: Dark"}))
client.publish("alert", payload=json.dumps({"name": "iotp016", "message": "Status: presence not detected"}))
client.publish("alert", payload=json.dumps({"name": "iotp014", "message": "Status: COLD"}))
