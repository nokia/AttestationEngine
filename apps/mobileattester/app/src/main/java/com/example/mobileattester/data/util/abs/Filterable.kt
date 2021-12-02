package com.example.mobileattester.data.util.abs

import android.util.Log
import com.example.mobileattester.ui.util.Timeframe

/**
 * Indicates that a class in filterable by DataFilter.
 * Each class must implement their own logic on how to handle filtering.
 */
interface Filterable {
    fun filter(f: DataFilter): Boolean
    fun filterAny(f: DataFilter): Boolean
}

private const val TAG = "DataFilter"

/**
 * @param searchString A string of all the keywords, separated by spaces.
 * @param timeFrame Filter between a time
 * @param flags Any additional flags, which some classes can use for optional filtering methods
 */
data class DataFilter(
    private val searchString: String,
    val timeFrame: Timeframe? = null,
    val flags: Set<Int>? = null,
) {
    /** All the keywords in lowercase  */
    val keywords: List<String> = searchString.split(' ').map {
        it.lowercase()
    }

    init {
        // For potential time saves...
        if (timeFrame != null && (flags == null || flags.isEmpty())) {
            Log.w(TAG,
                ": A timeframe was provided to DataFilter without flags. " +
                        "Make sure that the element does not need a flag to use the timeframes. " +
                        "If the aforementioned case is true, this message can be ignored.")
        }
    }
}