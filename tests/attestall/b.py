import requests
import ast
import threading

asvr = "http://192.168.1.82:8520"

#
# This version uses threading
#
# I should add joins to this
#

def processVerify(k):
	# k is a claim rule structure
	vr = requests.post(asvr+"/verify", json=k).text
	print("V ",k,vr)


def getResults(cl,rs):
	# cl is the claim id
	# rs list rule tuples ( "rule name", "rule parameters"), eg: [ ( "tpm2rules/PCRsAllUnassigned", {"bank":"sha256"} ), ... ]
	for r in rs:
		k = { "cid":cl,"rule": r }
		thr = threading.Thread(target=processVerify, args=(k,))
		thr.start()
		print("   verify thread ",thr)


def getClaimAndResults(j,rs):
	# j is the claim request structure with eid,pid and cps
	# rs is the result set to calculate

	cl = requests.post(asvr+"/attest", json=j).text
	print("  claim ",cl)

	getResults(cl,rs)
	

def attestByType(t,p,rs):
	# t type
	# p policy name

	es = ast.literal_eval(requests.get(asvr+"/elements/type/"+t).text)
	print("Attesting ",len(es)," elements of type ",t)

	for e in es:
		el = ast.literal_eval(requests.get(asvr+"/element/"+e).text)
		pl = ast.literal_eval(requests.get(asvr+"/policy/name/PCR read").text)
		j={ "eid":"", "pid":"", "cps":{} }
		
		if el["protocol"]=="A10TPMSENDSSL":
			j["cps"]["a10_tpm_send_ssl"] = el["a10_tpm_send_ssl"]
		else:
			j["cps"]={}

		j["eid"]=el["itemid"]
		j["pid"]=pl["itemid"]

		thr = threading.Thread(target=getClaimAndResults, args=(j,rs,))
		thr.start()
		print("   claim thread ",thr)

	print(".\n")


#
# Stress Test ... uncomment
#
# for x in range(1,20):

# 	attestByType("pi","PCR read",
# 	[ ( "tpm2rules/PCRsAllUnassigned", {"bank":"sha256"} ),
# 		( "tpm2rules/PCRsAllUnassigned", {"bank":"sha1"} ) ]
# 	)

# 	attestByType("tpm2","PCR read",
# 	[ ( "tpm2rules/PCRsAllUnassigned", {"bank":"sha256"} ),
# 		( "tpm2rules/PCRsAllUnassigned", {"bank":"sha1"} ) ]
# 	)

#
# Not stress test
#

attestByType("pi","PCR read",
	[ ( "tpm2rules/PCRsAllUnassigned", {"bank":"sha256"} ),
		( "tpm2rules/PCRsAllUnassigned", {"bank":"sha1"} ) ]
	)

attestByType("tpm2","PCR read",
	[ ( "tpm2rules/PCRsAllUnassigned", {"bank":"sha256"} ),
		( "tpm2rules/PCRsAllUnassigned", {"bank":"sha1"} ) ]
	)	