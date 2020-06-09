package com.kakaovx.homet.tv.store.api

import com.google.gson.annotations.SerializedName
import com.lib.util.Log
import com.skeleton.module.network.ErrorType
import com.skeleton.module.network.HttpStatusCode
//import com.skeleton.module.network.NetworkAdapter
import kotlinx.coroutines.*
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


data class HomeTResponse<T> (
    @SerializedName("status") val status: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("msg") val message: String,
    @SerializedName("data") val data: T
)


class HomeTInterceptor : Interceptor {
    var jwtToken: String = ""

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val request = original.newBuilder()
            .header("User-Agent", "Android")
            .header("Authorization", "bearer $jwtToken")
            .build()
        return chain.proceed(request)
    }
}
/*
class HomeTAdapter(getData: ()->HomeTResponse<*>? ) : NetworkAdapter<HomeTResponse<*>>(getData) {

    override fun onReceive(response: HomeTResponse<*>?) {
        response ?: return super.onReceive(response)
        when (response.code) {
            ApiCode.SUCCESS -> super.onReceive(response)
            else -> onApiError(response.code,response.message)
        }
    }
}*/

class NetworkAdapter(val getData: () ->HomeTResponse<*>?)  {
    companion object{
        const val TAG = "NetworkAdapter"
    }
    interface ApiInterface{
        fun onReceive(response: HomeTResponse<*>?) {}
        fun onFail(type: ErrorType, code:String) {}
        fun onApiError(type: ErrorType, code:String, msg: String? = null) {}
        fun onFinish(){}
    }
    protected var apiInterface: ApiInterface? = null


    fun onSuccess(
        success: (HomeTResponse<*>?) -> Unit,
        apiError: (type: ErrorType, code:String, msg: String?) -> Unit,
        fail: (type: ErrorType, code:String) -> Unit,
        finished: (() -> Unit?)? = null
    ) {
        onStart(
            object : ApiInterface {
                override fun onReceive(response: HomeTResponse<*>?) { success(response) }
                override fun onFail(type: ErrorType, code:String) {  fail(type, code) }
                override fun onApiError(type: ErrorType, code:String, msg: String?) {  apiError(type, code, msg) }
                override fun onFinish() { if ( finished != null)  finished() }
            })
    }
    private fun onStart(apiInterface: ApiInterface?) {
        this.apiInterface = apiInterface
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = getData.invoke()
                withContext(Dispatchers.Main) { onReceive(result) }

            } catch (e: CancellationException) {
                Log.e(TAG, e)
                withContext(Dispatchers.Main) {
                    apiInterface?.onFail(ErrorType.CANCEL, HttpStatusCode.CANCEL)
                    apiInterface?.onFinish()
                }
            } catch (e: HttpException) {
                cancel()
                Log.e(TAG, e)
                withContext(Dispatchers.Main) {
                    apiInterface?.onFail(ErrorType.HTTP, e.code().toString())
                    apiInterface?.onFinish()
                }
            } catch (e: UnknownHostException) {
                cancel()
                Log.e(TAG, e)
                withContext(Dispatchers.Main) {
                    apiInterface?.onFail(ErrorType.HOST, HttpStatusCode.HOST)
                    apiInterface?.onFinish()
                }
            } catch (e: SocketTimeoutException){
                cancel()
                Log.e(TAG, e)
                withContext(Dispatchers.Main) {
                    apiInterface?.onFail(ErrorType.TIME_OUT, HttpStatusCode.TIME_OUT)
                    apiInterface?.onFinish()
                }
            } catch (e: Exception) {
                cancel()
                Log.e(TAG, e)
                withContext(Dispatchers.Main) {
                    apiInterface?.onFail(ErrorType.EXCEPTION, HttpStatusCode.EXCEPTION)
                    apiInterface?.onFinish()
                }
            }
        }
    }

    private fun onReceive(response: HomeTResponse<*>?)  {
        apiInterface?.onReceive(response)
        apiInterface?.onFinish()
    }
    private fun onApiError(code:String, msg:String? = null)  {
        apiInterface?.onApiError(ErrorType.API,code,msg)
        apiInterface?.onFinish()
    }

    private fun retry() {
        onStart(apiInterface)
    }
}

