package com.kakaovx.homet.tv.page.exercise

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.core.content.ContextCompat
import androidx.leanback.app.BrowseSupportFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.kakaovx.homet.tv.*
import com.kakaovx.homet.tv.page.player.PagePlayer
import com.kakaovx.homet.tv.page.popups.PageErrorSurport
import com.kakaovx.homet.tv.page.viewmodel.BasePageViewModel
import com.kakaovx.homet.tv.page.viewmodel.PageID
import com.kakaovx.homet.tv.store.api.ApiField
import com.kakaovx.homet.tv.store.api.HomeTResponse
import com.kakaovx.homet.tv.store.api.homet.*
import com.lib.page.PageFragmentCoroutine
import com.lib.page.PageObject
import com.skeleton.module.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.page_exercise.*
import java.util.HashMap
import javax.inject.Inject

class PageExercise : PageFragmentCoroutine(){
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

    override fun getLayoutResID(): Int = R.layout.page_exercise
    private var pageList: PageExerciseList? = null
    private var programID:String = ""
    private var exerciseData:ExerciseData? = null
    override fun onDestroyView() {
        super.onDestroyView()
        scrollHandler.removeCallbacks(scrollRunable)
        exerciseData = null
    }

    override fun onWillDestory(pageObject: PageObject?) {
        super.onWillDestory(pageObject)
        isInitFocus = true
    }
    override fun onDestroy() {
        super.onDestroy()
        pageList = null
    }

    override fun onPageParams(params: Map<String, Any?>) {
        super.onPageParams(params)
        programID = params[PROGRAM_ID] as? String ?: programID
        exerciseData = params[EXERCISE] as ExerciseData
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if( pageList == null){
            try {
                val supportFragmentManager = childFragmentManager
                val transaction = supportFragmentManager.beginTransaction()
                val page = viewModel.repo.pageProvider.getPageObject(PageID.EXERCISE_LIST)
                val fragment = viewModel.repo.pageProvider.getPageView(page)
                pageList = fragment as PageExerciseList
                transaction.add(R.id.listArea, fragment.pageFragment)
                transaction.commit()

            }catch(e:IllegalStateException){

            }
        }
        isInitFocus = true
        super.onViewCreated(view, savedInstanceState)
        pageList?.programID = programID
        pageList?.exerciseData = exerciseData
        pageList?.exitFocusView = btnExercise

    }

    private var scrollHandler = Handler()
    private var scrollRunable = Runnable { scroll.smoothScrollTo(0, 0) }
    private var isInitFocus = true //시작시 움직이는거 보기싫
    override fun onCoroutineScope() {
        super.onCoroutineScope()
        btnExercise.setOnClickListener{

            val param = HashMap<String, Any>()
            exerciseData?.let {  param[PagePlayer.EXERCISE] = it }
            param[PagePlayer.PROGRAM_ID] = programID
            viewModel.pageChange(PageID.PLAYER, param)
        }


        btnExercise.setOnFocusChangeListener { _, hasFocus ->
            if(isInitFocus){
                isInitFocus = false
                scroll.scrollTo(0,0)
                return@setOnFocusChangeListener
            }
            if(!hasFocus) return@setOnFocusChangeListener
            scrollHandler.postDelayed(scrollRunable, 100)
        }


        viewModel.repo.hometManager.success.observe(this,Observer { e ->
            e ?: return@Observer
            val type = e.type as? HometApiType
            val response = e.data as? HomeTResponse<*>
            response ?: return@Observer
            type ?: return@Observer
            when(type){
                HometApiType.EXERCISE_MOTION -> btnExercise.requestFocus()
                HometApiType.EXERCISE -> {
                    val data = response.data as? ExerciseDetailData
                    data ?: return@Observer
                    setupData(data)
                }
                else -> {}
            }
        })

        viewModel.repo.hometManager.error.observe(viewLifecycleOwner ,Observer { e ->
            e ?: return@Observer
            if( e.type != HometApiType.EXERCISE ) return@Observer
            viewModel.presenter.loaded()
            val param = HashMap<String, Any>()
            param[PageErrorSurport.API_ERROR] = e
            param[PageErrorSurport.PAGE_EVENT_ID] = appTag
            viewModel.openPopup(PageID.ERROR_SURPORT, param)
        })

        viewModel.observable.event.observe(this, Observer { evt->
            if( evt?.id != PageID.ERROR_SURPORT.value  && evt?.id != PageID.VIDEO_EXO.value) return@Observer
            btnExercise.requestFocus()
        })
        loadData()

    }

    private fun setupData(data:ExerciseDetailData){
        viewModel.presenter.loaded()
        title.text = data.title
        textInfo1.text = data.bodyPartsName
        textInfo2.text = data.getTime(context)
        textInfo3.text = data.getKal(context)
        textInfo4.text = data.exerciseToolsName
        textInfo.text = data.description
        context ?: return
        Glide.with(context!!)
            .load(data.thumbnail)
            .centerCrop()
            .error( ContextCompat.getDrawable(context!!, R.drawable.ic_content_no_image) )
            .into(image)
    }

    fun loadData(){
        viewModel.presenter.loading()
        val params = HashMap<String, String>()
        params[ApiField.PROGRAM_ID] = programID
        params[ApiField.EXERCISE_ID] = exerciseData?.exerciseId ?: ""
        params[ApiField.ROUND_ID] = exerciseData?.roundId ?: ""
        viewModel.repo.hometManager.loadApi(this, HometApiType.EXERCISE , params)
    }

    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
        btnExercise.requestFocus()
    }


    companion object {
        const val PROGRAM_ID = "programID"
        const val EXERCISE = "exercise"
    }
}