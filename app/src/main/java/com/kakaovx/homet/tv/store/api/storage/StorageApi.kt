package com.kakaovx.homet.tv.store.api.storage

import com.google.gson.annotations.SerializedName
import com.kakaovx.homet.tv.store.api.ApiPath
import com.skeleton.module.network.NetworkAdapter
import retrofit2.http.GET

interface StorageApi {

    /**
     * Recommend CMD

    @GET(ApiPath.STORAGE_GUIDE)
    suspend  fun getGuideData(
    ): GuideImageData
     */
}

enum class StorageApiType{
    GUIDE_IMAGES
}



class StorageAdapter(getData: ()-> Any? ) : NetworkAdapter<Any>(null, getData) {

    fun withRespondId(id:String): StorageAdapter{
        responseId = id
        return this
    }
}