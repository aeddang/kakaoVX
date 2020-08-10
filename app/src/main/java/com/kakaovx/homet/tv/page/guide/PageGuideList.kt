package com.kakaovx.homet.tv.page.guide

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.leanback.widget.BrowseFrameLayout.OnFocusSearchListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.component.items.ItemImage
import com.kakaovx.homet.tv.page.viewmodel.BasePageViewModel
import com.kakaovx.homet.tv.page.viewmodel.PageID
import com.kakaovx.homet.tv.store.api.homet.GuideImage
import com.lib.util.Log
import com.skeleton.component.item.ItemImageCardView
import com.skeleton.component.item.ItemPresenter
import com.skeleton.module.ViewModelFactory
import com.skeleton.page.PageBrowseSupportFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


class PageGuideList : PageBrowseSupportFragment(){
    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    protected lateinit var viewModel: BasePageViewModel
    private val appTag = javaClass.simpleName

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
        autoRollingJob = null
        items.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        workaroundFocus()

    }

    private fun setupUIElements() {
        val text =  SpannableString(getString(R.string.page_program_title))
        val size =  text.length
        text.setSpan(StyleSpan(Typeface.BOLD), size-2, size, 0)
        title = text
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
                        return@OnFocusSearchListener viewModel.getLeftFocusTab(PageID.GUIDE)
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
        viewModel.repo.guides.observe(this,Observer { e ->
            e ?: return@Observer
            e.images?.let { setupImageRow(it) }
        })
        loadData()
        onItemViewSelectedListener = OnItemViewSelectedListener { _, item, _, _ -> onItemSelected(item) }
    }

    private fun onItemSelected(item:Any?){
        val set = item as? Pair<String, Int>
        set?.let{
            currentPos = it.second
            Log.d(appTag, "onItemSelected $currentPos")
        }
        autoRolling()
    }

    private var total = 0
    private var autoRollingJob:Job? = null
    private var items = ArrayList<ItemImage>()
    private var currentPos = 0
    private fun autoRolling(){
        autoRollingJob?.cancel()
        autoRollingJob = scope.launch {
            delay(5000)
            val pos = if(currentPos >= (total-1)) 0
            else currentPos + 1
            Log.d(appTag, "autoRolling $pos")
            val find = items.find { it.index == pos}
            find?.let{
                it.requestFocus()
                currentPos = pos
                autoRolling()
            }

        }
    }

    fun loadData(){
        viewModel.repo.loadGuides(this)
    }

    private fun setupImageRow(imageList:ArrayList<GuideImage>) {
        viewModel.presenter.loaded()
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        val listRowAdapter = ArrayObjectAdapter(ImagePresenter())
        total = 0
        imageList.forEachIndexed { idx, img -> img.imgurl?.let{
            listRowAdapter.add(Pair(it, idx))
            total ++
        } }
        rowsAdapter.add(ListRow(null, listRowAdapter))
        adapter = rowsAdapter
    }



    inner class ImagePresenter:ItemPresenter(){
        init {
            cardWidth = context?.resources?.getDimension(R.dimen.guide_list_width)?.toInt() ?: cardWidth
            cardHeight = context?.resources?.getDimension(R.dimen.guide_list_height)?.toInt() ?: cardHeight
        }
        override fun getItemView(): ItemImageCardView {
            val item = ItemImage(context!!)
            items.add(item)
            return item
        }
    }


}