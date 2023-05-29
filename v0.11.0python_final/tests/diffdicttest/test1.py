import requests
from dictdiffer import diff

c1 = requests.get("http://127.0.0.1:8520/v2/claim/5abc5573-f138-4ffc-a489-d681615b2e8b")
c2 = requests.get("http://127.0.0.1:8520/v2/claim/cf87b0e0-11b1-4d40-a173-397e98fa51ea")

try:
   j1 = c1.json()["claim"]["payload"]["payload"]["pcrs"]
except KeyError:
   print("Not a claim with PCRs  - C1")

try:
   j2 = c2.json()["claim"]["payload"]["payload"]["pcrs"]
except KeyError:
   print("Not a claim with PCRs  - C2")


pcrdifference =  list(diff(j1,j2))

print("Difference is ",pcrdifference)

for i in pcrdifference:
	# i is a 3-tuple
	print("Difference is ",i, i[0], i[1], i[2] )