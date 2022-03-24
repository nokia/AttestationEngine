# Attestation Policy Language DSL

Simplest way of running this is to provide two files with polcies and the elements to be evaluated and point at an A10 REST endpoint

Use the example files below, names f.att and f.eva and run like this against a local endpoint (Actually you can skip that because it defaults to 127.0.0.1:8520), pretty prints, no debug staff and outputs to a file report.dict.

```bash
./attall.py examplescripts/a.att examplescripts/a.eva -r http://127.0.0.1:8520 -P -p 0 -o rep1
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
