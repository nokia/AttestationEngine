#!/bin/sh 

#pi  and tpm2/quote policy
#curl -X GET http://127.0.0.1:8520/element/c7219a2b-7d02-4c1f-bad7-50c4d3b2cd2a
#curl -X GET http://127.0.0.1:8520/policy/6770b5b9-d0ea-4f8b-817f-cab1bf8169c0

#pcrs policy
#curl -X GET http://127.0.0.1:8520/policy/ba64f197-0eb3-4f3f-a74d-a9a8e0a3f17d

#
#open session
#
osid="$( curl -s -X POST -H "Content-Type: application/json" http://127.0.0.1:8520/session   | jq -r '.itemid' )"
echo "Session opened: http://127.0.0.1:8520/session/$osid"

#sysinfo
cidx="$( curl -s -X POST -H "Content-Type: application/json" -d '{"eid":"765e0188-a609-49c0-bf61-3825849413cd","pid":"9a74825f-f780-4eec-9b20-646ba924857a", "sid": "'"$osid"'", "parameters":{}}' http://127.0.0.1:8520/attest  | jq -r '.itemid' )"
echo "Sysinfo Claim is: http://127.0.0.1:8520/claim/$cidx"

# attest PCRs
cidx="$( curl -s -X POST -H "Content-Type: application/json" -d '{"eid":"765e0188-a609-49c0-bf61-3825849413cd","pid":"ba64f197-0eb3-4f3f-a74d-a9a8e0a3f17d", "sid": "'"$osid"'", "parameters":{}}' http://127.0.0.1:8520/attest  | jq -r '.itemid' )"
echo "PCRS Claim is: http://127.0.0.1:8520/claim/$cidx"

#
#attest QUOTE
#
cid="$( curl -s -X POST -H "Content-Type: application/json" -d '{"eid":"765e0188-a609-49c0-bf61-3825849413cd","pid":"6770b5b9-d0ea-4f8b-817f-cab1bf8169c0", "sid": "'"$osid"'", "parameters":{}}' http://127.0.0.1:8520/attest  | jq -r '.itemid' )"
echo "Quote Claim is: http://127.0.0.1:8520/claim/$cid"


# Note: we do not check the HTTP response codes in the following, only the itemid in any JSON output. So no error checking!

#
#verify tpm2 rules
#
rid1="$( curl -s -X POST -H "Content-Type: application/json" -d '{"cid": "'"$cid"'", "sid": "'"$osid"'", "rule":"tpm2_firmware" 
, "parameters":{}}' http://127.0.0.1:8520/verify | jq -r '.itemid' )"
echo "FirmareResult is: http://127.0.0.1:8520/result/$rid1"

rid2="$( curl -s -X POST -H "Content-Type: application/json" -d '{"cid": "'"$cid"'", "sid": "'"$osid"'", "rule":"tpm2_magicNumber" 
, "parameters":{}}' http://127.0.0.1:8520/verify | jq -r '.itemid' )"
echo "Magic Result is: http://127.0.0.1:8520/result/$rid2"

rid3="$( curl -s -X POST -H "Content-Type: application/json" -d '{"cid": "'"$cid"'", "sid": "'"$osid"'", "rule":"tpm2_attestedValue" 
, "parameters":{}}' http://127.0.0.1:8520/verify | jq -r '.itemid' )"
echo "Attested Value Result is: http://127.0.0.1:8520/result/$rid3"

rid4="$( curl -s -X POST -H "Content-Type: application/json" -d '{"cid": "'"$cid"'", "sid": "'"$osid"'", "rule":"tpm2_safe" 
, "parameters":{}}' http://127.0.0.1:8520/verify | jq -r '.itemid' )"
echo "Safe Result is: http://127.0.0.1:8520/result/$rid4"

#
#verify null rules
#
# ridn1="$( curl -s -X POST -H "Content-Type: application/json" -d '{"cid": "'"$cid"'", "sid": "'"$osid"'", "rule":"null_success" 
# , "parameters":{}}' http://127.0.0.1:8520/verify | jq -r '.itemid' )"
# echo "Result is: http://127.0.0.1:8520/result/$ridn1"
# ridn2="$( curl -s -X POST -H "Content-Type: application/json" -d '{"cid": "'"$cid"'", "sid": "'"$osid"'", "rule":"null_fail" 
# , "parameters":{}}' http://127.0.0.1:8520/verify | jq -r '.itemid' )"
# echo "Result is: http://127.0.0.1:8520/result/$ridn2"
# ridn3="$( curl -s -X POST -H "Content-Type: application/json" -d '{"cid": "'"$cid"'", "sid": "'"$osid"'", "rule":"null_verifycallfail" 
# , "parameters":{}}' http://127.0.0.1:8520/verify | jq -r '.itemid' )"
# echo "Result is: http://127.0.0.1:8520/result/$ridn3"
# ridn4="$( curl -s -X POST -H "Content-Type: application/json" -d '{"cid": "'"$cid"'", "sid": "'"$osid"'", "rule":"null_noresult" 
# , "parameters":{}}' http://127.0.0.1:8520/verify | jq -r '.itemid' )"
# echo "Result is: http://127.0.0.1:8520/result/$ridn4"

#
#close session
#
csid="$( curl -s -X DELETE -H "Content-Type: application/json" http://127.0.0.1:8520/session/""$osid""   | jq -r '.itemid' )"
echo "Session closed: http://127.0.0.1:8520/session/$osid"


