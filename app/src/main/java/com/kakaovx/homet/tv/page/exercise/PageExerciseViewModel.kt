package com.kakaovx.homet.tv.page.exercise

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.kakaovx.homet.tv.page.viewmodel.BasePageViewModel
import com.kakaovx.homet.tv.store.PageRepository
import com.kakaovx.homet.tv.store.api.HomeTResponse
import com.kakaovx.homet.tv.store.api.homet.*
import com.lib.page.PageObject


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

    }

    fun loadData(programID:String, exerciseID:String, roundID:String) {
        presenter.loading()
        owner?.let { repo.loadExerciseDetail(it, programID, exerciseID, roundID) }
    }



}