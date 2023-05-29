package com.example.mobileattester.data.util.abs

import android.util.Log
import com.example.mobileattester.ui.util.Timeframe

private const val TAG = "DataFilter"

/**
 * Indicates that a class in filterable by DataFilter.
 * Each class must implement their own logic on how to handle filtering.
 */
interface Filterable {
    /** Method is expected to return true, if the object matches the provided filter */
    fun filter(f: DataFilter): Boolean
}

/**
 * What type of filtering is used.
 *
 * MATCH_ALL -> Filterable is expected to contain all keywords.
 * MATCH_ANY -> Filterable is expected to match at least one keyword.
 */
enum class MatchType {
    MATCH_ALL, MATCH_ANY
}

/**
 * @param searchString A string of all the keywords, separated by spaces.
 * @param timeFrame Filter between a time
 * @param flags Any additional flags, which some classes can use for optional filtering methods
 */
data class DataFilter(
    private val searchString: String,
    val matchType: MatchType = MatchType.MATCH_ALL,
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
                        "Make sure that the element does not need a flag to use the timeframes. "
                        + "If the aforementioned case is true, this message can be ignored.")
        }
    }
}