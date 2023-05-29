package com.example.mobileattester.ui.util

import com.example.mobileattester.data.util.abs.DataFilter
import com.example.mobileattester.data.util.abs.MatchType

object FilterBuilder {

    fun buildWithBaseFilter(
        baseFilters: String?,
        baseType: MatchType,
        additional: String,
        additionalType: MatchType,
    ): List<DataFilter> {
        return mutableListOf<DataFilter>().apply {
            baseFilters?.let {
                val d = DataFilter(it, baseType)
                this.add(d)
            }
            add(DataFilter(additional, additionalType))
        }
    }
}