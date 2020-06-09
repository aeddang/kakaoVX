package com.kakaovx.homet.tv.store.api.homet

import android.content.Context
import android.os.Build
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.kakaovx.homet.tv.store.api.*
import com.kakaovx.homet.tv.store.api.account.AccountManager
import com.kakaovx.homet.tv.store.preference.SettingPreference
import com.lib.page.PageCoroutineScope
import com.lib.page.PageLifecycleUser
import com.skeleton.module.network.HttpStatusCode
import kotlinx.coroutines.*
import java.util.*


class HometManager(
    private val context: Context,
    private val settingPreference: SettingPreference,
    private val restApi: HometApi,
    private val accountManager: AccountManager
):  PageLifecycleUser {


    val event = MutableLiveData<ApiEvent<HometApiType>>()
    val error = MutableLiveData<ApiError<HometApiType>>()
    private val scope = PageCoroutineScope()
    private var apiQ : ArrayList<HometApiData>  = ArrayList()

    override fun setDefaultLifecycleOwner(owner: LifecycleOwner) {
        accountManager.event.observe(owner, Observer { evt->
            when(evt){
                AccountManager.AccountEvent.onJWT -> {
                    apiQ.forEach {
                        loadApi(it.owner, it.type, it.params)
                    }
                    apiQ.clear()
                }
                AccountManager.AccountEvent.onError -> { }
                else -> { }
            }
        })
        scope.createJob()
    }

    override fun disposeDefaultLifecycleOwner(owner: LifecycleOwner) {
        accountManager.disposeLifecycleOwner(owner)
        scope.destoryJob()
        apiQ.clear()
        owner.let { accountManager.disposeLifecycleOwner(it) }
    }


    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun disposeLifecycleOwner(owner: LifecycleOwner){
        event.removeObservers( owner )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            apiQ.removeIf{ owner == it.owner }
        } else {
            val newQ = apiQ.filter{ owner != it.owner }.map { it }
            apiQ = newQ.toTypedArray() as ArrayList<HometApiData>
        }
    }

    private fun getApi( type:HometApiType , params:Map<String, Any?>? = null)=  runBlocking { when ( type ){
        HometApiType.CATEGORY -> restApi.getCategory( accountManager.deviceKey )
        HometApiType.PROGRAMS -> {
            var filterPurpose = "01"
            var page = ApiValue.PAGE_START
            var count = ApiValue.PAGE_COUNT
            params?.let {
                filterPurpose = it[ ApiField.FILTER_PURPOSE ] as? String? ?: filterPurpose
                page = it[ ApiField.PAGE ] as? String? ?: page
                count = it[ ApiField.COUNT ] as? String? ?: count
            }
            restApi.getPrograms( accountManager.deviceKey, filterPurpose, page, count)

        }
        HometApiType.PROGRAMS_RECENT -> restApi.getProgramsRecent( accountManager.deviceKey )
        HometApiType.PROGRAM -> {
            var programID = ""
            params?.let {
                programID  = it[ ApiField.PROGRAM_ID ] as? String? ?: programID
            }
            restApi.getProgram( programID , accountManager.deviceKey )
        }
        HometApiType.PROGRAM_EXERCISE -> {
            var programID = ""
            params?.let {
                programID  = it[ ApiField.PROGRAM_ID ] as? String? ?: programID
            }
            restApi.getProgramExercise( accountManager.deviceKey , programID )
        }
    }}

    fun loadApi(owner:LifecycleOwner, type:HometApiType , params:Map<String, Any?>? = null) = scope.launch {
        if( ! checkAccountManagerStatus(type) ) {
            apiQ.add(HometApiData(owner, type, params))
            return@launch
        }
        NetworkAdapter{
            getApi(type, params)
        }.onSuccess(
            {
                val data = it?.data
                data ?: return@onSuccess  onError( type , HttpStatusCode.DATA)
                onSuccess(type, data)
            },{ _, code, msg ->
                if(code == ApiCode.ERROR_JWT_REFRESH) {
                    apiQ.add(HometApiData(owner, type, params))
                    accountManager.reflashJWT()
                }
                else onError( type , code, msg )
            }, { _, code ->
                onError( type , code )
            }
        )
    }

    private fun checkAccountManagerStatus( type:HometApiType ) : Boolean{
        if( accountManager.deviceKey == "") {
            accountManager.setupDeviceID()
            return false
        }

        if( accountManager.status  == AccountManager.AccountStatus.busy || accountManager.status  == AccountManager.AccountStatus.error){
            accountManager.reflashJWT()
            return false
        }
        return true
    }

    private fun onSuccess(type:HometApiType , data:Any){
        event.value = ApiEvent( type, data )
    }

    private fun onError( type:HometApiType ,code:String, msg:String? = null){
        error.value  = ApiError( type, code, msg )
    }


}