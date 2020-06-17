package com.kakaovx.homet.tv.store.api.homet

import android.content.Context
import android.os.Build
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.kakaovx.homet.tv.store.api.*
import com.kakaovx.homet.tv.store.api.account.AccountManager
import com.kakaovx.homet.tv.store.preference.SettingPreference
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


    val success = MutableLiveData<ApiSuccess<HometApiType>?>()
    val error = MutableLiveData<ApiError<HometApiType>?>()

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
                AccountManager.AccountEvent.onJwtError -> { }
                else -> { }
            }
        })

    }

    override fun disposeDefaultLifecycleOwner(owner: LifecycleOwner) {
        accountManager.disposeLifecycleOwner(owner)
        apiQ.clear()
        owner.let { accountManager.disposeLifecycleOwner(it) }
    }


    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun disposeLifecycleOwner(owner: LifecycleOwner){
        success.removeObservers( owner )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            apiQ.removeIf{ owner == it.owner }
        } else {
            val newQ = apiQ.filter{ owner != it.owner }.map { it }
            apiQ = newQ.toTypedArray() as ArrayList<HometApiData>
        }
    }

    fun loadPrograms(owner:LifecycleOwner, filterPurpose: String = "", page:Int = 1){
        val param = HashMap<String, String>()
        param[ApiField.FILTER_PURPOSE] = filterPurpose
        param[ApiField.PAGE] = page.toString()
        loadApi(owner, HometApiType.PROGRAMS , param, filterPurpose)
    }

    fun loadProgramDetail(owner:LifecycleOwner, programID:String){
        val param = HashMap<String, String>()
        param[ApiField.PROGRAM_ID] = programID
        val flows = arrayOf(
            getApi(HometApiType.PROGRAM, param),
            getApi(HometApiType.PROGRAM_EXERCISE,param))
            //getApi(HometApiType.PROGRAMS_RECENT))
        loadApis(owner, HometApiType.PROGRAM_DETAIL, flows)

    }


    private fun getApi( type:HometApiType , params:Map<String, Any?>? = null)=  runBlocking {
        when ( type ){
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
            restApi.getProgramExercise( programID , accountManager.deviceKey )
        }
            else -> null
    }}

    fun loadApi(owner:LifecycleOwner, type:HometApiType , params:Map<String, Any?>? = null, respondId:String = ""){
        if( ! checkAccountManagerStatus(type) ) {
            apiQ.add(HometApiData(owner, type, params))
            return
        }
        HomeTAdapter{
            getApi(type, params)
        }
            .withRespondId(respondId)
            .onSuccess(
            { data, id ->
                data ?: return@onSuccess  onError( type , HttpStatusCode.DATA)
                onSuccess(type, data, id)
            }, { _, code, msg , id->
                if(code == ApiCode.ERROR_JWT_REFRESH) {
                    apiQ.add(HometApiData(owner, type, params))
                    accountManager.reflashJWT()
                }
                else onError( type , code, msg ,id)
            }, { _, code, id ->
                onError( type , code ,null ,id )
            }
        )
    }

    fun loadApis(owner:LifecycleOwner, type:HometApiType, flows: Array<HomeTResponse<out Any?>?>, respondId:String = ""){

        HomeTFlow (
            flows
        )
            .withRespondId(respondId)
            .onSuccess(
                { datas, id ->
                    datas ?: return@onSuccess  onError( type , HttpStatusCode.DATA)
                    onSuccess(type, datas, id)
                }, { _, code, msg , id->
                    onError( type , code, msg ,id)
                }
            )
    }

    fun clearEvent(){
        error.value = null
        success.value = null
    }

    private fun checkAccountManagerStatus( type:HometApiType ) : Boolean{
        if( accountManager.status  == AccountManager.AccountStatus.busy) return false
        if( accountManager.deviceKey == "") {
            accountManager.setupDeviceID()
            return false
        }
        if( accountManager.status  == AccountManager.AccountStatus.error){
            accountManager.reflashJWT()
            return false
        }
        return true
    }

    private fun onSuccess(type:HometApiType , data:Any, respondId:String? = null){
        success.value = ApiSuccess( type, data , respondId)
    }


    private fun onError( type:HometApiType ,code:String, msg:String? = null, respondId:String? = null){
        error.postValue(ApiError( type, code, msg, respondId ))
    }


}