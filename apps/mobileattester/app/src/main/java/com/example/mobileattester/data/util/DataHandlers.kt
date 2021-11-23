package com.example.mobileattester.data.util

import com.example.mobileattester.data.model.Element
import com.example.mobileattester.data.model.Policy
import com.example.mobileattester.data.util.abs.BatchedDataHandler
import com.example.mobileattester.data.util.abs.FetchIdData
import com.example.mobileattester.data.util.abs.FetchIdList

// ------------------------------------------------------------------------------------------
// ------------ Concrete classes to handle different types of data in batches ---------------
// ------------------------------------------------------------------------------------------

class ElementDataHandler(
    batchSize: Int,
    fetchIdList: FetchIdList<String>,
    fetchDataForId: FetchIdData<String, Element>,
) : BatchedDataHandler<String, Element>(batchSize, fetchIdList, fetchDataForId) {

    override fun <T> notify(data: T) {
        if (data is Element) {
            this.refreshSingleValue(data.itemid)
        }
    }
}

class PolicyDataHandler(
    batchSize: Int,
    fetchIdList: FetchIdList<String>,
    fetchDataForId: FetchIdData<String, Policy>,
) : BatchedDataHandler<String, Policy>(batchSize, fetchIdList, fetchDataForId) {

    override fun <T> notify(data: T) {
        if (data is Policy) {
            this.refreshSingleValue(data.itemid)
        }
    }
}