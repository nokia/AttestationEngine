package com.example.mobileattester.data.util

import com.example.mobileattester.data.model.Rule
import com.example.mobileattester.data.network.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.security.Policy


interface AttestationUtil {
    val policyFlow: MutableStateFlow<Response<List<Policy>>>
    val ruleFlow: MutableStateFlow<Response<List<Rule>>>

    /**
     * Attest an element.
     * If rule is provided, also sends a verification request for the claim.
     *
     * Returns either Status.SUCCESS or Status.ERROR.
     */
    suspend fun attest(eid: String, pid: String, rule: String? = null): Response<NONE>
}

// ---------------------
// ---------------------

class AttestUtil(
    private val dataHandler: AttestationDataHandler,
) : AttestationUtil {

    override val policyFlow: MutableStateFlow<Response<List<Policy>>> =
        MutableStateFlow(Response.loading())

    override val ruleFlow: MutableStateFlow<Response<List<Rule>>> =
        MutableStateFlow(Response.loading())

    override suspend fun attest(eid: String, pid: String, rule: String?): Response<NONE> {
        return when (rule) {
            null -> attestOnly(eid, pid)
            else -> attestVerify(pid, eid, rule)
        }
    }

    // ---- Private ----

    private suspend fun attestOnly(eid: String, pid: String): Response<NONE> {
        try {
            retryIO(
                catchErrors = false
            ) {
                dataHandler.attestElement(eid, pid)
            }
        } catch (e: Exception) {
            return Response.error(message = e.message.toString())
        }

        return Response.success(data = NONE())
    }

    private suspend fun attestVerify(eid: String, pid: String, rule: String): Response<NONE> {
        return Response.error(null, "ERROR: NOT IMPLEMENTED")
    }

}