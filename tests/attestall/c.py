import requests
import ast

asvr = "http://192.168.1.82:8520"

es = ast.literal_eval(requests.get(asvr+"/elements/type/pi2").text)

for e in es:
    el = ast.literal_eval(requests.get(asvr+"/element/"+e).text)
    pl = ast.literal_eval(requests.get(asvr+"/policy/name/Check Credentials").text)
    print("\n\nAttesting ",el["name"],el["protocol"]," with ",pl["name"])

    j={ "eid":"", "pid":"", "cps":{} }

    if el["protocol"]=="A10TPMSENDSSL":
        j["cps"]["a10_tpm_send_ssl"] = el["a10_tpm_send_ssl"]
    else:
        j["cps"]={}

    j["eid"]=el["itemid"]
    j["pid"]=pl["itemid"]
      
    #for check credentials (intent is tpm2/credentialcheck) we need
    #the ekpub and akname from the element

    akname = el["tpm2"]["tpm0"]["akname"]
    ekpub = el["tpm2"]["tpm0"]["ekpem"]

    #print(akname,ekpub)

    #load these into the cps structure

    j["cps"]["akname"] = akname
    j["cps"]["ekpub"] = ekpub


    cl = requests.post(asvr+"/attest", json=j)
    cid = cl.text


    if cl.status_code == 201:
        k = { "cid":cid,"rule":( "tpm2rules/TPM2CredentialVerify", {} ) }

        print("   k=",k)

        vr = requests.post(asvr+"/verify", json=k).text
        print("   Verify result is ",vr)
    else:
        print("   Failed to get a claim because ",cl.text)
        
