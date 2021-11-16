import tpm2_pytss

e = tpm2_pytss.ESAPI("mssim:host=localhost,port=2321")

#
# We already have EK and AK preloaded into the TPM using tpm2_tools
# 0x810100EE is the EK  ( tpm2_createek ... )
# 0x810100AA is the AK  ( tpm2_createak, tpm2_load, tpm2_evictcontrol IIRC )
#

ekhandle = e.tr_from_tpmpublic(0x810100EE)
ekpub, ekname, ekname2 = e.read_public(ekhandle)

akhandle = e.tr_from_tpmpublic(0x810100AA)
akpub, akname, akname2 = e.read_public(akhandle)

# makecredential from tpm2_toolstool
#
# tpm2_makecredential  -u PEMFILE of remote EKpub -s SECRET -n KEYNAME of remote AK
#   returns  CREDENTIAL
#
# tpm2_activatecrednetal -c AK KEY HANDLE/CTX -C EK handle -i CREDENTIAL -o OUTPUT -P SESSION
#
#  if OUTPUT == SECRET then SUCCESS

# 1. Create a secret
thesecret = tpm2_pytss.TPM2B_DIGEST("a secret")

# Ignore this - we can use the handle above
# 2. we need to load the public key into the TPM to get a handle
# temphandle = e.load_external(None,ekpub)

# 3. Now make the credential
cred, secret = e.make_credential(ekhandle, thesecret, akname)

# Ignore this too
# 4. flush the temporary object
# e.flush_context(temphandle)

# 5. Activate Credentual
# cmd = "tpm2_startauthsession --policy-session -S " + sfile.name
#    cmd = "tpm2_policysecret -S " + sfile.name + " -c e"

sym = tpm2_pytss.TPMT_SYM_DEF(
    algorithm=tpm2_pytss.TPM2_ALG.XOR,
    keyBits=tpm2_pytss.TPMU_SYM_KEY_BITS(exclusiveOr=tpm2_pytss.TPM2_ALG.SHA256),
    mode=tpm2_pytss.TPMU_SYM_MODE(aes=tpm2_pytss.TPM2_ALG.CFB),
)

sessionhandle = e.start_auth_session(
    tpm2_pytss.ESYS_TR.NONE,
    tpm2_pytss.ESYS_TR.NONE,
    tpm2_pytss.TPM2_SE.POLICY,
    sym,
    tpm2_pytss.TPM2_ALG.SHA256,
)

expiration = -(10 * 365 * 24 * 60 * 60)

timeout, auth = e.policy_secret(
    tpm2_pytss.ESYS_TR.ENDORSEMENT, sessionhandle, b"", b"", b"", expiration
)


unencrypted_secret = e.activate_credential(akhandle, ekhandle, cred, secret)

# If there is an error before here, then run tpm2_flushcontext -l from the command line

e.flush_context(sessionhandle)

print("unencrypted_secret=", type(unencrypted_secret), unencrypted_secret)


# Ignore these
# q,s = e.quote ( handle, TPML_PCR_SELECTION.parse("sha256:1,2,3,4"), TPM2B_DATA() )
# print("Q=",q)
# print("S=",s)
# print("\n\nGet_Random\n\n")
# print("Random number=",e.get_random(16))

e.close()
