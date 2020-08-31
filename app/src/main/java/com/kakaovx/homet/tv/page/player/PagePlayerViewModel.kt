package com.kakaovx.homet.tv.page.player

import android.content.DialogInterface
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.player.model.Exercise
import com.kakaovx.homet.tv.page.player.model.ExerciseResult
import com.kakaovx.homet.tv.page.player.model.Player
import com.kakaovx.homet.tv.page.player.model.PlayerUIEvent
import com.kakaovx.homet.tv.page.popups.PageErrorSurport
import com.kakaovx.homet.tv.page.viewmodel.BasePageViewModel
import com.kakaovx.homet.tv.page.viewmodel.PageID
import com.kakaovx.homet.tv.store.PageRepository
import com.kakaovx.homet.tv.store.api.ApiField
import com.kakaovx.homet.tv.store.api.ApiValue
import com.kakaovx.homet.tv.store.api.HomeTResponse
import com.kakaovx.homet.tv.store.api.homet.*
import com.lib.page.PageObject
import com.skeleton.component.alert.CustomDialog
import com.skeleton.component.alert.CustomToast
import com.skeleton.module.network.ErrorType
import io.reactivex.Single
import java.util.HashMap




class PagePlayerViewModel(repo: PageRepository) : BasePageViewModel( repo ) {
    val appTag = javaClass.simpleName
    val player = Player()
    val exercise = MutableLiveData<Exercise?>()
    var isExitChecked = false ; private set
    var isCompleted = false ; private set
    fun goBackImmediately() {
        isExitChecked = true
        goBack()
    }

    override fun onCleared() {
        super.onCleared()
    }

    override fun onDestroyOwner(owner: LifecycleOwner, pageObject: PageObject?) {
        exercise.removeObservers(owner)
        exercise.value?.disposeLifecycleOwner(owner)
        exercise.value = null
    }

    override fun onCreateView(owner: LifecycleOwner, pageObject: PageObject?) {
        super.onCreateView(owner, pageObject)
        isExitChecked = false
        isCompleted = false
        var exercisePlayData: ExercisePlayData? = null
        var exerciseStartData: ExerciseStartData? = null

        repo.hometManager.success.observe(owner, Observer { e ->
            e ?: return@Observer
            val type = e.type as? HometApiType
            type ?: return@Observer
            val res = e.data as? HomeTResponse<*>
            when (type) {
                HometApiType.EXERCISE_PLAY -> exercisePlayData = res?.data  as? ExercisePlayData
                HometApiType.EXERCISE_START -> exerciseStartData = res?.data as? ExerciseStartData
                HometApiType.EXERCISE_PLAYER -> {
                    presenter.loaded()
                    if( exercisePlayData != null && exerciseStartData != null){
                        val exercise = Exercise(exerciseID, repo.ctx )
                        exercise.initSet(exercisePlayData!!)
                        exercise.setStartData(exerciseStartData!!)
                        presenter.loaded()
                        this.exercise.value = exercise
                    }
                }
                else -> { }
            }
        })

        var isError = false
        repo.hometManager.error.observe(owner ,Observer { e ->
            e ?: return@Observer
            if( isError ) return@Observer
            if( e.type != HometApiType.EXERCISE_PLAY && e.type != HometApiType.EXERCISE_START ) return@Observer
            isError = true
            val param = HashMap<String, Any>()
            param[PageErrorSurport.API_ERROR] = e
            param[PageErrorSurport.PAGE_EVENT_ID] = appTag
            openPopup(PageID.ERROR_SURPORT, param)
        })

    }
    var isMultiView= false
    private var exerciseID:String = ""
    private var programID:String = ""
    private var roundID:String = ""
    fun loadData(programID:String, exerciseID:String, roundID:String, movieType:String) {
        this.exerciseID = exerciseID
        this.programID = programID
        this.roundID = roundID
        presenter.loading()
        owner?.let { repo.loadExercisePlayer(it, programID, exerciseID, roundID, movieType) }
    }

    var isPutResult = false; private set
    fun putExerciseProgressResult(result:ExerciseResult? = null){
        val exerciseData = exercise.value
        exerciseData ?: return
        val progressResult = result ?: exerciseData.currentResultObservable.value
        progressResult ?: return
        if(!progressResult.isEffectiveExercise) return
        val avg = progressResult.allMotionAvg
        repo.hometManager.putExerciseMotionEnd(exerciseID, programID, roundID,
            exerciseData.info.playId, isMultiView,
            progressResult,
            {
                isPutResult = true
                exerciseData.addAvg( avg )
                //CustomToast.makeToast(repo.ctx, R.string.page_player_exercise_result_complete).show()
                progressResult.reset()
            },{ _, _, _->
                //CustomToast.makeToast(repo.ctx, R.string.page_player_exercise_result_error).show()
                progressResult.reset()
            }
        )
    }

    fun putExerciseRecord() {
        val exerciseData = exercise.value
        exerciseData ?: return
        val params = HashMap<String, String>()
        params[ApiField.PROGRAM_ID] = programID
        params[ApiField.EXERCISE_ID] = exerciseID
        params[ApiField.ROUND_ID] = roundID
        params[ApiField.PLAY_ID] = exerciseData.info.playId
        owner?.let { repo.hometManager.loadApi(it, HometApiType.EXERCISE_RECORD, params, "") }
    }

    fun putExerciseCompleted(result:ExerciseResult? = null){
        presenter.loading()
        val exerciseData = exercise.value
        exerciseData ?: return
        val progressResult =exerciseData.currentResultObservable.value
        if(progressResult != null && progressResult.isEffectiveExercise){
            val avg = progressResult.allMotionAvg
            repo.hometManager.putExerciseMotionEnd(exerciseID, programID, roundID,
                exerciseData.info.playId, isMultiView,
                progressResult,
                {
                    isPutResult = true
                    exerciseData.addAvg( avg )
                    putExerciseCompletedResult(result)
                    //CustomToast.makeToast(repo.ctx, R.string.page_player_exercise_result_complete).show()
                    progressResult.reset()
                },{ _, _, _->

                   // CustomToast.makeToast(repo.ctx, R.string.page_player_exercise_result_error).show()
                    progressResult.reset()
                }
            )

        }else putExerciseCompletedResult(result)
    }

    private fun putExerciseCompletedResult(result:ExerciseResult?) {
        if(result == null) exerciseCompleted()
        val exerciseData = exercise.value
        exerciseData ?: return exerciseCompleted()
        if(!isPutResult) return exerciseCompleted()
        val totalAvg = exerciseData.getTotalAvg()
        repo.hometManager.putExerciseEnd(exerciseID, programID, roundID,
            exerciseData.info.playId, isMultiView,totalAvg, result!!,
            {
                //CustomToast.makeToast(repo.ctx, R.string.page_player_exercise_result_complete).show()
                exerciseCompleted()
                result.reset()
            },{ _, _, _->
               // CustomToast.makeToast(repo.ctx, R.string.page_player_exercise_result_error).show()
                exerciseCompleted()
                result.reset()
            }
        )
    }

    private fun exerciseCompleted() {
        isCompleted = true
        presenter.loaded()
        player.uiEvent.value = PlayerUIEvent.Pause
        CustomDialog.makeDialog(presenter.activity, null, R.string.page_player_completed)
            .setCancelable(false)
            .setNegativeButton(R.string.btn_action_confirm)
            {  _,_ ->

                goBack()
               // pageChange(PageID.PROGRAM_LIST)
            }
            .show()
    }
}