import requests

# Set the address of the A10REST endpoint and use the v2 version of the REST API
asvr = "http://192.168.1.82:8520/v2"


# Get the list of all elements and display the results
r = requests.get(asvr+"/elements")
print("ES returns ",r)
print("   status code ",r.status_code)
print("   JSON        ",r.json())

print("\nNow some interesting data about what was returned")
# Extract the JSON returned
es = r.json()
print("JSON returned has been marshalled into a python type of ",type(es))

# Print out the individual fields and their python types
print("Field count has python type",type(es["count"]),"and contents",es["count"] )
print("Field count has python type",type(es["elements"]),"and contents",es["elements"] )

# Print out each individual element ID and call the element endpoint to get specific information
print("\nThis structure contains the following elements:")
for e in es["elements"]:
    r  = requests.get(asvr+"/element/"+e)
    if r.status_code != 200:
        print("*** OK, this is weird, we found an element ID but it doesn't exist. This means it was deleted while we were doing this. So much for database consistency under concurrent conditions! If this happens then things have gone terribly, terribly wrong. You should never see this!!!")
    else:
        theElement = r.json()
        # let's extract some interesting fields
        theElementName = theElement["name"]
        theElementDesc = theElement["description"]
        # And print
        print("Element",theElementName," - ",theElementDesc)
