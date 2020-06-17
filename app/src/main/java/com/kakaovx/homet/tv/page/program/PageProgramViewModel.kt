package com.kakaovx.homet.tv.page.program

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.kakaovx.homet.tv.page.viewmodel.BasePageViewModel
import com.kakaovx.homet.tv.page.viewmodel.PageViewModelEvent
import com.kakaovx.homet.tv.store.PageRepository
import com.kakaovx.homet.tv.store.api.HomeTResponse
import com.kakaovx.homet.tv.store.api.homet.ExerciseData
import com.kakaovx.homet.tv.store.api.homet.HometApiType
import com.kakaovx.homet.tv.store.api.homet.ProgramData
import com.kakaovx.homet.tv.store.api.homet.ProgramDetailData
import com.lib.page.PageObject
import com.lib.util.Log
import java.util.*


class PageProgramViewModel(repo: PageRepository) : BasePageViewModel( repo ) {
    private val appTag = javaClass.simpleName
    override fun onCleared() {
        super.onCleared()
        programDetailData.value = null
        exerciseList.value = null
        programList.value = null
    }

    val programDetailData = MutableLiveData<ProgramDetailData?>()
    val exerciseList = MutableLiveData<List<ExerciseData>?>()
    val programList = MutableLiveData<List<ProgramData>?>()
    private var dataLoadedNum = 0

    override fun onCreateView(owner: LifecycleOwner, pageObject: PageObject?) {
        super.onCreateView(owner, pageObject)
        repo.hometManager.success.observe(owner, Observer { e ->
            e ?: return@Observer
            val type = e.type as? HometApiType
            val responseAll = e.data as? ArrayList<HomeTResponse<*>>
            responseAll ?: return@Observer
            type ?: return@Observer
            when (type) {
                HometApiType.PROGRAM_DETAIL -> {
                    responseAll.forEachIndexed{ idx, response ->
                        when (idx) {
                            0 -> programDetailData.value = response.data as? ProgramDetailData
                            1 -> exerciseList.value = response.data as? List<ExerciseData>
                            2 -> programList.value = response.data as? List<ProgramData>
                            else -> {}
                        }
                    }
                }
                else -> { }
            }
        })

    }

    fun loadData(programID:String) {
        owner?.let {
            repo.loadProgramDetail(it, programID)
        }

    }



}