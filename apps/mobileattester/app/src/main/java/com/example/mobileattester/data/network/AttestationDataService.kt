package com.example.mobileattester.data.network

import com.example.mobileattester.data.model.*
import retrofit2.http.*

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

    @PUT("element")
    suspend fun updateElement(@Body element: Element): String

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
        @Path("pid") policyId: String,
    ): ExpectedValue

    /**
    Claims
     */

    @GET("claim/{itemid}")
    suspend fun getClaim(@Path("itemid") itemid: String): com.example.mobileattester.data.model.Claim

    /**
    Results
     */

    @GET("result/{itemid}")
    suspend fun getResult(@Path("itemid") itemid: String): ElementResult

    @GET("results/latest")
    suspend fun getLatestResults(
        @Query("timestamp") limit: Float?,
    ): List<ElementResult>

    @GET("results/element/latest/{itemid}")
    suspend fun getElementResults(
        @Path("itemid") itemid: String,
        @Query("limit") limit: Int,
    ): List<ElementResult>

    /**
    Attestation
     */

    // Returns the id of the claim which was produced if successful
    @POST("attest")
    suspend fun attestElement(@Body body: AttestationParams): String

    @POST("verify")
    suspend fun verifyClaim(@Body body: VerifyParams): String

    /**
    Rules
     */

    @GET("rules")
    suspend fun getRules(): List<Rule>
}
