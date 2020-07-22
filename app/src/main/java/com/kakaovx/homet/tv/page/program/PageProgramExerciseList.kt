package com.kakaovx.homet.tv.page.program

import android.os.Bundle
import android.os.Handler

import android.view.View

import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.leanback.widget.BrowseFrameLayout.OnFocusSearchListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.component.items.ItemExercise
import com.kakaovx.homet.tv.page.exercise.PageExercise
import com.kakaovx.homet.tv.page.viewmodel.BasePageViewModel
import com.kakaovx.homet.tv.page.viewmodel.PageID
import com.kakaovx.homet.tv.store.api.ApiField
import com.kakaovx.homet.tv.store.api.HomeTResponse
import com.kakaovx.homet.tv.store.api.homet.ExerciseData
import com.kakaovx.homet.tv.store.api.homet.HometApiType
import com.lib.util.Log
import com.skeleton.component.item.ItemImageCardView
import com.skeleton.component.item.ItemPresenter
import com.skeleton.module.ViewModelFactory
import com.skeleton.page.PageBrowseSupportFragment
import dagger.android.support.AndroidSupportInjection
import java.util.*
import javax.inject.Inject


class PageProgramExerciseList : PageBrowseSupportFragment(){
    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    protected lateinit var viewModel: BasePageViewModel
    private val appTag = javaClass.simpleName
    var programId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(BasePageViewModel::class.java)
        pageViewModel = viewModel
        setupUIElements()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        exitFocusView = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleView.visibility= View.GONE
        workaroundFocus()

    }

    private fun setupUIElements() {
        headersState = BrowseSupportFragment.HEADERS_DISABLED
        isHeadersTransitionOnBackEnabled = false

    }
    var exitFocusView:View? = null
    private fun workaroundFocus() {
        if (view != null) {
            val browseFrameLayout: BrowseFrameLayout =
                view!!.findViewById(androidx.leanback.R.id.browse_frame)
            val origin = browseFrameLayout.onFocusSearchListener
            browseFrameLayout.onFocusSearchListener =
                OnFocusSearchListener { focused: View?, direction: Int ->

                    if (direction == View.FOCUS_UP ) {
                        return@OnFocusSearchListener exitFocusView
                    }
                    if (direction == View.FOCUS_LEFT ) {
                        return@OnFocusSearchListener exitFocusView
                    } else {
                        return@OnFocusSearchListener  origin.onFocusSearch(focused, direction)
                    }
                }
        }
    }

    override fun onSuperBackPressAction() {
        viewModel.repo.pagePresenter.superBackPressAction()
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
                HometApiType.PROGRAM_EXERCISE -> {
                    val exerciseList = response.data as? List<ExerciseData>
                    exerciseList ?: return@Observer
                    val handler = Handler()
                    handler.post(
                        Runnable { setupExerciseRow( exerciseList ) }
                    )
                }
                else -> {}
            }
        })
        onItemViewClickedListener = OnItemViewClickedListener { _ , item, _, _ -> onItemClicked(item) }
        loadData()

    }

    fun loadData(){
        val params = HashMap<String, String>()
        params[ApiField.PROGRAM_ID] = programId
        viewModel.repo.hometManager.loadApi(this, HometApiType.PROGRAM_EXERCISE, params)
    }

    private fun onItemClicked(item:Any?){
        val exercise = item as? ExerciseData
        exercise?.let {
            Log.i(appTag, exercise.toString())
            val param = HashMap<String, Any>()
            param[PageExercise.PROGRAM_ID] = programId ?: ""
            param[PageExercise.EXERCISE] = it
            viewModel.pageChange(PageID.EXERCISE, param)
        }
    }

    private fun setupExerciseRow(exerciseList:List<ExerciseData>) {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        val listRowAdapter = ArrayObjectAdapter(ExercisePresenter())
        exerciseList.forEach { listRowAdapter.add(it) }
        val header = HeaderItem(0, context!!.getString(R.string.page_program_list_title))
        rowsAdapter.add(ListRow(header, listRowAdapter))
        adapter = rowsAdapter

    }

    inner class ExercisePresenter:ItemPresenter(){
        init {
            cardWidth = context?.resources?.getDimension(R.dimen.program_list_width)?.toInt() ?: cardWidth
            cardHeight = context?.resources?.getDimension(R.dimen.program_list_height)?.toInt() ?: cardHeight
        }
        override fun getItemView(): ItemImageCardView = ItemExercise(context!!)
    }


}