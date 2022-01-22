import requests
import ast

asvr = "http://192.168.1.82:8520"

es = ast.literal_eval(requests.get(asvr+"/elements/type/pi").text)

for e in es:
	el = ast.literal_eval(requests.get(asvr+"/element/"+e).text)
	pl = ast.literal_eval(requests.get(asvr+"/policy/name/PCR read").text)
	print("Attesting ",el["name"],el["protocol"]," with ",pl["name"])

	j={ "eid":"", "pid":"", "cps":{} }

	if el["protocol"]=="A10TPMSENDSSL":
		j["cps"]["a10_tpm_send_ssl"] = el["a10_tpm_send_ssl"]
	else:
		j["cps"]={}

	j["eid"]=el["itemid"]
	j["pid"]=pl["itemid"]
	  
	cl = requests.post(asvr+"/attest", json=j).text
	print("Claim id ",vr)

	k = { "cid":cl,
		  "rule":( "tpm2rules/PCRsAllUnassigned", {"bank":"sha1"} ) }
	vr = requests.post(asvr+"/verify", json=k).text
	print("Result id ",vr)

	k = { "cid":cl,
		  "rule":( "tpm2rules/PCRsAllUnassigned", {"bank":"sha256"} ) }
	vr = requests.post(asvr+"/verify", json=k).text
	print("Result id ",vr)
