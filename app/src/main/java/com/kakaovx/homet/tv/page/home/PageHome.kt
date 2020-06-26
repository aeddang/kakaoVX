package com.kakaovx.homet.tv.page.home

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.component.items.ItemProgram
import com.kakaovx.homet.tv.page.popups.PageErrorSurport
import com.kakaovx.homet.tv.page.program.PageProgram
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

class PageHome : PageBrowseSupportFragment(){
    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    protected lateinit var viewModel:PageHomeViewModel
    private val appTag = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(PageHomeViewModel::class.java)
        pageViewModel = viewModel
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUIElements()
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
            viewModel.openPopup(PageID.ERROR_SURPORT, param)
        })

        viewModel.observable.event.observe(this, Observer { evt->
            if( evt?.id != PageID.ERROR_SURPORT.value) return@Observer
            val type = evt.data as? PageErrorSurport.ErrorActionType?
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
        /*
        viewModel.pageChange(PageID.PROGRAM, param,
            (itemViewHolder.view as ImageCardView).mainImageView,
            PageProgram.SHARE_IMAGE_NAME )
        */
    }
    private fun onItemSelected(item:Any?){
        val program = item as? ProgramData
        program ?: return
        Log.i(appTag, program.toString())
        viewModel.repo.pageModel.backGroundImage.value = program.thumbnail
    }



    private fun setupUIElements() {
        title = getString(R.string.page_home_title)
        headersState = BrowseSupportFragment.HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        activity?.let {
            brandColor = ContextCompat.getColor(it, R.color.colorAccent)
            searchAffordanceColor = ContextCompat.getColor(it, R.color.color_white)
        }
    }

    private var categoryAdapters = HashMap<String, ArrayObjectAdapter>()
    private val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
    private fun loadedCateGory(cate: List<CategoryData>) {
        val cardPresenter = ProgramPresenter()
        cate.forEachIndexed { index, categoryData ->
            val listRowAdapter = ArrayObjectAdapter(cardPresenter)
            val header = HeaderItem(index.toLong(), categoryData.codeName)
            rowsAdapter.add(ListRow(header, listRowAdapter))
            val key = categoryData.codeId
            key?.let {
                categoryAdapters[it] = listRowAdapter
                viewModel.repo.loadPrograms(this, it , 1)
            }
        }
    }

    private fun loadedProgramList(programList: ProgramList, key:String) {
        viewModel.presenter.loaded()
        val list = programList.programs
        list ?: return
        list.forEach {
            categoryAdapters[key]?.add(it)
        }
        adapter = rowsAdapter
    }

    inner class ProgramPresenter:ItemPresenter(){
        override fun getItemView(): ItemImageCardView = ItemProgram(context!!)
    }


}