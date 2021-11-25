package com.example.mobileattester.data.model

import com.example.mobileattester.data.util.abs.DataFilter
import com.example.mobileattester.data.util.abs.Filterable
import com.example.mobileattester.ui.util.Timeframe
import com.example.mobileattester.ui.util.Timestamp
import java.lang.Exception


data class ElementResult(
    // missing "additional: []"
    val claimID: String,
    val elementID: String,
    val ev: ExpectedValueElementResult,
    val itemid: String,
    val message: String,
    val policyID: String,
    val result: Int,
    val ruleName: String,
    val verifiedAt: String,
) : Filterable {

    companion object {
        const val CODE_RESULT_OK = 0
        const val CODE_RESULT_ERROR = 9001
        const val CODE_RESULT_VERIFY_ERROR = 9002

        const val FILTER_FLAG_WITHIN_TIMEFRAME = 4356126
        const val FILTER_FLAG_ONLY_RESULT_OK = 1531325
    }

    override fun filter(f: DataFilter): Boolean {
        val flags = f.flags ?: listOf()
        val onlyOk = flags.contains(FILTER_FLAG_ONLY_RESULT_OK)
        val checkTime = flags.contains(FILTER_FLAG_WITHIN_TIMEFRAME)

        if (checkTime && f.timeFrame == null) {
            throw Exception("DataFilter was asked to filter by time, but timeframe was not provided.")
        }

        // If filtering for only passed result
        if (onlyOk) return handleOkResults(f, checkTime)

        // Other cases
        return handleCheckTimeResult(f, checkTime)
    }

    private fun handleOkResults(f: DataFilter, checkTime: Boolean): Boolean {
        return when (checkTime) {
            true -> this.result == CODE_RESULT_OK && inTimeframe(f.timeFrame!!)
            false -> this.result == CODE_RESULT_OK
        }
    }

    private fun handleCheckTimeResult(f: DataFilter, checkTime: Boolean): Boolean {
        return when (checkTime) {
            true -> inTimeframe(f.timeFrame!!)
            false -> true // Not using time filter
        }
    }

    private fun inTimeframe(timeframe: Timeframe): Boolean {
        return Timestamp.fromSecondsString(this.verifiedAt)!!
            .isBetween(timeframe.first, timeframe.second)
    }
}
