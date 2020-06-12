package com.kakaovx.homet.tv.store.api.account

import com.kakaovx.homet.tv.store.api.ApiField
import com.kakaovx.homet.tv.store.api.ApiPath
import com.kakaovx.homet.tv.store.api.HomeTResponse
import retrofit2.http.*

enum class AccountApiType{
    JWT, JWT_REFRESH
}


interface AccountApi{

    @FormUrlEncoded
    @POST(ApiPath.ACCOUNT_API_JWT)
    suspend fun getJWT(
        @Field(ApiField.DEVICE_KEY) deviceKey: String?
    ): HomeTResponse<JWTData?>?

    @FormUrlEncoded
    @POST(ApiPath.ACCOUNT_API_JWT_REFRESH)
    suspend fun getJWTRefresh(
        @Field(ApiField.DEVICE_KEY) deviceKey: String?,
        @Field(ApiField.PRE_JWT) preJWt: String?
    ): HomeTResponse<JWTData?>?
}