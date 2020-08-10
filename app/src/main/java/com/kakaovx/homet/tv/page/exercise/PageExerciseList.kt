package com.kakaovx.homet.tv.page.exercise

import android.os.Bundle
import android.os.Handler

import android.view.View

import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.leanback.widget.BrowseFrameLayout.OnFocusSearchListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.component.items.ItemMotion
import com.kakaovx.homet.tv.page.viewmodel.BasePageViewModel
import com.kakaovx.homet.tv.page.viewmodel.PageID
import com.kakaovx.homet.tv.page.viewmodel.Video
import com.kakaovx.homet.tv.page.viewmodel.VideoData
import com.kakaovx.homet.tv.store.api.ApiField
import com.kakaovx.homet.tv.store.api.HomeTResponse
import com.kakaovx.homet.tv.store.api.homet.*
import com.kakaovx.homet.tv.store.api.wecandeo.PlayData
import com.kakaovx.homet.tv.util.secToLong
import com.lib.util.Log
import com.skeleton.component.item.ItemImageCardView
import com.skeleton.component.item.ItemPresenter
import com.skeleton.module.ViewModelFactory
import com.skeleton.page.PageBrowseSupportFragment
import dagger.android.support.AndroidSupportInjection
import java.util.*
import javax.inject.Inject

class PageExerciseList : PageBrowseSupportFragment(){
    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    protected lateinit var viewModel: BasePageViewModel
    private val appTag = javaClass.simpleName
    var programID:String = ""
    var exerciseData:ExerciseData? = null
    var motionData:ExerciseMotionsData? = null
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
        motionData = null
        motionDatas = null
        exerciseData = null
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
                HometApiType.EXERCISE_MOTION -> {
                    motionData = response.data as? ExerciseMotionsData
                    motionData ?: return@Observer
                    val handler = Handler()
                    handler.post(
                        Runnable { setupMotionRow( motionData?.motions ) }
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
        params[ApiField.PROGRAM_ID] = programID
        params[ApiField.EXERCISE_ID] = exerciseData?.exerciseId ?: ""
        params[ApiField.ROUND_ID] = exerciseData?.roundId ?: ""
        viewModel.repo.hometManager.loadApi(this, HometApiType.EXERCISE_MOTION , params)
    }

    private var motionDatas:List<MotionData>? = null
    private fun onItemClicked(item:Any?){

        motionData ?: return
        val motion = item as? MotionData
        motion?.let {
            Log.i(appTag, motion.toString())
            val param = HashMap<String, Any>()
            val videoData = VideoData("")
            videoData.title = motion.title
            videoData.subtitle = motion.getSubTitle(context)
            videoData.startTime = motion.timerStart?.secToLong() ?: 0
            videoData.endTime = motion.timerEnd?.secToLong() ?: 0
            val playData = PlayData(it.movieUrl ?: "")
            playData.mediaAccessApiUrl = motionData?.mediaAccessApiUrl
            playData.mediaAccessApiKey = motionData?.mediaAccessApiKey
            playData.mediaAccesskey = it.mediaAccesskey
            param[Video.PLAY_DATA] = playData
            motionDatas?.let{ list ->
                param[Video.PLAY_DATAS] = list
                param[Video.PLAY_DATA_INDEX] = list.indexOf(it)
            }
            param[Video.VIDEO] = videoData
            viewModel.pageChange(PageID.VIDEO_EXO, param)
        }
    }

    private fun setupMotionRow(motionList:List<MotionData>?) {
        motionList ?: return
        motionDatas = motionList
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        val listRowAdapter = ArrayObjectAdapter(MotionPresenter())
        motionList.forEach { listRowAdapter.add(it) }
        val header = HeaderItem(0, context!!.getString(R.string.page_exercise_motion_title, motionList.size.toString()))
        rowsAdapter.add(ListRow(header, listRowAdapter))
        adapter = rowsAdapter

    }

    inner class MotionPresenter:ItemPresenter(){
        init {
            cardWidth = context?.resources?.getDimension(R.dimen.program_list_width)?.toInt() ?: cardWidth
            cardHeight = context?.resources?.getDimension(R.dimen.program_list_height)?.toInt() ?: cardHeight
        }
        override fun getItemView(): ItemImageCardView = ItemMotion(context!!)
    }


}