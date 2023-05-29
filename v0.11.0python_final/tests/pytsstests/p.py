import tpm2_pytss
import inspect

with tpm2_pytss.ESAPI("mssim:host=localhost,port=2321") as t:
	print("T is ",t)

	pcrs = tpm2_pytss.TPML_PCR_SELECTION.parse("sha1:0,1,2")
	s = t.pcr_read(pcrs)

	print(s[1].bytes())	
