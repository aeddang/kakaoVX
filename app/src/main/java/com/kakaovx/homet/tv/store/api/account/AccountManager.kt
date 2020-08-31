package com.kakaovx.homet.tv.store.api.account

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.kakaovx.homet.tv.store.api.*
import com.kakaovx.homet.tv.store.preference.AccountPreference
import com.kakaovx.homet.tv.store.preference.SettingPreference
import com.lib.page.PageLifecycleUser
import com.skeleton.module.network.HttpStatusCode
import kotlinx.coroutines.*


class AccountManager  (
    private val context: Context,
    private val accountPreference: AccountPreference,
    private val settingPreference: SettingPreference,
    private val restApi: AccountApi,
    private val interceptor: HomeTInterceptor
) : PageLifecycleUser {


    private val appTag = javaClass.simpleName
    var deviceKey: String = "" ; private set
    var tempJWT: String = "" ; private set

    enum class AccountEvent{
        onActivity,onDeviceKey,onJWT, onJwtError
    }

    enum class AccountStatus{
        initiate, ready, busy, error
    }

    var status:AccountStatus = AccountStatus.initiate ;private set
    var error:ApiError<*>? = null
    val event = MutableLiveData<AccountEvent>()

    override fun setDefaultLifecycleOwner(owner: LifecycleOwner) {
        status = AccountStatus.initiate
        event.postValue(AccountEvent.onActivity)

    }
    override fun disposeDefaultLifecycleOwner(owner: LifecycleOwner) {
    }

    override fun disposeLifecycleOwner(owner: LifecycleOwner){
        event.removeObservers( owner )
    }

    @SuppressLint("HardwareIds")
    fun setupDeviceID() {
        status = AccountStatus.busy
        deviceKey = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        event.postValue(AccountEvent.onDeviceKey)
        setJWT()
    }

    private fun setJWT() {
        val prev = accountPreference.getJWT()
        if (prev == "") loadJWT() else onReady(prev)
    }

    private fun getJWT() = runBlocking { restApi.getJWT(deviceKey) }
    private fun getJWTRefresh() = runBlocking { restApi.getJWTRefresh(deviceKey, tempJWT) }


    fun loadJWT()  {
        if( deviceKey == "") {
            onJwtError()
            return
        }
        status = AccountStatus.busy
        HomeTAdapter{
            getJWT()
        }
            .onSuccess(
                { onLoadJWT(it) },
                { _, code , msg ->
                    onJwtError( ApiError(AccountApiType.JWT, code , msg )) }
            )
    }

    fun reflashJWT() {
        if( deviceKey == ""){
            onJwtError()
            return
        }
        if(tempJWT == ""){
            loadJWT()
            return
        }
        status = AccountStatus.busy
        HomeTAdapter{
            getJWTRefresh()
        }
            .onSuccess(
                { onLoadJWT(it) },
                { _, code , msg ->
                    onJwtError( ApiError(AccountApiType.JWT_REFRESH, code , msg )) }
            )
    }

    private fun onLoadJWT(data:HomeTResponse<*>?) {
        val jwtData = data?.data as JWTData?
        jwtData ?: return onJwtError(ApiError(AccountApiType.JWT, HttpStatusCode.PARSE))
        jwtData.jwt ?: return onJwtError(ApiError(AccountApiType.JWT, HttpStatusCode.PARSE))
        onReady(jwtData.jwt!!)
    }
    private fun onJwtError(e:ApiError<*>? = null) {
        error = e
        status = AccountStatus.error
        event.postValue(AccountEvent.onJwtError)
    }

    private fun onReady(jwt:String) {
        tempJWT = jwt
        interceptor.jwtToken = tempJWT
        status = AccountStatus.ready
        event.value = AccountEvent.onJWT
    }
}