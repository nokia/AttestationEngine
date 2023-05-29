#!/bin/sh

echo "Gets"
curl -X GET http://127.0.0.1:8520/elements
curl -X GET http://127.0.0.1:8520/policies
curl -X GET http://127.0.0.1:8520/expectedValues
#curl -X GET http://127.0.0.1:8520/claims
curl -X GET http://127.0.0.1:8520/results
curl -X GET http://127.0.0.1:8520/hashes
curl -X GET http://127.0.0.1:8520/protocols
curl -X GET http://127.0.0.1:8520/rules

echo "Get specifics"
curl -X GET http://127.0.0.1:8520/element/c7219a2b-7d02-4c1f-bad7-50c4d3b2cd2a
curl -X GET http://127.0.0.1:8520/policy/6770b5b9-d0ea-4f8b-817f-cab1bf8169c0
curl -X GET http://127.0.0.1:8520/protocol/A10HTTPRESTv2

echo "Get stuff that does not exist"
curl -X GET http://127.0.0.1:8520/element/foobar
curl -X GET http://127.0.0.1:8520/policy/barfoo
curl -X GET http://127.0.0.1:8520/protocol/foobar