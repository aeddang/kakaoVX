package com.kakaovx.homet.tv.page.exercise

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.kakaovx.homet.tv.page.popups.PageErrorSurport
import com.kakaovx.homet.tv.page.viewmodel.BasePageViewModel
import com.kakaovx.homet.tv.page.viewmodel.PageID
import com.kakaovx.homet.tv.store.PageRepository
import com.kakaovx.homet.tv.store.api.HomeTResponse
import com.kakaovx.homet.tv.store.api.homet.*
import com.lib.page.PageObject
import java.util.HashMap


class PageExerciseViewModel(repo: PageRepository) : BasePageViewModel( repo ) {
    private val appTag = javaClass.simpleName
    override fun onCleared() {
        super.onCleared()
        exerciseDetailData.value = null
        exerciseMotionsData.value = null
    }

    override fun onDestroyOwner(owner: LifecycleOwner, pageObject: PageObject?) {
        exerciseDetailData.removeObservers(owner)
        exerciseMotionsData.removeObservers(owner)
    }

    val exerciseDetailData = MutableLiveData<ExerciseDetailData?>()
    val exerciseMotionsData = MutableLiveData<ExerciseMotionsData?>()

    override fun onCreateView(owner: LifecycleOwner, pageObject: PageObject?) {
        super.onCreateView(owner, pageObject)
        repo.hometManager.success.observe(owner, Observer { e ->
            e ?: return@Observer
            val type = e.type as? HometApiType
            type ?: return@Observer
            when (type) {
                HometApiType.EXERCISE_DETAIL -> {
                    presenter.loaded()
                    val responseAll = e.data as? List<HomeTResponse<*>?>
                    responseAll?.forEachIndexed{ idx, response ->
                        response?.data?.let {
                            when (idx) {
                                0 -> exerciseDetailData.value = it as? ExerciseDetailData
                                1 -> exerciseMotionsData.value = it as? ExerciseMotionsData?
                                else -> {}
                            }
                        }
                    }
                }
                else -> { }
            }
        })

        repo.hometManager.error.observe(owner ,Observer { e ->
            e ?: return@Observer
            if( e.type != HometApiType.EXERCISE ) return@Observer
            val param = HashMap<String, Any>()
            param[PageErrorSurport.API_ERROR] = e
            param[PageErrorSurport.PAGE_EVENT_ID] = appTag
            openPopup(PageID.ERROR_SURPORT, param)
        })

        observable.event.observe(owner, Observer { evt ->
            if (evt?.id != PageID.ERROR_SURPORT.value) return@Observer
            if( evt.type.id != appTag) return@Observer
            val type = evt.data as? PageErrorSurport.ErrorActionType?
            type ?: return@Observer
            when (type) {
                PageErrorSurport.ErrorActionType.Retry -> reloadData()
                PageErrorSurport.ErrorActionType.Confirm -> goBack()
                else -> {
                }
            }
        })

    }

    private var programID = ""
    private var exerciseID = ""
    private var roundID = ""
    fun loadData(programID:String, exerciseID:String, roundID:String) {
        this.programID = programID
        this.exerciseID = exerciseID
        this.roundID = roundID
        presenter.loading()
        owner?.let { repo.loadExerciseDetail(it, programID, exerciseID, roundID) }
    }

    fun reloadData() {
        presenter.loading()
        owner?.let { repo.loadExerciseDetail(it, programID, exerciseID, roundID) }
    }



}