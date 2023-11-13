Make sure to change the permissions for files, GenerateKeys.sh, CreateElements.sh and VerifyElements.sh
Do this with command 'sudo chmod u+x <name of file>'.

Start by adding all elements that you wish to add to the attestation server
to file elements.txt. The format for each device is the following; <name of device> <user>@<ipaddress>
IMPORTANT: Make sure that the device name has no spaces. Leave one empty line at the end of the file.

To create tpm keys on each of the devices run the GenerateKeys.sh script
Note that you may be prompted to add the password for each device.
This will only happen once since the ssh keys are saved.

To create elements on the attestation server run the bash script CreateElements.sh.
It will add the elements in elements.txt to the attestation server and create expected values
according to the policies specified in the bash script.

To verify devices that already exist on the attestation server, run the VerifyElements.sh
bash script. It will verify all devices specified in the elements.txt file, according to 
the rules and policies specified in the bash script.

