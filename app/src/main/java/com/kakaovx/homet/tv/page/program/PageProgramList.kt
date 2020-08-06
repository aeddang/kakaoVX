package com.kakaovx.homet.tv.page.program

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.leanback.widget.BrowseFrameLayout.OnFocusSearchListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.component.items.ItemProgram
import com.kakaovx.homet.tv.page.popups.PageErrorSurport
import com.kakaovx.homet.tv.page.viewmodel.BasePageViewModel
import com.kakaovx.homet.tv.page.viewmodel.PageID
import com.kakaovx.homet.tv.store.api.ApiField
import com.kakaovx.homet.tv.store.api.ApiValue
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

class PageProgramList : PageBrowseSupportFragment(){
    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    protected lateinit var viewModel: BasePageViewModel
    private val appTag = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        setupUIElements()
        viewModel = ViewModelProvider(this, viewModelFactory).get(BasePageViewModel::class.java)
        pageViewModel = viewModel
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.let{
            view.setBackgroundColor(it.resources.getColor(R.color.transparent_black80))
        }
        workaroundFocus(view)
    }

    private fun workaroundFocus(view: View) {
        val browseFrameLayout: BrowseFrameLayout =
            view.findViewById(androidx.leanback.R.id.browse_frame)
        val origin = browseFrameLayout.onFocusSearchListener
        browseFrameLayout.onFocusSearchListener =
            OnFocusSearchListener { focused: View?, direction: Int ->
                if (direction == View.FOCUS_LEFT ) {
                   // val item = focused as? RowHeaderView ?: return@OnFocusSearchListener  origin.onFocusSearch(focused, direction)
                    return@OnFocusSearchListener viewModel.getLeftFocusTab(PageID.PROGRAM_LIST)
                } else {
                    return@OnFocusSearchListener  origin.onFocusSearch(focused, direction)
                }
            }
    }

    override fun onSuperBackPressAction() {
        viewModel.repo.pagePresenter.superBackPressAction()
    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()
        viewModel.presenter.loading()
        viewModel.repo.hometManager.success.observe(viewLifecycleOwner ,Observer { e ->
            e ?: return@Observer
            val type = e.type as? HometApiType
            val response = e.data as? HomeTResponse<*>
            response ?: return@Observer
            type ?: return@Observer
            when(type){
                HometApiType.CATEGORY -> {
                    val cates = response.data as? List<CategoryData>
                    cates ?: return@Observer
                    loadedCateGory(cates)
                }
                HometApiType.PROGRAMS -> {
                    val programList = response.data as? ProgramList
                    programList ?: return@Observer
                    loadedProgramList(programList, e.id ?: "")
                }
                else -> {}
            }
        })

        viewModel.repo.hometManager.error.observe(viewLifecycleOwner ,Observer { e ->
            e ?: return@Observer
            if( e.type != HometApiType.CATEGORY ) return@Observer
            val param = HashMap<String, Any>()
            param[PageErrorSurport.API_ERROR] = e
            param[PageErrorSurport.PAGE_EVENT_ID] = appTag
            viewModel.openPopup(PageID.ERROR_SURPORT, param)
        })

        viewModel.observable.event.observe(this, Observer { evt->
            if( evt?.id != PageID.ERROR_SURPORT.value) return@Observer
            val type = evt.data as? PageErrorSurport.ErrorActionType?
            if( evt.type.id != appTag) return@Observer
            type ?: return@Observer
            when(type){
                PageErrorSurport.ErrorActionType.Retry -> loadData()
                PageErrorSurport.ErrorActionType.Confirm -> pageObject?.let{ viewModel.presenter.closePopup(it)}
                else ->{}
            }
        })
        onItemViewClickedListener = OnItemViewClickedListener { itemViewHolder , item, _, _ -> onItemClicked(item, itemViewHolder) }
        onItemViewSelectedListener = OnItemViewSelectedListener { _, item, _, _ -> onItemSelected(item) }
        loadData()
    }

    private fun loadData(){
        viewModel.repo.loadApi(this, HometApiType.CATEGORY)
    }

    private fun onItemClicked(item:Any?, itemViewHolder: Presenter.ViewHolder){
        val program = item as? ProgramData
        program ?: return
        Log.i(appTag, program.toString())
        val param = HashMap<String, Any>()
        param[PageProgram.PROGRAM] = program
        viewModel.pageChange(PageID.PROGRAM, param)

    }
    private fun onItemSelected(item:Any?){
        val program = item as? ProgramData
        program ?: return
        Log.i(appTag, program.toString())
        viewModel.repo.pageModel.backGroundImage.value = program.thumbnail
        if(program.isLast) {
            program.isLast = false
            loadProgramList(program.key)
        }
    }

    private fun setupUIElements() {
        val text =  SpannableString(getString(R.string.page_program_title))
        val size =  text.length
        text.setSpan(StyleSpan(Typeface.BOLD), size-2, size, 0)
        title = text
        headersState = BrowseSupportFragment.HEADERS_DISABLED
        isHeadersTransitionOnBackEnabled = false
        activity?.let {
            brandColor = ContextCompat.getColor(it, R.color.colorAccent)
            searchAffordanceColor = ContextCompat.getColor(it, R.color.color_white)
        }
    }

    enum class PageProgressType{
        Completed, Progress, Loading
    }

    private var categoryPages = HashMap<String, Pair<PageProgressType, Int> >()
    private var categoryAdapters = HashMap<String, ArrayObjectAdapter>()
    private val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
    private fun loadedCateGory(cate: List<CategoryData>) {
        val cardPresenter = ProgramPresenter()
        cate.forEachIndexed { index, categoryData ->
            val listRowAdapter = ArrayObjectAdapter(cardPresenter)
            val headerItem = HeaderItem(index.toLong(), categoryData.codeName)
            val low = ListRow(headerItem, listRowAdapter)
            rowsAdapter.add( low )
            val key = categoryData.codeId
            key?.let {
                categoryAdapters[it] = listRowAdapter
                categoryPages[key] = Pair(PageProgressType.Progress, 0)
                loadProgramList(key)
            }
        }
    }

    private fun loadProgramList(key:String) {
        val page = categoryPages[key]
        page ?: return
        if(page.first != PageProgressType.Progress) return
        val pageIdx = page.second + 1
        categoryPages[key] = Pair(PageProgressType.Loading, pageIdx)
        viewModel.repo.loadPrograms(this, key , pageIdx)
    }

    private fun loadedProgramList(programList: ProgramList, key:String) {
        viewModel.presenter.loaded()
        val page = categoryPages[key]
        page ?: return
        val list = programList.programs
        list ?: return
        if(list.size == ApiValue.PAGE_COUNT.toInt()){
            categoryPages[key] = Pair(PageProgressType.Progress, page.second)
            list.last().apply {
                isLast = true
                this.key = key
            }
        }else{
            categoryPages[key] = Pair(PageProgressType.Completed, page.second)

        }

        list.forEach {
            categoryAdapters[key]?.add(it)
        }
        if(adapter == null) adapter = rowsAdapter
    }

    inner class ProgramPresenter:ItemPresenter(){
        init {
            cardWidth = context?.resources?.getDimension(R.dimen.program_list_width)?.toInt() ?: cardWidth
            cardHeight = context?.resources?.getDimension(R.dimen.program_list_height)?.toInt() ?: cardHeight
        }
        override fun getItemView(): ItemImageCardView = ItemProgram(context!!)
    }


}