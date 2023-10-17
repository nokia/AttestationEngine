#!/bin/bash

#Bash script that adds all the elements specified in the elements.txt file
#to the attestation server. An element and expected values are created.
#Policy 1 and policy 2 variables represent the policies and rule 1, rule 2 the 
#rules by which the expected values are created.

server="194.157.71.11:8520"
policy1="Pi Fakeboot CRTM"
policy2="Pi Fakeboot SRTM"
rule1="tpm2_attestedValue"
rule2="tpm2_firmware"
params='{"Test":"test2"}'

session=$(./createSession.py $server -m "bash script test")
echo $session

while read -r name device; do
    ./createElement.py $device -d "$name element" -n $name -t "letstrust" -sE $server
    claim1=$(./attest.py -e $name -p "$policy1" -s $session -sE $server)
    claim2=$(./attest.py -e $name -p "$policy2" -s $session -sE $server)
    ./createExpected.py -n "expected value for $name" -d "expected value for policy $policy1" -e "$name" -p "$policy1" -s $session -c $claim1 -sE $server
    ./createExpected.py -n "expected value for $name" -d "expected value for policy $policy2" -e "$name" -p "$policy2" -s $session -c $claim2 -sE $server
done < elements.txt


./closeSession.py $server -s $session
