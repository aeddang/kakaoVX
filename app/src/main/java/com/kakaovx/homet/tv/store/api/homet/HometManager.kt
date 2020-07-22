package com.kakaovx.homet.tv.store.api.homet

import android.content.Context
import android.os.Build
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.kakaovx.homet.tv.page.player.model.ExerciseResult
import com.kakaovx.homet.tv.store.api.*
import com.kakaovx.homet.tv.store.api.account.AccountManager
import com.kakaovx.homet.tv.store.preference.SettingPreference
import com.kakaovx.homet.tv.util.millisecToSec
import com.kakaovx.homet.tv.util.toPercent
import com.lib.page.PageLifecycleUser
import com.lib.util.Log
import com.skeleton.module.network.ErrorType
import com.skeleton.module.network.HttpStatusCode
import com.skeleton.module.network.NetworkAdapter
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
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

    @Suppress("UNCHECKED_CAST")
    fun loadProgramDetail(owner:LifecycleOwner, programID:String){
        val params = HashMap<String, String>()
        params[ApiField.PROGRAM_ID] = programID
        val types = arrayOf(HometApiType.PROGRAM, HometApiType.PROGRAM_EXERCISE)
        loadApiGroup(owner, types, types.map { params }.toTypedArray(), HometApiType.PROGRAM_DETAIL)
    }

    @Suppress("UNCHECKED_CAST")
    fun loadExerciseDetail(owner:LifecycleOwner, programID:String, exerciseID:String, roundID:String){
        val params = HashMap<String, String>()
        params[ApiField.PROGRAM_ID] = programID
        params[ApiField.EXERCISE_ID] = exerciseID
        params[ApiField.ROUND_ID] = roundID
        val types = arrayOf(HometApiType.EXERCISE, HometApiType.EXERCISE_MOTION)
        loadApiGroup(owner, types, types.map { params }.toTypedArray(), HometApiType.EXERCISE_DETAIL)
    }

    @Suppress("UNCHECKED_CAST")
    fun loadExercisePlayer(owner:LifecycleOwner, programID:String, exerciseID:String, roundID:String, movieType:String){
        val params = HashMap<String, String>()
        params[ApiField.PROGRAM_ID] = programID
        params[ApiField.EXERCISE_ID] = exerciseID
        params[ApiField.ROUND_ID] = roundID
        params[ApiField.MOVIE_TYPE] = movieType
        params[ApiField.PLAY_TYPE] = ApiValue.MoviePlayType.All.value
        val types = arrayOf( HometApiType.EXERCISE_START, HometApiType.EXERCISE_PLAY)
        loadApiGroup(owner, types, types.map { params }.toTypedArray(), HometApiType.EXERCISE_PLAYER, true)
    }



    private val apiGroup = HashMap<String, ApiGroup<HometApiType> >()
    fun loadApiGroup(owner:LifecycleOwner, types:Array<HometApiType> , params:Array<Map<String, Any?>?> = arrayOf(), groupType:HometApiType = HometApiType.GROUP , isSerial:Boolean = false) : String {
        val respondGroupId:String = UUID.randomUUID().toString()
        val group = ArrayList<ApiSuccess<HometApiType>>()
        val groupParam = ArrayList<Map<String, Any?>?>()
        apiGroup[respondGroupId] = ApiGroup(groupType, group, types.size, groupParam, isSerial, owner)
        types.zip( params ).forEach {
            val type = it.first
            val param = it.second
            group.add( ApiSuccess( type, null  , null) )
            groupParam.add(param)
            if(!isSerial) loadApi(owner, type , param, respondGroupId)
        }
        if(isSerial) {
            val type = types.first()
            val param = params.first()
            loadApi(owner, type , param, respondGroupId)
        }
        return respondGroupId
    }


    private fun getApi(type:HometApiType, params:Map<String, Any?>? = null)= runBlocking {
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
        HometApiType.BREAT_TIME -> {
            var exerciseID = ""
            params?.let {
                exerciseID  = it[ ApiField.EXERCISE_ID ] as? String? ?: exerciseID
            }
            restApi.getExerciseBreakTime( exerciseID , accountManager.deviceKey )
        }
        HometApiType.EXERCISE, HometApiType.EXERCISE_MOTION, HometApiType.EXERCISE_PLAY, HometApiType.EXERCISE_START, HometApiType.EXERCISE_RECORD-> {
            var programID = ""
            var exerciseID = ""
            var roundID = ""
            var playID = ""
            var movieType = ""
            var playType = ""
            params?.let {
                programID = it[ ApiField.PROGRAM_ID ] as? String? ?: programID
                exerciseID = it[ ApiField.EXERCISE_ID ] as? String? ?: exerciseID
                roundID = it[ ApiField.ROUND_ID ] as? String? ?: roundID
                playID = it[ ApiField.PLAY_ID ] as? String? ?: playID
                movieType  = it[ ApiField.MOVIE_TYPE ] as? String? ?: movieType
                playType  = it[ ApiField.PLAY_TYPE ] as? String? ?: playType

            }
            when(type){
                HometApiType.EXERCISE_MOTION -> restApi.getExerciseMotion(exerciseID , accountManager.deviceKey, programID)
                HometApiType.EXERCISE_PLAY -> restApi.getExercisePlay(exerciseID , accountManager.deviceKey, programID, roundID)
                HometApiType.EXERCISE_START -> restApi.putExerciseStart(accountManager.deviceKey, exerciseID, programID, roundID, movieType, playType)
                HometApiType.EXERCISE_RECORD -> restApi.putExerciseExec(accountManager.deviceKey, exerciseID, programID, roundID, playID)
                else -> restApi.getExercise( exerciseID , accountManager.deviceKey, programID, roundID)
            }
        }

        else -> null
    }}

    fun loadApi(owner:LifecycleOwner, type:HometApiType , params:Map<String, Any?>? = null, respondId:String = ""){
        if( ! checkAccountManagerStatus(type) ) {
            val f = apiQ.find { it.type == type }
            if( f == null) apiQ.add(HometApiData(owner, type, params))
            return
        }
        HomeTAdapter {
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
        respondId?.let{ onGroupComplete(it, type, data) }
    }

    private fun onError( type:HometApiType ,code:String, msg:String? = null, respondId:String? = null){
        error.postValue(ApiError( type, code, msg, respondId ))
        respondId?.let{ onGroupComplete(it, type) }
    }

    private fun onGroupComplete(respondId:String, type:HometApiType, data:Any? = null){
        val groupSet = apiGroup[respondId]
        groupSet?.let{ set->
            set.group.find { it.type == type }?.data = data
            if(set.finish()) success.postValue( ApiSuccess( set.type , set.group.map { it.data } , respondId) )
            else {
                if (set.isSerial) {
                    set.owner?.let {
                        val process = set.process
                        val group = set.group[process]
                        val param = if (set.params == null) null else set.params!![process]
                        loadApi(it, group.type, param, respondId)
                    }

                }
            }
        }
    }




    fun putExerciseMotionEnd(
        exerciseId: String?,
        programId: String?,
        roundId: String?,
        playId: String?,
        isMultiView: Boolean?,
        result:ExerciseResult,
        success: (HomeTResponse<*>) -> Unit,
        error: (type: ErrorType, code:String,  msg: String?) -> Unit
    ){
        HomeTAdapter {
            runBlocking {
                restApi.putExerciseMotionEnd(
                    accountManager.deviceKey,
                    exerciseId,
                    programId,
                    roundId,
                    result.motionMovieId,
                    result.motionMovieRoundId,
                    playId,
                    result.movieId,
                    result.motionId,
                    result.getResultProgressPct().toPercent(),
                    result.exerciseType,
                    result.motionParts,
                    result.rangeCount,
                    result.realRangeCount,
                    result.resultDuration.millisecToSec(),
                    result.exerciseTime.millisecToSec(),
                    result.resultStartTime.millisecToSec(),
                    result.resultEndTime.millisecToSec(),
                    result.allMotionAvg.toPercent(),
                    result.bodyPoint,
                    result.shoulderPoint,
                    result.armPoint,
                    result.corePoint,
                    result.hipPoint,
                    result.legPoint,
                    result.shinPoint,
                    isMultiView,
                    ApiValue.MovieType.Ai.value,
                    ApiValue.MoviePlayType.All.value
                )
            }
        }.onSuccess(
                { data, _ ->
                    data ?: return@onSuccess  error( ErrorType.API , HttpStatusCode.DATA, null)
                    success(data)

                }, { type , code, msg , _->
                    error( type , code, msg)

                }, { type, code, _ ->
                    error( type , code, null)
                }
            )
    }


    fun putExerciseEnd(
        exerciseId: String?,
        programId: String?,
        roundId: String?,
        playId: String?,
        isMultiView: Boolean?,
        allMotionAvg:Double,
        result:ExerciseResult,
        success: (HomeTResponse<*>) -> Unit,
        error: (type: ErrorType, code:String,  msg: String?) -> Unit
    ){
        HomeTAdapter {
            runBlocking {
                restApi.putExerciseEnd(
                    accountManager.deviceKey,
                    exerciseId,
                    programId,
                    roundId,
                    playId,
                    result.getResultProgressPct().toPercent(),
                    result.exerciseType,
                    result.resultDuration.millisecToSec(),
                    result.exerciseTime.millisecToSec(),
                    result.resultStartTime.millisecToSec(),
                    result.resultEndTime.millisecToSec(),
                    allMotionAvg.toPercent(),
                    result.bodyPoint,
                    result.shoulderPoint,
                    result.armPoint,
                    result.corePoint,
                    result.hipPoint,
                    result.legPoint,
                    result.shinPoint,
                    isMultiView
                )
            }
        }.onSuccess(
            { data, _ ->
                data ?: return@onSuccess  error( ErrorType.API , HttpStatusCode.DATA, null)
                success(data)
            }, { type , code, msg , _->
                error( type , code, msg)

            }, { type, code, _ ->
                error( type , code, null)
            }
        )
    }

}