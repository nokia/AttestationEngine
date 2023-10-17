#!/bin/bash

#Bash script to attest and verify all elements in the elements.txt file according to rules/
#and policies given by the policy1, policy2, rule1 and rule2 variables.
#The server variable gives the address of the attestation server
#given as the ip address and port number.

server="194.157.71.11:8520"
policy1="Pi Fakeboot CRTM"
policy2="Pi Fakeboot SRTM"
rule1="tpm2_attestedValue"
rule2="tpm2_firmware"

session=$(./createSession.py $server -m "bash script test")

while read -r name device; do
    claim1=$(./attest.py -e $name -p "$policy1" -s $session -sE $server)
    claim2=$(./attest.py -e $name -p "$policy2" -s $session -sE $server)
    r1=$(./verify.py -r "$rule1" -s $session -c $claim1 -sE $server)
    r2=$(./verify.py -r "$rule2" -s $session -c $claim1 -sE $server)
    r3=$(./verify.py -r "$rule1" -s $session -c $claim2 -sE $server)
    r4=$(./verify.py -r "$rule2" -s $session -c $claim2 -sE $server)
    if [ $r1 -eq 0 ] && [ $r2 -eq 0 ] && [ $r3 -eq 0 ] && [ $r4 -eq 0 ]
then
    echo "$name device is trusted"
else
    echo "$name device is not trusted"
fi

done < elements.txt

./closeSession.py $server -s $session


