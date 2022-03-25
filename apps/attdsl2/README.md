# Attestation Policy Language DSL

Simplest way of running this is to provide two files with polcies and the elements to be evaluated and point at an A10 REST endpoint

Use the example files below, names f.att and f.eva and run like this against a local endpoint (Actually you can skip that because it defaults to 127.0.0.1:8520), pretty prints, no debug staff and outputs to a file report.dict.

```bash
./attall.py examplescripts/a.att examplescripts/a.eva -r http://127.0.0.1:8520 -PP -S -p 0 -o rep1
```

## Command Line Options:

```bash
./attall.py --help
usage: attall.py [-h] [-r RESTENDPOINT] [-PP] [-S] [-p PROGRESS] [-o OUTPUTFILE]
                 template evaluation

Attest Elements Command Line Utility

positional arguments:
  template              Location of the template file
  evaluation            Location of the evaluation file

optional arguments:
  -h, --help            show this help message and exit
  -r RESTENDPOINT, --restendpoint RESTENDPOINT
                        Address of an A10REST endpoint
  -PP, --prettyprint    Pretty print the report output
  -S, --summary         Print summary of decisions
  -p PROGRESS, --progress PROGRESS
                        Show progress, 1=none, 2=a little,..., 5=lots
  -o OUTPUTFILE, --outputfile OUTPUTFILE
                        Write the output to the given file
```

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
```

## Example Output

```bash
$ ./attall.py examplescripts/a.att examplescripts/a.eva -S -o report.txt
Element                                  Result   Template
------------------------------------------------------------
cc2029e6-f4d1-4cdf-8172-9659f6b8dfda     False    test2
df3653d6-ad24-40c0-b438-2e8b7b2b05c9     False    test2
fc9c02a2-a166-42f0-aa08-ae0b9bee2a02     False    test2
8843ba90-a4bb-4179-b781-3a2ceedac59f     False    test2
3963611a-6edc-43b8-bd9c-9dd81b296855     True     testx86
------------------------------------------------------------
$ ls -l report.txt
-rw-rw-r-- 1 ian ian 2885 Maw  25 16:43 report.txt

```