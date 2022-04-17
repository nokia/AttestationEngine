# Attestation Policy Language DSL

Simplest way of running this is to provide two files with policies and the elements to be evaluated and point at an A10 REST endpoint

Use the example files below, names a.att and a.eva and run like this against a local endpoint (Actually you can skip that because it defaults to 127.0.0.1:8520), pretty prints, no debug staff and outputs to a file report.dict.

The other option is to use the UI/REST API which takes the IP address of an A10REST endpoint as a parameter. This might be overridden in any call however.

```bash
attall.py examplescripts/a.att examplescripts/a.eva -r http://127.0.0.1:8520 -PP -S -p 0 -o rep1
``` 

```bash
attallrest.py http://127.0.0.1:8520
```

Note, attall assumes python3 is stored at `/usr/bin/python3` if this is not the case the run using `python3 attall.py`


## Command Line Options:

```bash
$ attall.py --help
usage: attall.py [-h] [-r RESTENDPOINT] [-pp] [-s] [-e] [-D DEBUG] [-o OUTPUTFILE] template evaluation

Attest Elements Command Line Utility

positional arguments:
  template              Location of the template file
  evaluation            Location of the evaluation file

optional arguments:
  -h, --help            show this help message and exit
  -r RESTENDPOINT, --restendpoint RESTENDPOINT
                        Address of an A10REST endpoint
  -pp, --prettyprint    Pretty print the report output
  -s, --summary         Print summary of decisions
  -e, --errors          Print a list of any errors recorded
  -D DEBUG, --debug DEBUG
                        Show debug output, 1=none, 2=a little,..., 5=lots
  -o OUTPUTFILE, --outputfile OUTPUTFILE
                        Write the output to the given file
```


## UI

The attall UI is found on port 8542 by default

## Example ATT file

```
template testx86
 attest
   PCRread,{}
   SystemInfo, {}
   SRTM-SHA1,{}   
      [[
         q1_1 <- tpm2rules/TPM2FirmwareVersion, {}
         q1_2 <- tpm2rules/TPM2QuoteAttestedValue, {}
         q1_3 <- tpm2rules/TPM2QuoteStandardVerify, {}         
      ]]
   SRTM-SHA256,{}  
      [[
         q256_1 <- tpm2rules/TPM2FirmwareVersion, {}
         q256_2 <- tpm2rules/TPM2QuoteAttestedValue, {}
         q256_3 <- tpm2rules/TPM2QuoteStandardVerify, {}         
      ]]
   CheckCredentials,{},copycredentials
      [[
         id <-  tpm2rules/TPM2CredentialVerify  ,{}
      ]]
  decision
    ( q1_1 ^ q1_2 ^ q1_3 ) ^
    ( q256_1 ^ q256_2 ^ q256_3 ) ^
     id
```

## Example EVA file

```
evaluate name=ubuntu using testx86 , logic = strict
evaluate name=ubuntu using testx86 , logic = flexible
evaluate type=[ pi, arm ] using testpi, logic = loose
```

## Example Output

```bash
$python3 ../AttestationEngine/apps/attdsl2/attall.py ./a.att ./q.eva  -s -e -o report.json

**** Summary *****
50 items,  6 errors 19 decisions
Element                               Result   Logic      Template
------------------------------------------------------------------------------
3963611a-6edc-43b8-bd9c-9dd81b296855  True     strict     quickx86
cc2029e6-f4d1-4cdf-8172-9659f6b8dfda  True     strict     testpi
d27e149c-15b8-49a4-9934-7fa33bc6eafb  True     strict     testpi
abc25c1c-185c-4ea7-a9af-acc99684d4e0  True     strict     testpi
04b3a481-ec8a-43a9-b262-f84871fc8ccb  False    strict     testpi
e5a13808-16b0-47a4-910f-9892899d4bd6  False    strict     testpi
d27e149c-15b8-49a4-9934-7fa33bc6eafb  True     strict     testpi
cc2029e6-f4d1-4cdf-8172-9659f6b8dfda  True     strict     testpi
abc25c1c-185c-4ea7-a9af-acc99684d4e0  True     strict     testpi
04b3a481-ec8a-43a9-b262-f84871fc8ccb  True     loose      testpi
e5a13808-16b0-47a4-910f-9892899d4bd6  True     loose      testpi
d27e149c-15b8-49a4-9934-7fa33bc6eafb  True     loose      testpi
cc2029e6-f4d1-4cdf-8172-9659f6b8dfda  True     loose      testpi
abc25c1c-185c-4ea7-a9af-acc99684d4e0  True     loose      testpi
04b3a481-ec8a-43a9-b262-f84871fc8ccb  False    flexible   testpi
e5a13808-16b0-47a4-910f-9892899d4bd6  False    flexible   testpi
d27e149c-15b8-49a4-9934-7fa33bc6eafb  True     flexible   testpi
cc2029e6-f4d1-4cdf-8172-9659f6b8dfda  True     flexible   testpi
abc25c1c-185c-4ea7-a9af-acc99684d4e0  True     flexible   testpi
------------------------------------------------------------------------------

**** Errors *****
50 items,  6 errors 19 decisions
==============================================================================
{'eid': '04b3a481-ec8a-43a9-b262-f84871fc8ccb', 'msg': {'msg': {'msg': 'failed to communicate with remote TPM. Also EK/AK could be invalid.'}}}
------------------------------------------------------------------------------
{'eid': 'e5a13808-16b0-47a4-910f-9892899d4bd6', 'msg': {'msg': {'msg': 'failed to communicate with remote TPM. Also EK/AK could be invalid.'}}}
------------------------------------------------------------------------------
{'eid': '04b3a481-ec8a-43a9-b262-f84871fc8ccb', 'msg': {'msg': {'msg': 'failed to communicate with remote TPM. Also EK/AK could be invalid.'}}}
------------------------------------------------------------------------------
{'eid': 'e5a13808-16b0-47a4-910f-9892899d4bd6', 'msg': {'msg': {'msg': 'failed to communicate with remote TPM. Also EK/AK could be invalid.'}}}
------------------------------------------------------------------------------
{'eid': '04b3a481-ec8a-43a9-b262-f84871fc8ccb', 'msg': {'msg': {'msg': 'failed to communicate with remote TPM. Also EK/AK could be invalid.'}}}
------------------------------------------------------------------------------
{'eid': 'e5a13808-16b0-47a4-910f-9892899d4bd6', 'msg': {'msg': {'msg': 'failed to communicate with remote TPM. Also EK/AK could be invalid.'}}}
------------------------------------------------------------------------------
==============================================================================

```

From the above the file `report.json` will contain the report in JSON format.

# Grammar
The default language grammar is defined in the file `attgrammar.py`. The files `*.lark` are deprecated, but useful as SublimeText 4 has a Lark syntax highlighter.