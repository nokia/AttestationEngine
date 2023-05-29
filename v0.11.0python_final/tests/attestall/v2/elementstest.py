import requests
import json

print("Elements end point tests")

# Set the address of the A10REST endpoint and use the v2 version of the REST API
asvr = "http://192.168.1.82:8520/v2"

print("NAE address is ",asvr)

print("\n\nPART 1 - /elements and /element endpoints\n\n")

# Get the list of all elements and display the results
r = requests.get(asvr+"/elements")
print("Returns ",r)
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


print("\n\nPART 2- /types and /element/type endpoints\n\n")

# Get the list of all types
r = requests.get(asvr+"/elements/types")
print("Returns ",r)
print("   status code ",r.status_code)
print("   JSON        ",r.json())

typelist = r.json()["types"]

#Show the number of elements for each type
print("\nThe nunber of elements with each type is:")
for t in typelist:
    r = requests.get(asvr+"/elements/type/"+t)
    count = r.json()["count"]
    print(count,"elements have type",t)




print("\n\nPART 3- add, modify and delete an element \n\n")

e = {
    "description": "TEST",
    "endpoint": "http://192.168.1.82:8530",
    "location": "60.10.15N 24.56.15E",
    "name": "Test",
    "protocol": "A10HTTPREST",
    "tpm2": {
        "tpm0": {
            "akhandle": "0x810100aa",
            "akname": "000b156990c5ec39e69632f355ac172e23584ddd2da8d608f780d407b520fdc142c4",
            "akpem": "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvwAY5SZBikkMSyliARbcu/zwFimSZ6smiXz1uywlQ1zVlUluU3ISpp/sHmvYtQ5LRquLtUWALpX4jhSPRRS2X4whSXiW3M9QqMRD5x/XSHaM/YMDlZzc6Mt65G5iSKZHLIn45XEGn+bxn3azZCOHTWJHLYluBbf1BmUSjIMNxVwTImzfMePQazxXyD6YDdNIVTZkDiQSzmYV1haWaAW2XfOcPy+4bQXhcaBPk7xS6R6I621VEiNfVUv8FHU47O0UcwMRYGOQZIroSWl6hPtlO0lQx9ik+x3BLMAisvgj+u6ySeqYus8zgYNR06Uy7vgnL6mgIFcV8Zpys2Raw3bUIQIDAQAB-----END PUBLIC KEY-----",
            "ekhandle": "0x810100ee",
            "ekname": "000bde0fab6bdbe5b2032c11aceb9a676555842dc1eef292965c6f33dc47a5e9cf21",
            "ekpem": "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArtFXyVd6mH0CKDtew2Q9GVekErHUEfpqe+rE2qLwhV7vnRkX1U9PoYA4ITgNVzEnw36e+zsjh00nQfNm4KNVI2Dfr6aBckQ4pzrwjxogZuEMhzDhUqnw7PnzAusQ02A7JrAmAX0+3+YE1OpQbP8Ozw+pct7WbxGQtJJVQ5tIlpdpJOi6nQu7pJq+M+m4fYTIzddywaGbXxu1WHSPWQoWUGDUPsP8sQOm/c+c0sA7CfVe/vvXVHTO3sDgu6FvEvr4A6CPlDQX3f6tppr+YUJBuZgx7T5Xvdk0nSS/jrLjhtt0OdsNfOAGmuJAmERWIS945X1eyI1U33eNPJRXhFNcywIDAQAB-----END PUBLIC KEY-----"
        },
        "tpm1": {
            "akhandle": "0x810100AA",
            "akname": "x",
            "akpub": "x",
            "ekhandle": "0x810100EE",
            "ekname": "x",
            "ekpub": "x"
        }
    },
    "type": [
        "swtpm",
        "ibmtpmsim"
    ],
    "uefi": {
        "eventlog": "/sys/kernel/security/tpm0/binary_bios_measurements"
    }
}

print("Adding an element called TEST")
r = requests.post(asvr+"/element",json = e)
print("Return code is ",r.status_code,"and content is ",r.json())
elementItemID=r.json()["itemid"]
print("The itemid is ",elementItemID)


eurl=asvr+"/element/"+elementItemID


print("Now getting the element with URL ",eurl)
r = requests.get(eurl)
ename = r.json()["name"]
print("Return code is ",r.status_code," and the element name is ",ename)


print("Now we want to change the name of the element to PRAWF_UN and location to Helsinki using the PUT HTTP verb")
modification = { "name":"PRAWF_UN","location":"Helsinki","itemid":elementItemID}
r = requests.put(eurl, json=modification)
print("Return code is ",r.status_code," Content is",type(r.json()),"and content is",r.json())


print("Now we want to change the name of the element to PRAWF_DAU and location to Caerdydd using the PATCH HTTP verb")
modification = { "name":"PRAWF_DAU","location":"Caerdydd","itemid":elementItemID}
r = requests.patch(eurl, json=modification)
print("Return code is ",r.status_code," Content is",type(r.json()),"and content is",r.json())


print("Now getting the element with URL ",eurl," Name should be CAERDYDD")
r = requests.get(eurl)
ename = r.json()["name"]
print("Return code is ",r.status_code," and the element name is ",ename)



print("Finally delete the element with URL ",eurl)
r = requests.delete(eurl)
print("Return code is ",r.status_code," and the element name is ",ename)


print("Now getting the element with URL ",eurl," THIS SHOULD FAIL WITH A 404 CODE")
r = requests.get(eurl)
print("Return code is ",r.status_code," and the element name is ",r.json())
