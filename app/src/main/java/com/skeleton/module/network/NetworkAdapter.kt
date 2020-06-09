package com.skeleton.module.network

import com.lib.util.Log
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/*
abstract class NetworkAdapter<T>(val getData: () ->T?)  {
    companion object{
        const val TAG = "NetworkAdapter"
    }
    interface ApiInterface<T>{
        fun onReceive(response: T?) {}
        fun onFail(type: ErrorType, code:String) {}
        fun onApiError(type: ErrorType, code:String, msg: String? = null) {}
        fun onFinish(){}
    }
    protected var apiInterface: ApiInterface<T>? = null


    fun onSuccess(
        success: (T?) -> Unit,
        apiError: (type: ErrorType, code:String, msg: String?) -> Unit,
        fail: (type: ErrorType, code:String) -> Unit,
        finished: (() -> Unit?)? = null
    ) {
        onStart(
            object : ApiInterface<T> {
                override fun onReceive(response: T?) { success(response) }
                override fun onFail(type: ErrorType, code:String) {  fail(type, code) }
                override fun onApiError(type: ErrorType, code:String, msg: String?) {  apiError(type, code, msg) }
                override fun onFinish() { if ( finished != null)  finished() }
        })
    }
    private fun onStart(apiInterface: ApiInterface<T>?) {
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
            } catch (e:SocketTimeoutException){
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

    protected open fun onReceive(response: T?)  {
        apiInterface?.onReceive(response)
        apiInterface?.onFinish()
    }
    protected fun onApiError(code:String, msg:String? = null)  {
        apiInterface?.onApiError(ErrorType.API,code,msg)
        apiInterface?.onFinish()
    }

    protected fun retry() {
        onStart(apiInterface)
    }
}*/