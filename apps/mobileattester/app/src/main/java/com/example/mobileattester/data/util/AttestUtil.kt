package com.example.mobileattester.data.util

import android.util.Log
import com.example.mobileattester.data.model.Claim
import com.example.mobileattester.data.model.ElementResult
import com.example.mobileattester.data.model.Policy
import com.example.mobileattester.data.model.Rule
import com.example.mobileattester.data.network.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


enum class AttestationStatus(msg: String = "") {
    IDLE, LOADING, ERROR, SUCCESS
}

interface AttestationUtil {

    /** Contains the policies */
    val policyFlow: MutableStateFlow<Response<List<Policy>>>

    /** Contains the rules */
    val ruleFlow: MutableStateFlow<Response<List<Rule>>>

    val attestationStatus: MutableStateFlow<AttestationStatus>

    /** Last successfully created claim */
    val claim: MutableStateFlow<Response<Claim>?>

    /** Last result from attestation */
    val result: MutableStateFlow<Response<ElementResult>?>


    /**
     * Attest an element.
     * If rule is provided, also sends a verification request for the received claim.
     */
    fun attest(eid: String, pid: String, rule: String? = null)

    /** Reset status+claim+result, cancel running coroutines */
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
    override val claim: MutableStateFlow<Response<Claim>?> = MutableStateFlow(null)
    override val result: MutableStateFlow<Response<ElementResult>?> = MutableStateFlow(null)

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
                        else -> attestVerify(eid, pid, rule)
                    }
                    setStatus(AttestationStatus.SUCCESS)
                }
            } catch (e: Exception) {
                Log.d(TAG, "attest error: $e")
                setStatus(AttestationStatus.ERROR)
            }
        }
    }

    override fun reset() {
        job.cancelChildren()
        setStatus(AttestationStatus.IDLE)
        claim.value = null
        result.value = null
    }

    // ----------------------- Private ---------------------------
    // ----------------------- Private ---------------------------

    private suspend fun attestOnly(eid: String, pid: String) {
        val claimId = dataHandler.attestElement(eid, pid)

        try {
            val c = dataHandler.getClaim(claimId)
            claim.value = Response.success(c)
        } catch (e: Exception) {
            claim.value = Response.error(message = "Error attesting element $e")
        }
    }

    private suspend fun attestVerify(eid: String, pid: String, rule: String) {
        val claimId = dataHandler.attestElement(eid, pid)
        val resId = dataHandler.verifyClaim(claimId, rule)
        println("Claim verification, Result id: $resId")
        result.value = Response.success(dataHandler.getResult(resId))
    }

    private fun setStatus(status: AttestationStatus) {
        _stat.value = status
    }
}