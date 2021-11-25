package com.example.mobileattester.data.model

import com.example.mobileattester.data.util.abs.DataFilter
import com.example.mobileattester.data.util.abs.Filterable
import com.example.mobileattester.ui.util.Timeframe
import com.example.mobileattester.ui.util.Timestamp


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

        const val FILTER_FLAG_WITHIN_TIMEFRAME = CODE_RESULT_ERROR.xor(CODE_RESULT_VERIFY_ERROR) // Magic value, could be whatever
        const val FILTER_FLAG_RESULT_FAIL = CODE_RESULT_ERROR.or(CODE_RESULT_VERIFY_ERROR) // Magic value, could be whatever
    }

    override fun filter(f: DataFilter): Boolean {
        val flags = f.flags ?: listOf()
        val checkTime = flags.contains(FILTER_FLAG_WITHIN_TIMEFRAME)
        val checkResultFail = flags.contains(FILTER_FLAG_WITHIN_TIMEFRAME)

        if (checkTime && f.timeFrame == null) {
            throw Exception("DataFilter was asked to filter by time, but timeframe was not provided.")
        }

        return if (checkTime && checkResultFail)
            inTimeframe(f.timeFrame!!) && isFailed()
        else if(checkResultFail)
            isFailed()
        else if(checkTime)
            inTimeframe(f.timeFrame!!)
        else
            false
    }

    fun isFailed(): Boolean {
        return this.result != CODE_RESULT_OK
    }

    fun inTimeframe(timeframe: Timeframe): Boolean {
        return Timestamp.fromSecondsString(this.verifiedAt)!!
            .isBetween(timeframe.first, timeframe.second)
    }
}
