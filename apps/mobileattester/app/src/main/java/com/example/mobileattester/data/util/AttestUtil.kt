package com.example.mobileattester.data.util

import android.util.Log
import com.example.mobileattester.data.model.Policy
import com.example.mobileattester.data.model.Rule
import com.example.mobileattester.data.network.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
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


    /**
     * Attest an element.
     * If rule is provided, also sends a verification request for the claim.
     */
    fun attest(eid: String, pid: String, rule: String? = null)
}

// ---------------------
// ---------------------
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

    override fun attest(eid: String, pid: String, rule: String?) {
        if (_stat.value == AttestationStatus.LOADING) {
            return
        }
        _stat.value = AttestationStatus.LOADING


        scope.launch {
            try {
                retryIO(
                    catchErrors = false
                ) {
                    when (rule) {
                        null -> attestOnly(eid, pid)
                        else -> attestVerify(pid, eid, rule)
                    }
                    _stat.value = AttestationStatus.SUCCESS
                }
            } catch (e: Exception) {
                Log.d(TAG, "attest: ")
                _stat.value = AttestationStatus.ERROR
            }
        }
    }

    // ---- Private ----

    private suspend fun attestOnly(eid: String, pid: String) {
        val claimId = dataHandler.attestElement(eid, pid)
        println("ATTESTRES: $claimId")
    }

    private suspend fun attestVerify(eid: String, pid: String, rule: String) {

    }
}