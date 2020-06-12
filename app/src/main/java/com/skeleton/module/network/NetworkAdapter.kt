package com.skeleton.module.network


import com.google.gson.JsonSyntaxException
import com.lib.util.Log
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


abstract class NetworkAdapter<T>(var responseId:String?, val getData: () ->T?)  {
    companion object{
        const val TAG = "NetworkAdapter"
    }
    interface ApiInterface<T>{
        fun onReceive(response: T?, id:String?) {}
        fun onFail(type: ErrorType, code:String, id:String?) {}
        fun onApiError(type: ErrorType, code:String, msg: String?, id:String?) {}
        fun onFinish(id:String?){}
    }
    protected var apiInterface: ApiInterface<T>? = null
    protected var job: Job? = null
    fun onSuccess(
        success: (T?) -> Unit,
        error: (type: ErrorType, code:String,  msg: String?) -> Unit
    ) {
        onStart(
            object : ApiInterface<T> {
                override fun onReceive(response: T?, id:String?) { success(response) }
                override fun onFail(type: ErrorType, code:String, id:String?) {  error(type, code, null) }
                override fun onApiError(type: ErrorType, code:String, msg: String?, id:String?) {  error(type, code, msg) }
                override fun onFinish(id:String?) { }
            })
    }

    fun onSuccess(
        success: (T?, id:String?) -> Unit,
        apiError: (type: ErrorType, code:String, msg: String?,id:String?) -> Unit,
        fail: (type: ErrorType, code:String,id:String?) -> Unit,
        finished: ((id:String?) -> Unit?)? = null
    ) {
        onStart(
            object : ApiInterface<T> {
                override fun onReceive(response: T?, id:String?) { success(response, id) }
                override fun onFail(type: ErrorType, code:String, id:String?) {  fail(type, code, id) }
                override fun onApiError(type: ErrorType, code:String, msg: String?, id:String?) {  apiError(type, code, msg, id) }
                override fun onFinish(id:String?) { if ( finished != null)  finished(id) }
        })
    }
    private fun onStart(apiInterface: ApiInterface<T>?) {
        this.apiInterface = apiInterface

        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = getData.invoke()
                result?.let {
                    withContext(Dispatchers.Main) {
                        onReceive(result)
                    }
                }

            } catch (e: CancellationException) {
                //Log.d(TAG, e)
                //apiInterface?.onFail(ErrorType.CANCEL, HttpStatusCode.CANCEL, responseId)
                onCancel()

            } catch (e: HttpException) {
                Log.e(TAG, e)
                apiInterface?.onFail(ErrorType.HTTP, e.code().toString(), responseId)
                onFinish()

            } catch (e: UnknownHostException) {
                Log.e(TAG, e)
                apiInterface?.onFail(ErrorType.HOST, HttpStatusCode.HOST, responseId)
                onFinish()

            } catch (e: SocketTimeoutException){
                Log.e(TAG, e)
                apiInterface?.onFail(ErrorType.TIME_OUT, HttpStatusCode.TIME_OUT, responseId)
                onFinish()
            } catch (e: Exception) {
                Log.d(TAG, e)
                apiInterface?.onFail(ErrorType.EXCEPTION, HttpStatusCode.EXCEPTION, responseId)
                onFinish()
            }
        }
    }

    protected open fun onReceive(response: T?)  {
        apiInterface?.onReceive(response, responseId)
        onFinish()

    }
    protected fun onApiError(code:String, msg:String? = null)  {
        apiInterface?.onApiError(ErrorType.API,code,msg, responseId)
        apiInterface?.onFinish(responseId)
    }
    protected fun onFinish()  {
        job?.cancel()
        job = null
        apiInterface?.onFinish(responseId)
    }

    protected fun onCancel()  {
        job?.cancel()
        job = null
    }

    protected fun retry() {
        onStart(apiInterface)
    }
}