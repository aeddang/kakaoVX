package com.kakaovx.homet.tv.store.api.storage

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.kakaovx.homet.tv.store.api.*
import com.lib.page.PageLifecycleUser
import com.skeleton.module.network.HttpStatusCode
import kotlinx.coroutines.*


class StorageManager(
    private val context: Context,
    private val restApi: StorageApi
):  PageLifecycleUser {

    val success = MutableLiveData<ApiSuccess<StorageApiType>?>()
    val error = MutableLiveData<ApiError<StorageApiType>?>()

    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun disposeLifecycleOwner(owner: LifecycleOwner){
        success.removeObservers( owner )
    }
    /*
    fun loadGuideImages(owner:LifecycleOwner ){
        val type = StorageApiType.GUIDE_IMAGES
        StorageAdapter {
             runBlocking {
                 restApi.getGuideData()
             }
        }
            .onSuccess(
            { data, id ->
                val images = data as? GuideImageData
                images ?: return@onSuccess  onError( type , HttpStatusCode.DATA)
                onSuccess(type, images , id)
            },
                { _, code, msg , id-> onError( type , code, msg ,id) },
                { _, code, id -> onError( type , code ,null ,id ) }
        )
    }
    */
    fun clearEvent(){
        error.value = null
        success.value = null
    }

    private fun onSuccess(type:StorageApiType , data:Any, respondId:String? = null){
        success.value = ApiSuccess( type, data , respondId)
    }

    private fun onError( type:StorageApiType ,code:String, msg:String? = null, respondId:String? = null){
        error.postValue(ApiError( type, code, msg, respondId ))
    }
}