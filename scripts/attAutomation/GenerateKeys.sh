#!/bin/bash

#Bash script that creates tpm keys on all devices specified in the elements.txt file

while read -r name device; do
    ./createKeys.py $device
done < elements.txt




