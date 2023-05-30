# Example Element, Policy and Expected Value

Here we show some examples of an element, a policy and an expected value

## Element

The structure of an element contains a number of compulsory fields: itemid, name, description, protocol, endpoint and tags.

The *itemid* is generated automatically by NAE when the element is added to the database. It should never be changed or duplicated.

The *name* is a common, short name for the element, eg: a hostname; while a *description* is a longer textual description of the element.

The *protocol* field specifies which translation and transport mechanisms should be used. The typical entry here is *A10HTTPRESTv2* when talking to the current Go based trust agent t10.

The *endpoint* is a URL denoting where the element can be found.

Finally *tag* is a freeform list of descriptors that may be shared amongst all elements. For example, denoting the operating system, architecture, roots of trust etc.

The optional fields are uefi, ima, tpm2 and txt. These are shown in full in the example below.

The *uefi* section contains a single subfield *eventlog* specifing where the UEFI Eventlog can be found. Similarly for *ima* and Linux Integrity Management Architecture's ASCII log recorded inside this section under the field name *asciilog*. TXT is again similar with the *txt* section and the *log* field (the example below does not contain a TXT section).

The *tpm2* section contains a *device* (usually /dev/tpm0 or /dev/tpmrm0) and two sections for *ek* and *ak* denoting their expected TPM *handle*, *name* and the publikey in PEM format. The example below does not contain a valid PEM.

```json
{
  "itemid": "ec70dadc-3109-4797-9bf3-11877f371c59",
  "name": "Example",
  "description": "An example machine",
  "protocol": "A10HTTPRESTv2",
  "endpoint": "http://127.0.0.1:8530",
  "tags": [
    "x86",
    "linux",
    "tpm2.0"
  ],
  "uefi": {
    "eventlog": "/sys/kernel/security/tpm0/binary_bios_measurements"
  },
  "ima": {
    "asciilog": "/sys/kernel/security/ima/ascii_runtime_measurements"
  },
  "tpm2": {
    "device": "/dev/tpmrm0",
    "ek": {
      "handle": "0x810100EE",
      "public": "aaa",
      "name": "111"
    },
    "ak": {
      "handle": "0x810100AA",
      "public": "bbb",
      "name": "222"
    }
  }
}
```

## Policy

Similarly to elements, policies contain *itemid*, *name* and *description*. Additionally *intent* and *parameters* are compulsory; the latter may be left as an empty structure.

The *intent* section records the "layer 7" operation which parameters and related and necessary parameters - these parameters can be overridden when making an attestation call.

```json
{
  "itemid": "6770b5b9-d0ea-4f8b-817f-cab1bf8169c0",
  "name": "UEFI CRTM",
  "description": "UEFI CRTM quote for CRTM, UEFI extensions, bootloader and db/dbx key tables",
  "intent": "tpm2/quote",
  "parameters": {
    "pcrSelection": "0,2,4,7",
    "bank": "sha256"
  }
}
```

### tpm2/quote
Requires a *pcrSelection* with a string comma separated PCRs in decimal format, and a *bank* section denoting which bank thsee should be collected from.

### tpm2/pcrread
Contains the intent for a PCR read. Note that parameters are left to an empty structure.

```json
{

  "itemid": "ba64f197-0eb3-4f3f-a74d-a9a8e0a3f17d",
  "name": "PCRs",
  "description": "A policy for requesting all supported PCR banks",
  "intent": "tpm2/pcrs",
  "parameters": {}
}
```

### List of Intents

   * tpm2/quote     - requires pcrSelection and bank
   * tpm2/pcrread
   * uefi/eventlog
   * ima/asciilog
   * sys/info


# Expected Value
Expected values relate elements to policies. Each expected value is unique to an element-policy pair. Some rule require expected values, eg: when reading a tpm2/quote, and each expected value contains a section *evs* with parameters specific to the rule and intent.

For example, this is an expected value for the element and policy described above.

```json
{
  "itemid": "d4b76ce1-2b56-4346-aa11-546ea412d1e5",
  "name": "Laptop firmware",
  "description": "Firmware values, specifically PCR0,2,4,7 for work laptop",
  "elementID": "ec70dadc-3109-4797-9bf3-11877f371c59",
  "policyID": "6770b5b9-d0ea-4f8b-817f-cab1bf8169c0",
  "evs": {
    "attestedValue": "lR+zAH+TVf7g+QTNDVs9J0hD+VcbYNthM1RUQ2nfrsY=",
    "firmwareVersion": "19984793217276456"
  }
}
```