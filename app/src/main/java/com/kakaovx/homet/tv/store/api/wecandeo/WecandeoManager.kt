package com.kakaovx.homet.tv.store.api.wecandeo

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.kakaovx.homet.tv.store.api.*
import com.lib.page.PageLifecycleUser
import com.skeleton.module.network.HttpStatusCode
import kotlinx.coroutines.*


class WecandeoManager(
    private val context: Context,
    private val restApi: WecandeoApi
):  PageLifecycleUser {

    val success = MutableLiveData<ApiSuccess<WecandeoApiType>?>()
    val error = MutableLiveData<ApiError<WecandeoApiType>?>()

    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun disposeLifecycleOwner(owner: LifecycleOwner){
        success.removeObservers( owner )
    }

    fun loadPlayData(owner:LifecycleOwner, playData:PlayData ){
        val type = WecandeoApiType.PLAY_DATA
        WecandeoAdapter {
             runBlocking {
                 restApi.signedVideoPath(
                     playData.mediaAccessApiUrl ?: "",
                     playData.mediaAccessApiKey,
                     playData.mediaAccesskey,
                     playData.expire.toString()
                 )
             }
        }
            .withRespondId(playData.mediaAccesskey ?: "")
            .onSuccess(
            { data, id ->
                val signed = data as? MovieSignedData
                signed ?: return@onSuccess  onError( type , HttpStatusCode.DATA)
                if( signed.authVideo == null )onError( type , HttpStatusCode.DATA)
                else {
                    val path = "${playData.playUrl.replace("http://", "https://")}${signed.authVideo.accessKey}"
                    onSuccess(type, path, id)
                }

            },
                { _, code, msg , id-> onError( type , code, msg ,id) },
                { _, code, id -> onError( type , code ,null ,id ) }
        )
    }

    fun clearEvent(){
        error.value = null
        success.value = null
    }

    private fun onSuccess(type:WecandeoApiType , data:Any, respondId:String? = null){
        success.value = ApiSuccess( type, data , respondId)
    }

    private fun onError( type:WecandeoApiType ,code:String, msg:String? = null, respondId:String? = null){
        error.postValue(ApiError( type, code, msg, respondId ))
    }
}