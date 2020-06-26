package com.kakaovx.homet.tv.store.api.wecandeo

import com.google.gson.annotations.SerializedName
import com.kakaovx.homet.tv.store.api.ApiField
import com.skeleton.module.network.NetworkAdapter
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface WecandeoApi {
    @GET
    suspend fun signedVideoPath(
        @Url path: String,
        @Query(ApiField.KEY) key: String?,
        @Query(ApiField.ACCESS_KEY) access_key: String?,
        @Query(ApiField.EXPIRE) expire:String?
    ): MovieSignedData
}

data class MovieSignedData(@SerializedName("authVideo") val authVideo: MovieAccessData?)
data class MovieAccessData(@SerializedName("accessKey") val accessKey: String?)

enum class WecandeoApiType{
    PLAY_DATA
}


data class PlayData (val playUrl:String){
    var mediaAccesskey:String? = null
    var mediaAccessApiUrl:String? = null
    var mediaAccessApiKey:String? = null
    var expire:Int = 10
}


class WecandeoAdapter(getData: ()-> Any? ) : NetworkAdapter<Any>(null, getData) {

    fun withRespondId(id:String): WecandeoAdapter{
        responseId = id
        return this
    }
}