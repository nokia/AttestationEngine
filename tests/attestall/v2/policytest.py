import requests

# Set the address of the A10REST endpoint and use the v2 version of the REST API
asvr = "http://192.168.1.82:8520/v2"


# Get the list of all elements and display the results
r = requests.get(asvr+"/policies")
print("Returns ",r)
print("   status code ",r.status_code)
print("   JSON        ",r.json())

print("\nNow some interesting data about what was returned")
# Extract the JSON returned
ps = r.json()
print("JSON returned has been marshalled into a python type of ",type(ps))

# Print out the individual fields and their python types
print("Field count has python type",type(ps["count"]),"and contents",ps["count"] )
print("Field policies has python type",type(ps["policies"]),"and contents",ps["policies"] )

# Print out each individual element ID and call the element endpoint to get specific information
print("\nThis structure contains the following elements:")
for p in ps["policies"]:
    r  = requests.get(asvr+"/policy/"+p)
    if r.status_code != 200:
        print("*** OK, this is weird, we found an element ID but it doesn't exist. This means it was deleted while we were doing this. So much for database consistency under concurrent conditions! If this happens then things have gone terribly, terribly wrong. You should never see this!!!")
        print(r.status_code)
    else:
        thePolicy = r.json()
        # let's extract some interesting fields
        thePolicyName = thePolicy["name"]
        thePolicyDesc = thePolicy["description"]
        # And print
        print("Policy",thePolicyName," - ",thePolicyDesc)
