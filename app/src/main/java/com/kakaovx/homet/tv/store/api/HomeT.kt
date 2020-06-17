package com.kakaovx.homet.tv.store.api

import com.google.gson.annotations.SerializedName
import com.skeleton.module.network.NetworkAdapter
import com.skeleton.module.network.NetworkFlow
import okhttp3.Interceptor
import java.io.IOException


data class HomeTResponse<T> (
    @SerializedName("status") val status: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("msg") val message: String,
    @SerializedName("data") val data: T
)


class HomeTInterceptor : Interceptor {
    var jwtToken: String = ""

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val original = chain.request()
        val request = original.newBuilder()
            .header("User-Agent", "Android")
            .header("Authorization", "bearer $jwtToken")
            .build()
        return chain.proceed(request)
    }
}

class HomeTAdapter(getData: ()->HomeTResponse<*>? ) : NetworkAdapter<HomeTResponse<*>>(null, getData) {

    fun withRespondId(id:String): HomeTAdapter{
        responseId = id
        return this
    }

    override fun onReceive(response: HomeTResponse<*>?) {
        response ?: return super.onReceive(response)
        when (response.code) {
            ApiCode.SUCCESS -> super.onReceive(response)
            else -> onApiError(response.code,response.message)
        }
    }
}

class HomeTFlow(flow: Array<HomeTResponse<out Any?>?>) : NetworkFlow<HomeTResponse<*>>(null, flow) {

    fun withRespondId(id:String): HomeTFlow{
        responseId = id
        return this
    }

    override fun onReceive(response: HomeTResponse<*>?) {
        response ?: return super.onReceive(response)
        when (response.code) {
            ApiCode.SUCCESS -> super.onReceive(response)
            else -> onApiError(response.code,response.message)
        }
    }
}


