package com.kakaovx.homet.tv.page.home

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kakaovx.homet.tv.MovieList
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.error.PageError
import com.kakaovx.homet.tv.page.component.ProgramPresenter
import com.kakaovx.homet.tv.store.PageID
import com.kakaovx.homet.tv.store.api.homet.CategoryData
import com.kakaovx.homet.tv.store.api.homet.HometApiType
import com.kakaovx.homet.tv.store.api.homet.ProgramList
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
        viewModel.repo.disposeLifecycleOwner(this)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUIElements()

    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()
        viewModel.repo.hometManager.event.observe(viewLifecycleOwner ,Observer { e ->
            val type = e.type as? HometApiType
            type ?: return@Observer
            when(type){
                HometApiType.CATEGORY -> {
                    val cates = e.data as? List<CategoryData>
                    cates ?: return@Observer
                    loadedCateGory(cates)
                }
                else -> {}
            }
        })

        viewModel.repo.hometManager.error.observe(viewLifecycleOwner ,Observer { e ->
            val param = HashMap<String, Any>()
            param[PageError.API_ERROR] = e
            viewModel.pageChange(PageID.ERROR, param)
        })
        viewModel.repo.loadApi(this, HometApiType.CATEGORY)
    }


    private fun setupUIElements() {
        title = getString(R.string.browse_title)
        // over title
        headersState = BrowseSupportFragment.HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true

        activity ?: return
        brandColor = ContextCompat.getColor(activity!!, R.color.fastlane_background)
        searchAffordanceColor = ContextCompat.getColor(activity!!, R.color.search_opaque)
    }

    private fun loadedCateGory(cate: List<CategoryData>) {
        val list = MovieList.list
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        val cardPresenter = ProgramPresenter()
        cate.forEachIndexed { index, categoryData ->
            val listRowAdapter = ArrayObjectAdapter(cardPresenter)
            val header = HeaderItem(index.toLong(), categoryData.codeName)
            rowsAdapter.add(ListRow(header, listRowAdapter))
            viewModel.repo.loadPrograms(this, categoryData.codeId ?: "", 1)
        }
        adapter = rowsAdapter
    }

    private fun loadedList(list: ProgramList, cate:String) {
        val list = list.programs
        list ?: return
        val listNum = list.size
        val rowsAdapter = adapter[0] as ListRow?
        rowsAdapter ?: return
        val listRowAdapter = rowsAdapter.adapter as ArrayObjectAdapter?
        listRowAdapter ?: return
        for (i in 0 until listNum) {
            listRowAdapter.add(list[i])
        }

    }


}