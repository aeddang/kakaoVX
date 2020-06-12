package com.kakaovx.homet.tv.page.program

import androidx.lifecycle.LifecycleOwner
import com.kakaovx.homet.tv.page.error.PageError
import com.kakaovx.homet.tv.page.viewmodel.BasePageViewModel
import com.kakaovx.homet.tv.page.viewmodel.PageID
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
        programDetailData = null
        exerciseList = null
        programList = null
    }

    var programDetailData:ProgramDetailData? = null; private set
    var exerciseList:List<ExerciseData>? = null; private set
    var programList:List<ProgramData>? = null; private set
    private var dataLoadedNum = 0



    override fun onCreateView(owner: LifecycleOwner, pageObject: PageObject?) {
        super.onCreateView(owner, pageObject)
        repo.hometManager.event.observe(owner ,androidx.lifecycle.Observer { e ->
            e ?: return@Observer
            val type = e.type as? HometApiType
            val response = e.data as? HomeTResponse<*>
            response ?: return@Observer
            type ?: return@Observer
            when(type){
                HometApiType.PROGRAM -> {
                    programDetailData = response.data as? ProgramDetailData
                    checkData()
                }
                HometApiType.PROGRAM_EXERCISE -> {
                    exerciseList = response.data as? List<ExerciseData>
                    checkData()
                }
                HometApiType.PROGRAMS_RECENT -> {
                    programList = response.data as? List<ProgramData>
                    checkData()
                }
                else -> {}
            }
        })

        repo.hometManager.error.observe(owner ,androidx.lifecycle.Observer { e ->
            e ?: return@Observer
            val type = e.type as? HometApiType
            when(type){
                HometApiType.PROGRAM -> {
                    val param = HashMap<String, Any>()
                    param[PageError.API_ERROR] = e
                    openPopup(PageID.ERROR, param)
                }
                HometApiType.PROGRAM_EXERCISE, HometApiType.PROGRAMS_RECENT -> checkData()
                else -> {}
            }
        })
    }

    private fun checkData(){
        dataLoadedNum ++
        Log.i(appTag, "loadData $dataLoadedNum")
        if(dataLoadedNum == 3) event.postValue(PageViewModelEvent.DataLoaded)
    }

    fun loadData(programID:String){
        dataLoadedNum = 0

        owner?.let {
            repo.loadProgramDetail(it, programID)
            repo.loadApi(it, HometApiType.PROGRAMS_RECENT)
        }

    }
}