package com.example.mobileattester.data.util

import android.util.Log
import com.example.mobileattester.data.model.Claim
import com.example.mobileattester.data.model.ElementResult
import com.example.mobileattester.data.model.Policy
import com.example.mobileattester.data.model.Rule
import com.example.mobileattester.data.network.*
import com.example.mobileattester.ui.pages.AttestationType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


enum class AttestationStatus(val msg: String = "") {
    IDLE, LOADING, ERROR, SUCCESS
}

interface AttestationUtil {

    /** Contains the policies */
    val policyFlow: MutableStateFlow<Response<List<Policy>>>

    /** Contains the rules */
    val ruleFlow: MutableStateFlow<Response<List<Rule>>>

    val attestationStatus: MutableStateFlow<AttestationStatus>

    val attestationResult: MutableStateFlow<Response<>>

    /**
     * Attest an element.
     * If rule is provided, also sends a verification request for the claim.
     */
    fun attest(eid: String, pid: String, rule: String? = null)

    // Reset the attestation state
    fun reset()
}

// ------------------------------------------
// ------------------------------------------

private const val TAG = "AttestUtil"

class AttestUtil(
    private val dataHandler: AttestationDataHandler,
    private val policyDataHandler: PolicyDataHandler,
) : AttestationUtil {
    private val job = Job()
    private val scope = CoroutineScope(job)
    private val _stat = MutableStateFlow(AttestationStatus.IDLE)

    override val policyFlow: MutableStateFlow<Response<List<Policy>>> =
        policyDataHandler.dataFlow

    override val ruleFlow: MutableStateFlow<Response<List<Rule>>> =
        MutableStateFlow(Response.loading())

    override val attestationStatus: MutableStateFlow<AttestationStatus> = _stat
    override val attestationResult: MutableStateFlow<AttestationResponse<Any>> =
        MutableStateFlow(AttestationResponse.Empty())

    init {
        scope.launch {
            retryIO {
                ruleFlow.value = Response.success(data = dataHandler.getRules())
            }
        }
    }

    override fun attest(eid: String, pid: String, rule: String?) {
        if (_stat.value == AttestationStatus.LOADING) {
            return
        }

        setStatus(AttestationStatus.LOADING)

        scope.launch {
            try {
                retryIO(
                    catchErrors = false
                ) {
                    when (rule) {
                        null -> attestOnly(eid, pid)
                        else -> attestVerify(pid, eid, rule)
                    }
                    setStatus(AttestationStatus.SUCCESS)
                }
            } catch (e: Exception) {
                Log.d(TAG, "attest: ")
                setStatus(AttestationStatus.ERROR)
            }
        }
    }

    override fun reset() {
        setStatus(AttestationStatus.IDLE)
    }

    // ---- Private ----

    private suspend fun attestOnly(eid: String, pid: String) {
        val claimId = dataHandler.attestElement(eid, pid)
        println("Attest only response: $claimId")

        try {
            val claim = dataHandler.getClaim(claimId)
            attestationResult.value = Response.success(claim)
        } catch (e: Exception) {
            attestationResult.value = Response.error(message = "Error getting claim data $e")
        }
    }

    private suspend fun attestVerify(eid: String, pid: String, rule: String) {
        val claimId = dataHandler.attestElement(eid, pid)

        try {
            dataHandler.verifyClaim(claimId, rule)
        } catch (e: Exception) {

        }
    }

    private fun setStatus(status: AttestationStatus) {
        _stat.value = status
    }
}