package com.kakaovx.homet.tv.page.program
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kakaovx.homet.tv.*
import com.kakaovx.homet.tv.page.popups.PageErrorSurport
import com.kakaovx.homet.tv.page.viewmodel.BasePageViewModel
import com.kakaovx.homet.tv.page.viewmodel.PageID
import com.kakaovx.homet.tv.store.api.ApiField
import com.kakaovx.homet.tv.store.api.HomeTResponse
import com.kakaovx.homet.tv.store.api.homet.HometApiType
import com.kakaovx.homet.tv.store.api.homet.ProgramData
import com.kakaovx.homet.tv.store.api.homet.ProgramDetailData
import com.lib.page.PageFragmentCoroutine
import com.skeleton.module.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.page_exercise.*
import kotlinx.android.synthetic.main.page_program.*
import kotlinx.android.synthetic.main.page_program.scroll
import kotlinx.android.synthetic.main.page_program.textInfo
import kotlinx.android.synthetic.main.page_program.textInfo1
import kotlinx.android.synthetic.main.page_program.textInfo2
import kotlinx.android.synthetic.main.page_program.textInfo3
import kotlinx.android.synthetic.main.page_program.textInfo4
import kotlinx.android.synthetic.main.page_program.title

import java.util.HashMap
import javax.inject.Inject

class PageProgram : PageFragmentCoroutine(){
    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    protected lateinit var viewModel: BasePageViewModel
    private val appTag = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(BasePageViewModel::class.java)
        pageViewModel = viewModel
    }

    override fun getLayoutResID(): Int = R.layout.page_program
    private var pageList: PageProgramExerciseList? = null
    private var programData:ProgramData? = null
    override fun onDestroyView() {
        super.onDestroyView()
        programData = null
    }

    override fun onDestroy() {
        super.onDestroy()
        pageList = null
    }

    override fun onPageParams(params: Map<String, Any?>) {
        super.onPageParams(params)
        programData = params[PROGRAM] as ProgramData
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if( pageList == null){
            try {
                val supportFragmentManager = childFragmentManager
                val transaction = supportFragmentManager.beginTransaction()
                val page = viewModel.repo.pageProvider.getPageObject(PageID.PROGRAM_EXERCISE_LIST)
                val fragment = viewModel.repo.pageProvider.getPageView(page)
                pageList = fragment as PageProgramExerciseList
                transaction.add(R.id.listArea, fragment.pageFragment)
                transaction.commit()

            }catch(e:IllegalStateException){

            }
        }
        super.onViewCreated(view, savedInstanceState)
        pageList?.programId = programData?.programId ?: ""
        pageList?.exitFocusView = title

    }


    override fun onCoroutineScope() {
        super.onCoroutineScope()
        viewModel.repo.hometManager.success.observe(this,Observer { e ->
            e ?: return@Observer
            val type = e.type as? HometApiType
            val response = e.data as? HomeTResponse<*>
            response ?: return@Observer
            type ?: return@Observer
            when(type){
                HometApiType.PROGRAM -> {
                    val data = response.data as? ProgramDetailData
                    data ?: return@Observer
                    setupData(data)
                }
                else -> {}
            }
        })

        viewModel.repo.hometManager.error.observe(viewLifecycleOwner ,Observer { e ->
            e ?: return@Observer
            if( e.type != HometApiType.PROGRAM ) return@Observer
            viewModel.presenter.loaded()
            val param = HashMap<String, Any>()
            param[PageErrorSurport.API_ERROR] = e
            param[PageErrorSurport.PAGE_EVENT_ID] = appTag
            viewModel.openPopup(PageID.ERROR_SURPORT, param)
        })
        loadData()
        textInfo.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus){
                val handler = Handler()
                handler.postDelayed(
                    { scroll.smoothScrollTo(0, scroll.maxScrollAmount) }, 100
                )
            }
        }

    }

    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
        scroll.scrollTo(0,0)
    }

    private fun setupData(data:ProgramDetailData){
        viewModel.presenter.loaded()
        title.text = data.title
        subTitle.text = data.getSubTitle(context)
        textInfo1.text = data.purposeName
        textInfo2.text = data.programClassName
        textInfo3.text = data.exercisePlanCount
        textInfo4.text = data.averagePlayTime
        textInfo.text = data.description
    }

    fun loadData(){
        viewModel.presenter.loading()
        val params = HashMap<String, String>()
        params[ApiField.PROGRAM_ID] = programData?.programId ?: ""
        viewModel.repo.hometManager.loadApi(this, HometApiType.PROGRAM , params)
    }


    companion object {
        const val PROGRAM = "program"
    }
}