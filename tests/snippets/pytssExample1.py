from tpm2_pytss import *


e = ESAPI("mssim:host=localhost,port=2321")
e.get_random(16)

handle = e.tr_from_tpmpublic(0x810100AA)
e.read_public(handle)


q, s = e.quote(
    ESYS_TR(0x810100AA), TPML_PCR_SELECTION.parse("sha256:1,2,3,4"), TPM2B_DATA()
)


q, s = e.quote(handle, TPML_PCR_SELECTION.parse("sha256:1,2,3,4"), TPM2B_DATA())


e.close()
