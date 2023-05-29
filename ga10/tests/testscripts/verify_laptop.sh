#!/bin/sh 

#work laptop and tpm2/quote policy
echo Element details...
curl -X GET http://127.0.0.1:8520/element/c7219a2b-7d02-4c1f-bad7-50c4d3b2cd2a

echo Example policy...
curl -X GET http://127.0.0.1:8520/policy/6770b5b9-d0ea-4f8b-817f-cab1bf8169c0

#
#open session
#
osid="$( curl -s -X POST -H "Content-Type: application/json" http://127.0.0.1:8520/session   | jq -r '.itemid' )"
echo "Session opened: http://127.0.0.1:8520/session/$osid"


#
#attest SYS
#
cidx="$( curl -s -X POST -H "Content-Type: application/json" -d '{"eid":"c7219a2b-7d02-4c1f-bad7-50c4d3b2cd2a","pid":"9a74825f-f780-4eec-9b20-646ba924857a", "sid": "'"$osid"'", "parameters":{}}' http://127.0.0.1:8520/attest  | jq -r '.itemid' )"
echo "SYS Claim is: http://127.0.0.1:8520/claim/$cidx"

#
#attest UEFI
#
cidx="$( curl -s -X POST -H "Content-Type: application/json" -d '{"eid":"c7219a2b-7d02-4c1f-bad7-50c4d3b2cd2a","pid":"e7c5d901-3caa-4542-8660-7054560198e1", "sid": "'"$osid"'", "parameters":{}}' http://127.0.0.1:8520/attest  | jq -r '.itemid' )"
echo "UEFI claim is: http://127.0.0.1:8520/claim/$cidx"

#
#attest IMA
#
cidx="$( curl -s -X POST -H "Content-Type: application/json" -d '{"eid":"c7219a2b-7d02-4c1f-bad7-50c4d3b2cd2a","pid":"f147fbd9-af7e-46f6-a8f8-3b0734918743", "sid": "'"$osid"'", "parameters":{}}' http://127.0.0.1:8520/attest  | jq -r '.itemid' )"
echo "IMA claim is: http://127.0.0.1:8520/claim/$cidx"

#
#attest PCRs
#
cidx="$( curl -s -X POST -H "Content-Type: application/json" -d '{"eid":"c7219a2b-7d02-4c1f-bad7-50c4d3b2cd2a","pid":"ba64f197-0eb3-4f3f-a74d-a9a8e0a3f17d", "sid": "'"$osid"'", "parameters":{}}' http://127.0.0.1:8520/attest  | jq -r '.itemid' )"
echo "PCRs claim is: http://127.0.0.1:8520/claim/$cidx"

#
#attest QUOTE
#
cid="$( curl -s -X POST -H "Content-Type: application/json" -d '{"eid":"c7219a2b-7d02-4c1f-bad7-50c4d3b2cd2a","pid":"6770b5b9-d0ea-4f8b-817f-cab1bf8169c0", "sid": "'"$osid"'", "parameters":{}}' http://127.0.0.1:8520/attest  | jq -r '.itemid' )"
echo "QUOTEClaim is: http://127.0.0.1:8520/claim/$cid"


# Note: we do not check the HTTP response codes in the following, only the itemid in any JSON output. So no error checking!

#
#verify tpm2 rules
#
rid1="$( curl -s -X POST -H "Content-Type: application/json" -d '{"cid": "'"$cid"'", "sid": "'"$osid"'", "rule":"tpm2_firmware" 
, "parameters":{}}' http://127.0.0.1:8520/verify | jq -r '.itemid' )"
echo "Result is: http://127.0.0.1:8520/result/$rid1"

rid2="$( curl -s -X POST -H "Content-Type: application/json" -d '{"cid": "'"$cid"'", "sid": "'"$osid"'", "rule":"tpm2_magicNumber" 
, "parameters":{}}' http://127.0.0.1:8520/verify | jq -r '.itemid' )"
echo "Result is: http://127.0.0.1:8520/result/$rid2"

rid3="$( curl -s -X POST -H "Content-Type: application/json" -d '{"cid": "'"$cid"'", "sid": "'"$osid"'", "rule":"tpm2_attestedValue" 
, "parameters":{}}' http://127.0.0.1:8520/verify | jq -r '.itemid' )"
echo "Result is: http://127.0.0.1:8520/result/$rid3"

rid4="$( curl -s -X POST -H "Content-Type: application/json" -d '{"cid": "'"$cid"'", "sid": "'"$osid"'", "rule":"tpm2_safe" 
, "parameters":{}}' http://127.0.0.1:8520/verify | jq -r '.itemid' )"
echo "Result is: http://127.0.0.1:8520/result/$rid4"

#
#verify null rules - these don't do much, just for completeness
#
ridn1="$( curl -s -X POST -H "Content-Type: application/json" -d '{"cid": "'"$cid"'", "sid": "'"$osid"'", "rule":"null_success" 
 , "parameters":{}}' http://127.0.0.1:8520/verify | jq -r '.itemid' )"
echo "Result is: http://127.0.0.1:8520/result/$ridn1"
ridn2="$( curl -s -X POST -H "Content-Type: application/json" -d '{"cid": "'"$cid"'", "sid": "'"$osid"'", "rule":"null_fail" 
 , "parameters":{}}' http://127.0.0.1:8520/verify | jq -r '.itemid' )"
echo "Result is: http://127.0.0.1:8520/result/$ridn2"
ridn3="$( curl -s -X POST -H "Content-Type: application/json" -d '{"cid": "'"$cid"'", "sid": "'"$osid"'", "rule":"null_verifycallfail" 
 , "parameters":{}}' http://127.0.0.1:8520/verify | jq -r '.itemid' )"
echo "Result is: http://127.0.0.1:8520/result/$ridn3"
ridn4="$( curl -s -X POST -H "Content-Type: application/json" -d '{"cid": "'"$cid"'", "sid": "'"$osid"'", "rule":"null_noresult" 
 , "parameters":{}}' http://127.0.0.1:8520/verify | jq -r '.itemid' )"
echo "Result is: http://127.0.0.1:8520/result/$ridn4"

#
#close session
#
csid="$( curl -s -X DELETE -H "Content-Type: application/json" http://127.0.0.1:8520/session/""$osid""   | jq -r '.itemid' )"
echo "Session closed: http://127.0.0.1:8520/session/$osid"


