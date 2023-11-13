#!/bin/sh

ELEMENT=765e0188-a609-49c0-bf61-3825849413cd
POLICY=ba64f197-0eb3-4f3f-a74d-a9a8e0a3f17d

EN="$(curl -s -X GET http://127.0.0.1:8520/element/$ELEMENT | jq -r .name)"
PN="$(curl -s -X GET http://127.0.0.1:8520/policy/$POLICY | jq -r .name)"


echo Applying $PN to $EN


#open session
SESSION="$(curl -s -X POST http://127.0.0.1:8520/session -H "Content-Type: application/json" --data '{"message":"test from curl"}' | jq -r .itemid)"

#attest element
CLAIMID="$(curl -s -X POST http://127.0.0.1:8520/attest -H  "Content-Type: application/json" --data '{"eid":"'$ELEMENT'","pid":"'$POLICY'","sid":"'$SESSION'"}' | jq -r .itemid)"

#close session
curl -s -X DELETE http://127.0.0.1:8520/session/$SESSION > /dev/null

#Print stuff
echo Session was $SESSION with claim $CLAIMID
curl -s -X GET http://127.0.0.1:8520/claim/$CLAIMID | jq .body.sha256