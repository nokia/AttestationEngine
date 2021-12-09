package com.example.mobileattester.data.util

import android.util.Log
import com.example.mobileattester.data.model.Claim
import com.example.mobileattester.data.model.ElementResult
import com.example.mobileattester.data.model.Policy
import com.example.mobileattester.data.model.Rule
import com.example.mobileattester.data.network.AttestationDataHandler
import com.example.mobileattester.data.network.Response
import com.example.mobileattester.data.network.Status
import com.example.mobileattester.data.network.retryIO
import com.example.mobileattester.data.util.abs.Notifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class AttestationStatus {
    IDLE, LOADING, ERROR, SUCCESS;
}

data class ElementAttested(val eid: String)

/**
 * Functionality to attest elements.
 */
interface AttestationUtil {

    /** Contains the policies */
    val policyFlow: StateFlow<Response<List<Policy>>>

    /** Contains the rules */
    val ruleFlow: MutableStateFlow<Response<List<Rule>>>

    val claimFlow: MutableStateFlow<Response<List<Claim>>>

    val attestationStatus: MutableStateFlow<AttestationStatus>

    /** Last successfully created claim */
    val latestClaim: MutableStateFlow<Response<Claim>?>

    /** Last result from attestation */
    val latestResult: MutableStateFlow<Response<ElementResult>?>

    /**
     * Attest an element.
     * If rule is provided, also sends a verification request for the received claim.
     */
    fun attest(eid: String, pid: String, rule: String? = null)

    /**
     * Reset status, so that a different element can be attested.
     * @param hardReset set to true to clear everything + fetch rules/policies again.
     */
    fun reset(hardReset: Boolean = false)

    fun getPolicyFromCache(policyId: String): Policy?

    fun fetchClaim(claimId: String)
}

// ------------------------------------------
// ------------------------------------------

private const val TAG = "AttestUtil"

class AttestUtil(
    private val notifier: Notifier,
    private val dataHandler: AttestationDataHandler,
    private val policyDataHandler: PolicyDataHandler,
) : AttestationUtil {
    private val job = Job()
    private val scope = CoroutineScope(job)
    private val _stat = MutableStateFlow(AttestationStatus.IDLE)

    override val policyFlow: StateFlow<Response<List<Policy>>> = policyDataHandler.dataFlow

    override val ruleFlow: MutableStateFlow<Response<List<Rule>>> =
        MutableStateFlow(Response.loading())
    override val claimFlow: MutableStateFlow<Response<List<Claim>>> =
        MutableStateFlow(Response.loading())

    override val attestationStatus: MutableStateFlow<AttestationStatus> = _stat
    override val latestClaim: MutableStateFlow<Response<Claim>?> = MutableStateFlow(null)
    override val latestResult: MutableStateFlow<Response<ElementResult>?> = MutableStateFlow(null)

    init {
        fetchRules()
    }

    override fun attest(eid: String, pid: String, rule: String?) {
        if (_stat.value == AttestationStatus.LOADING) {
            return
        }

        setStatus(AttestationStatus.LOADING)

        scope.launch {
            try {
                retryIO {
                    when (rule) {
                        null -> attestOnly(eid, pid)
                        else -> attestVerify(eid, pid, rule)
                    }
                    setStatus(AttestationStatus.SUCCESS)
                    notifier.notifyAll(ElementAttested(eid))
                }
            } catch (e: Exception) {
                Log.d(TAG, "attest error: $e")
                setStatus(AttestationStatus.ERROR)
            }
        }
    }

    override fun reset(hardReset: Boolean) {
        job.cancelChildren()
        setStatus(AttestationStatus.IDLE)

        if (hardReset) {
            latestClaim.value = null
            latestResult.value = null
            fetchRules()
            policyDataHandler.refreshData(true)
        }
    }

    override fun getPolicyFromCache(policyId: String): Policy? {
        return policyFlow.value.data?.find { it.itemid == policyId }
    }

    override fun fetchClaim(claimId: String) {
        val inCache = claimFlow.value.data?.find {
            it.itemid == claimId
        }

        if (inCache != null) {
            return
        }

        scope.launch {
            try {
                val c = dataHandler.getClaim(claimId)
                updateClaimCache(c)
            } catch (e: Exception) {
                // TODO
            }
        }
    }

    // ----------------------- Private ---------------------------
    // ----------------------- Private ---------------------------

    private suspend fun attestOnly(eid: String, pid: String) {
        val claimId = dataHandler.attestElement(eid, pid)

        try {
            val c = dataHandler.getClaim(claimId)
            updateClaimCache(c)
            latestClaim.value = Response.success(c)
        } catch (e: Exception) {
            latestClaim.value = Response.error(message = "Error attesting element $e")
        }
    }

    private suspend fun attestVerify(eid: String, pid: String, rule: String) {
        val claimId = dataHandler.attestElement(eid, pid)
        val resId = dataHandler.verifyClaim(claimId, rule)
        latestResult.value = Response.success(dataHandler.getResult(resId))
    }

    private fun fetchRules() {
        scope.launch {
            try {
                retryIO {
                    ruleFlow.value = Response.success(data = dataHandler.getRules())
                }
                if (ruleFlow.value.status == Status.ERROR) fetchRules()
            } catch (e: Exception) {
                Log.d(TAG, "Error: $e: ")
                ruleFlow.value = Response.error(message = "Could not get rules.")
            }
        }
    }

    private fun setStatus(status: AttestationStatus) {
        _stat.value = status
    }

    private fun updateClaimCache(claim: Claim) {
        val exc = claimFlow.value.data?.toMutableList() ?: mutableListOf()
        exc.add(claim)
        claimFlow.value = Response.success(exc)
    }
}