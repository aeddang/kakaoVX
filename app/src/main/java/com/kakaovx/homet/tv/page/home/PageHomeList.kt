package com.kakaovx.homet.tv.page.home

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*

import androidx.leanback.widget.BrowseFrameLayout.OnFocusSearchListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.component.items.ItemProgram
import com.kakaovx.homet.tv.page.component.tab.LeftTab
import com.kakaovx.homet.tv.page.popups.PageErrorSurport
import com.kakaovx.homet.tv.page.program.PageProgram
import com.kakaovx.homet.tv.page.viewmodel.BasePageViewModel
import com.kakaovx.homet.tv.page.viewmodel.PageID
import com.kakaovx.homet.tv.store.api.HomeTResponse
import com.kakaovx.homet.tv.store.api.homet.CategoryData
import com.kakaovx.homet.tv.store.api.homet.HometApiType
import com.kakaovx.homet.tv.store.api.homet.ProgramData
import com.kakaovx.homet.tv.store.api.homet.ProgramList
import com.lib.util.Log
import com.skeleton.component.item.ItemImageCardView
import com.skeleton.component.item.ItemPresenter
import com.skeleton.module.ViewModelFactory
import com.skeleton.page.PageBrowseSupportFragment
import dagger.android.support.AndroidSupportInjection
import java.util.*
import javax.inject.Inject


class PageHomeList : PageBrowseSupportFragment(){
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

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUIElements()
        workaroundFocus()
    }

    private fun setupUIElements() {
        title = getString(R.string.page_home_title)
        headersState = BrowseSupportFragment.HEADERS_DISABLED
        isHeadersTransitionOnBackEnabled = false
        activity?.let {
            brandColor = ContextCompat.getColor(it, R.color.colorAccent)
            searchAffordanceColor = ContextCompat.getColor(it, R.color.color_white)
        }
    }

    private fun workaroundFocus() {
        if (view != null) {
            val browseFrameLayout: BrowseFrameLayout =
                view!!.findViewById(androidx.leanback.R.id.browse_frame)
            val origin = browseFrameLayout.onFocusSearchListener
            browseFrameLayout.onFocusSearchListener =
                OnFocusSearchListener { focused: View?, direction: Int ->
                    if (direction == View.FOCUS_LEFT ) {
                        val item = focused as? RowHeaderView
                            ?: return@OnFocusSearchListener  origin.onFocusSearch(focused, direction)
                        return@OnFocusSearchListener viewModel.getLeftFocusTab(PageID.HOME)
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

        viewModel.repo.hometManager.success.observe(viewLifecycleOwner ,Observer { e ->
            e ?: return@Observer
            val type = e.type as? HometApiType
            val response = e.data as? HomeTResponse<*>
            response ?: return@Observer
            type ?: return@Observer
            when(type){
                HometApiType.PROGRAMS_RECENT -> {
                    val programList = response.data as? List<ProgramData>
                    programList ?: return@Observer
                    setupRecentProgramRow( programList )
                }
                else -> {}
            }
        })

        onItemViewClickedListener = OnItemViewClickedListener { _ , item, _, _ -> onItemClicked(item) }
        loadData()
    }

    private fun loadData(){
        viewModel.repo.hometManager.loadApi(this, HometApiType.PROGRAMS_RECENT)
    }

    private fun onItemClicked(item:Any?){
        val program = item as? ProgramData
        program?.let {
            Log.i(appTag, program.toString())
            val param = HashMap<String, Any>()
            param[PageProgram.PROGRAM] = program
            viewModel.pageChange(PageID.PROGRAM, param)
        }
    }

    private fun setupRecentProgramRow(programList:List<ProgramData>) {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        val listRowAdapter = ArrayObjectAdapter(ProgramPresenter())
        programList.forEach { listRowAdapter.add(it) }
        val header = HeaderItem(0, context!!.getString(R.string.list_recent_title))
        rowsAdapter.add(ListRow(header, listRowAdapter))
        adapter = rowsAdapter
    }

    inner class ProgramPresenter:ItemPresenter(){
        init {
            cardWidth = context?.resources?.getDimension(R.dimen.program_list_width)?.toInt() ?: cardWidth
            cardHeight = context?.resources?.getDimension(R.dimen.program_list_height)?.toInt() ?: cardHeight
        }
        override fun getItemView(): ItemImageCardView = ItemProgram(context!!)
    }


}