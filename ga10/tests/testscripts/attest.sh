#!/bin/sh 

#work laptop and tpm2/quote policy
#curl -X GET http://127.0.0.1:8520/element/c7219a2b-7d02-4c1f-bad7-50c4d3b2cd2a
#curl -X GET http://127.0.0.1:8520/policy/6770b5b9-d0ea-4f8b-817f-cab1bf8169c0

#open session
osid="$( curl -s -X POST -H "Content-Type: application/json" http://127.0.0.1:8520/session   | jq -r '.itemid' )"
echo "Session opened: http://127.0.0.1:8520/session/$osid"

#attest
cid="$( curl -s -X POST -H "Content-Type: application/json" -d '{"eid":"c7219a2b-7d02-4c1f-bad7-50c4d3b2cd2a","pid":"6770b5b9-d0ea-4f8b-817f-cab1bf8169c0", "sid": "'"$osid"'", "parameters":{}}' http://127.0.0.1:8520/attest  | jq -r '.itemid' )"
echo "Claim is: http://127.0.0.1:8520/claim/$cid"

#verify
#rid="$( curl -s -X POST -H "Content-Type: application/json" -d '{"cid": ""$cid"", "sid": ""$osid"", "rule":"tpm2/firmware" 
#, "parameters":{}}' http://127.0.0.1:8520/verify  | jq -r '.itemid' )"
#echo "Result is: http://127.0.0.1:8520/result/$rid"

#close session
csid="$( curl -s -X DELETE -H "Content-Type: application/json" http://127.0.0.1:8520/session/""$osid""   | jq -r '.itemid' )"
echo "Session closed: http://127.0.0.1:8520/session/$osid"
