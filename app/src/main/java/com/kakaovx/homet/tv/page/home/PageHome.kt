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
import com.kakaovx.homet.tv.page.home.component.CardPresenter
import com.kakaovx.homet.tv.store.PageID
import com.kakaovx.homet.tv.store.api.homet.CategoryData
import com.kakaovx.homet.tv.store.api.homet.HometApiType
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


    override val hasBackPressAction: Boolean
        get(){
            return when(headersState){
                HEADERS_HIDDEN -> false
                else ->{
                    headersState = HEADERS_HIDDEN
                    true
                } } }

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

    override fun onTransactionCompleted() {
        super.onTransactionCompleted()

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

    private fun loadedCateGory(list: List<CategoryData>) {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        val cardPresenter = CardPresenter()
        for (i in 0 until NUM_ROWS) {
            if (i != 0) Collections.shuffle(list)
            val listRowAdapter = ArrayObjectAdapter(cardPresenter)
            for (j in 0 until NUM_COLS) {
                listRowAdapter.add(list[j % 5])
            }
            val header = HeaderItem(i.toLong(), MovieList.MOVIE_CATEGORY[i])
            rowsAdapter.add(ListRow(header, listRowAdapter))
        }

        val gridHeader = HeaderItem(NUM_ROWS.toLong(), "PREFERENCES")
        val mGridPresenter = GridItemPresenter()
        val gridRowAdapter = ArrayObjectAdapter(mGridPresenter)
        gridRowAdapter.add(resources.getString(R.string.grid_view))
        gridRowAdapter.add(getString(R.string.error_fragment))
        gridRowAdapter.add(resources.getString(R.string.personal_settings))
        rowsAdapter.add(ListRow(gridHeader, gridRowAdapter))
        adapter = rowsAdapter
    }

    private inner class GridItemPresenter : Presenter() {
        override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
            val view = TextView(parent.context)
            view.layoutParams = ViewGroup.LayoutParams(
                GRID_ITEM_WIDTH,
                GRID_ITEM_HEIGHT
            )
            view.isFocusable = true
            view.isFocusableInTouchMode = true
            activity?.let {  view.setBackgroundColor(ContextCompat.getColor(it, R.color.default_background)) }
            view.setTextColor(Color.WHITE)
            view.gravity = Gravity.CENTER
            return Presenter.ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any) {
            (viewHolder.view as TextView).text = item as String
        }

        override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {}
    }

    companion object {
        private val TAG = "MainFragment"
        private val BACKGROUND_UPDATE_DELAY = 300
        private val GRID_ITEM_WIDTH = 200
        private val GRID_ITEM_HEIGHT = 200
        private val NUM_ROWS = 6
        private val NUM_COLS = 15
    }

}