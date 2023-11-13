import requests
import json
import re
import os

#Main functions listed below

#create a session
def createSession(server,message):
    return mSession(server,message)

#closes session
def closeSession(server,session):
    return cSession(server,session)


#creates an attest claim
def attest(policy,server,element,session,params={}):
  
    policyId = policy_id_finder(policy, server)
    elementId = element_id_finder(element, server)
  
    return attestSendRequest(server,elementId, policyId, session, params )

#create an element
def createElement(name,description,device,tag,server):
    #sshkey.copy_key_to_device(ipaddr)
    ekExtract(device)
    akExtract(device)
    ipaddr = device.split("@",1)[1]
    akInfo = "ak.txt"
    ekInfo = "ek.txt"
    ak= extract_name_and_public_key(akInfo)
    ek= extract_name_and_public_key(ekInfo)
    elementSendRequest(name,description,f"http://{ipaddr}:8530",tag,ek,ak,server)

#creates an expected value
def createExpected(name,description,element,policy,session,claim,server):
    policyId = policy_id_finder(policy,server)
    elementId = element_id_finder(element,server)
    intent = check_policy_intent(policyId,server)

    if (intent != "tpm2/quote"):
      raise Exception(f"policy {policy} cannot be used to create an expected value, needs to have tpm2/quote intent") 


    pcrdigest,firmwareVersion = getEVs(claim,server)
    evs = {
      "attestedValue":pcrdigest,
      "firmwareVersion":str(firmwareVersion)
    }

    return expectedSendRequest(name,description,elementId,policyId,evs,server)

#creates tpm keys on specified device
def createKeys(device):
    try:
        copy_key_to_device(device)
        deleteKeys(device,["0x810100AA","0x810100EE"])
        keyCreator(device)
        return keycheck(device)
    except:
        raise Exception("key creation didnt work")

#verify a claim

def verify(rule,session,claim,server):
    return verifySendRequest(claim,session,rule,server)


#Helper functions listed below

#Attest controller functions
#returns a format with all the variables required for the json file
def attestCreateJson(elementId,policId,sessionId,params):
    dictionary = {
    "eid":elementId,
    "pid":policId,
    "sid":sessionId,
    "parameters":params
    }
    return dictionary

#sends the created json file to end point attest, returns the attest claim id. This is used in the createExpected.py to automatically attest a device for 
#the creation of an expected value

def attestSendRequest(server,elementId,policId,sessionId,params):
   
    json_object = attestCreateJson(elementId,policId,sessionId,params)
    try:
        r = requests.post(f'http://{server}/attest', json=json_object)
    except:
        raise Exception("request to attest was not successfull")
        
    itemId = r.json()
    print(itemId['itemid'])
    return (itemId['itemid'])

#verify controller helper functions below

def verifyCreateJson(claimId,sessionId,rule):
    dictionary = {
    "cid":claimId,
    "rule":rule,
    "sid":sessionId,
    "parameters":{}
    }
    return dictionary

def verifySendRequest(claimId,sessionId,rule,server):
    # Serializing json
    json_object = verifyCreateJson(claimId,sessionId,rule)
    
    try:
        r = requests.post(f'http://{server}/verify', json=json_object)
    except:
        raise Exception("request to verify was not successfull")
    return r.json()


#Element creation controller helper functions below

#extracts the AK from the device, by using specific tpm2 tools and by utilizing tpm2_send over ssh. Takes the device ip address as the input

def akExtract(user):
    ak = f"tpm2_readpublic -c 0x810100AA -o /tmp/ak -fpem --tcti='cmd:ssh {user} tpm2_send'"
    
    # Execute the command and capture the output
    outputAk = os.popen(ak).read()
    # Save the command output to the specified file
    with open("ak.txt", "w") as file:
        file.write(outputAk)
    
    # Save the contents of the /tmp/k file
    os.system("cat /tmp/ak >> ak.txt")

#extracts the EK from the device, by using specific tpm2 tools and by utilizing tpm2_send over ssh. Takes the device ip address as the input

def ekExtract(user):
    ek = f"tpm2_readpublic -c 0x810100EE -o /tmp/ek -fpem --tcti='cmd:ssh {user} tpm2_send'"

    outputEk = os.popen(ek).read()
    # Save the command output to the specified file
    with open("ek.txt", "w") as file:
        file.write(outputEk)
    
    # Save the contents of the /tmp/k file
    os.system("cat /tmp/ek >> ek.txt")

#extracts the name and public key from Ek and AK files
def extract_name_and_public_key(file_path):
    hand=""
    
    if(file_path == "ak.txt"):
        hand = "0x810100AA"
    else:
        hand = "0x810100EE"

    with open(file_path, "r") as file:
        file_content = file.read()

    name_match = re.search(r"name: (\w+)", file_content)
    public_key_match = re.search(r"(-----BEGIN PUBLIC KEY-----.*?-----END PUBLIC KEY-----)", file_content, re.DOTALL)

    name = name_match.group(1) if name_match else None
    public_key = public_key_match.group(1).replace("\n", "") if public_key_match else None
    result = {"public":public_key,"handle":hand, "name":name}
    os.remove(file_path)
    return result


#returns a format with all the variables required for the json file
def elementCreateJson(name,desc,endpoint,tag,ek,ak):
    dictionary = {
     "name":name,
     "description":desc,
     "endpoint": endpoint,
     "protocol":"A10HTTPRESTv2",
     "tags":["pi","tpm2",tag],
     "sshkey":{"key":"","timeout":0,"username":""
     },
     "tpm2":{"device":"/dev/tpmrm0","ekcerthandle":"0x1c0002","ek":ek,
     "ak":ak
     },
     "uefi":{"eventlog":""
     },
     "ima":{"asciilog":""
     },
     "txt":{"log":""
     },
    }
    return dictionary

#Creates an element by sending a post request to end point /elements
def elementSendRequest(name,desc,endpoint,tag,ek,ak,server):
    # Serializing json
    json_object = elementCreateJson(name,desc,endpoint,tag,ek,ak)
    

    r = requests.post(f'http://{server}/element', json=json_object)

    print(r.ok)



#Expected value controller helper functions below

#check to see if a an expected value already exists for the same element and policy
def check_expected(policyId, elementId, server):
    r = requests.get(f'http://{server}/expectedValue/{elementId}/{policyId}')
    if(r.ok):
        j = json.loads(r.text)
        print("JSON=",j["itemid"])
        return r.ok,j["itemid"]
    else:
        return r.ok, ""

#Finds policy id from policy name as input
def policy_id_finder(policy,server):
    #get all policies
    r = requests.get(f'http://{server}/policies')
    policies = r.json()
    size = policies['length']
    #check which policy id matches the name given
    for x in range (0,size):
        element = policies['policies'][x]
        r1 = requests.get(f'http://{server}/policy/{format(element)}')
        availablePolicies = r1.json()
        
        if(availablePolicies['name'].lower() == policy.lower()):
            return(availablePolicies['itemid'])
    
    raise Exception(f"No such policy exists, please check the name")

#checks if the policy intent is tpm2/quote as these require and expected value
def check_policy_intent(policyId,server):
    r = requests.get(f'http://{server}/policy/{policyId}')
    intent = r.json()['intent']
    return intent
    

#Finds element id from element name as input
def element_id_finder(element, server):
    #get all elements
    try:
        r = requests.get(f'http://{server}/elements')
        elements = r.json()
        size = elements['length']
    except:
        raise Exception("couldnt connect to server")
    
    #check which element id matches the name given
    for x in range (0,size):
        try:
            el = elements['elements'][x]
            r1 = requests.get(f'http://{server}/element/{format(el)}')
            specificElement = r1.json()
        except:
            raise Exception("couldnt connect to server")

        if(specificElement['name'].lower() == element.lower()):
            return(specificElement['itemid'])
    
    raise Exception(f"No such element exists, please check the name")

#returns a format with all the variables required for the json file
def createEVstructure(name,desc,element,policy,evs):
    dictionary = {
    "description": desc,
    "name": name,
    "elementid": element,
    "policyid": policy,
    "evs": evs
    }
    return dictionary

#returns the values for the pcrdigest and firmwareVersion of the corresponding attestClaim. Takes in the attest claim id as an input.
def getEVs(attestClaimId, server):
    r = requests.get(f'http://{server}/claim/{format(attestClaimId)}')
    info = r.json()['body']
    pcrdigest= info['AttestedQuoteInfo']['PCRDigest']
    firmwareVersion= info['FirmwareVersion']

    return pcrdigest,firmwareVersion

#Creates an expected value by posting the required json file to endpoint /expectedValue
def expectedSendRequest(name,desc,elementId,policyId,evs, server):
    # Serializing json
    evstr = createEVstructure(name,desc,elementId,policyId,evs)

    expected = check_expected(policyId,elementId,server)


    if(expected[0]):
        # add itemid to json object
        evstr["itemid"] = expected[1]
        r = requests.put(f'http://{server}/expectedValue', json=evstr)
        print("Expected value has been updated")
    else:
        r = requests.post(f'http://{server}/expectedValue', json=evstr)
        print("New expected value has been created")

    print(r.text)
    


#keys controller helper functions below

def keycheck(device):
    try:
        keys = os.system(f"tpm2_getcap handles-persistent --tcti='cmd:ssh {device} tpm2_send'")
    except:
        raise Exception("key check failed, check the device username and ip address")
    print(keys)

#device variable gives the username and ip address of the device in the form <username>@<ipaddress>
#handles variable is a list of all the key handles that need to be deleted

def deleteKeys(device,handles):
    for key in handles:
        try:
            os.system(f"tpm2_evictcontrol -c {key} --tcti='cmd:ssh {device} tpm2_send'")
            os.system(f"tpm2_getcap handles-persistent --tcti='cmd:ssh {device} tpm2_send'")
        except:
            raise Exception("key deletion didnt work, check the device username, ip address and handle names")

#this function creates an EK and AK, requires parameter device given as <username>@<ipaddress>

def keyCreator(device):
    try:
        os.system(f"tpm2_createek -c 0x810100EE -G rsa -u ek.pub --tcti='cmd:ssh {device} tpm2_send'")
        os.system(f"tpm2_getcap handles-persistent --tcti='cmd:ssh {device} tpm2_send'")
        os.system(f"tpm2_createak -C 0x810100EE -c ak.ctx -G rsa -g sha256 -s rsassa -u ak.pub -f pem -n ak.name --tcti='cmd:ssh {device} tpm2_send'")
        os.system(f"tpm2_evictcontrol -c ak.ctx 0x810100AA --tcti='cmd:ssh {device} tpm2_send'")
    except:
        raise Exception("key creation didnt work, check the device username and ip address")

def copy_key_to_device(device):
    try:
        return os.system(f"ssh-copy-id -i ~/.ssh/id_rsa.pub {device}")
    except:
        raise Exception("copying ssh keys to device didnt work, please check the device username and ip address")



#session controller helper functions below

#Creates a session and return the session id
def mSession(ipaddr, message):
    try:
        r = requests.post(f'http://{ipaddr}/session', json={"message":message})
        session = r.json()
        print(session['itemid'])
        return session['itemid']
    except:
        raise Exception("something went wrong with session creation")

#closes a session by sessionId
def cSession(ipaddr, sessionId):
    try:
        r = requests.delete(f'http://{ipaddr}/session/{sessionId}')
    except:
        raise Exception("could not delete session")