package com.example.mobileattester.data.network

import com.example.mobileattester.data.model.Element
import com.example.mobileattester.data.model.ElementResult
import com.example.mobileattester.data.model.ExpectedValue
import com.example.mobileattester.data.model.Policy
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Communication with REST
 */
interface AttestationDataService {

    /**
    Elements
     */

    @GET("elements")
    suspend fun getElementIds(): List<String>

    @GET("element/{itemid}")
    suspend fun getElement(@Path("itemid") itemid: String): Element

    // Return all types currently in use
    @GET("elements/types")
    suspend fun getAllTypes(): List<String>

    /**
    Policies
     */

    @GET("policies")
    suspend fun getPolicyIds(): List<String>

    @GET("policy/{itemid}")
    suspend fun getPolicy(@Path("itemid") itemid: String): Policy

    /**
    Expected values
     */

    @GET("expectedvalue/{itemid}")
    suspend fun getExpectedValue(@Path("itemid") itemid: String): ExpectedValue

    @GET("expectedvalue/{eid}/{pid}")
    suspend fun getExpectedValueByElementPolicyIds(
        @Path("eid") elementId: String,
        @Path("pid") policyId: String
    ): ExpectedValue

    /**
    Results
     */

    @GET("result/element/latest/{itemid}/{limit}")
    suspend fun getElementResults(
        @Path("itemid") itemid: String,
        @Path("limit") limit: String
    ): List<ElementResult>


    /**
    Attestation
     */

    @POST("attest/")
    suspend fun attestElement(@Body eid: String, @Body pid: String)

    @POST("verify/")
    suspend fun verifyClaim(@Body cid: String, @Body rul: String)
}
