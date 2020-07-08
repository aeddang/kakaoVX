package com.kakaovx.homet.tv.page.exercise
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.leanback.app.DetailsSupportFragmentBackgroundController
import androidx.leanback.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.kakaovx.homet.tv.*
import com.kakaovx.homet.tv.page.component.items.ItemMotion
import com.kakaovx.homet.tv.page.player.PagePlayer
import com.kakaovx.homet.tv.page.viewmodel.Video
import com.kakaovx.homet.tv.page.viewmodel.VideoData
import com.kakaovx.homet.tv.page.viewmodel.PageID
import com.kakaovx.homet.tv.store.api.homet.*
import com.kakaovx.homet.tv.store.api.wecandeo.PlayData
import com.lib.util.CommonUtil
import com.lib.util.Log
import com.skeleton.component.item.ItemImageCardView
import com.skeleton.component.item.ItemPresenter
import com.skeleton.module.ViewModelFactory
import com.skeleton.page.PageDetailsSupportFragment
import dagger.android.support.AndroidSupportInjection
import java.util.HashMap
import javax.inject.Inject

class PageExercise : PageDetailsSupportFragment(){
    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    protected lateinit var viewModel: PageExerciseViewModel
    private val appTag = javaClass.simpleName

    private var programID:String = ""
    private var exerciseData:ExerciseData? = null
    private lateinit var detailsBackground: DetailsSupportFragmentBackgroundController
    private lateinit var presenterSelector: ClassPresenterSelector
    private lateinit var exereciseAdapter: ArrayObjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(PageExerciseViewModel::class.java)
        pageViewModel = viewModel
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }

    override fun onPageParams(params: Map<String, Any?>) {
        super.onPageParams(params)
        programID = params[PROGRAM_ID] as? String ?: programID
        exerciseData = params[EXERCISE] as ExerciseData
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        detailsBackground = DetailsSupportFragmentBackgroundController(this)
        presenterSelector = ClassPresenterSelector()
        exereciseAdapter = ArrayObjectAdapter(presenterSelector)
        exerciseData?.let{
            initializeBackground(it)
        }
        adapter = exereciseAdapter
    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()
        exerciseData ?: return
        viewModel.exerciseDetailData.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer
            setupDetailsOverviewRow(it) })
        viewModel.exerciseMotionsData.observe(viewLifecycleOwner, Observer { motionData->
            motionData ?: return@Observer
            motionData.motions?.let {
                setupMotionListRow(it)
            }
        })

        viewModel.loadData(programID,
            exerciseData?.exerciseId ?: "",
            exerciseData?.roundId ?: "")

        onItemViewClickedListener = OnItemViewClickedListener { itemViewHolder , item, _, _ -> onItemClicked(item) }
    }

    private fun onItemClicked(item:Any?){
        val motionData = viewModel.exerciseMotionsData.value
        motionData ?: return
        val motion = item as? MotionData
        motion?.let {
            Log.i(appTag, motion.toString())
            val param = HashMap<String, Any>()
            val videoData = VideoData("")
            videoData.title = motion.title
            videoData.subtitle = motion.subtitle
            val playData = PlayData(it.movieUrl ?: "")
            playData.mediaAccessApiUrl = motionData.mediaAccessApiUrl
            playData.mediaAccessApiKey = motionData.mediaAccessApiKey
            playData.mediaAccesskey = it.mediaAccesskey
            param[Video.PLAY_DATA] = playData
            param[Video.VIDEO] = videoData
            viewModel.pageChange(PageID.VIDEO_EXO, param)
        }
    }

    private fun initializeBackground(data:ExerciseData) {
        detailsBackground.enableParallax()
        context ?: return
        Glide.with(this).asBitmap()
            .load(data.thumbnail).centerCrop()
            .error(ContextCompat.getDrawable(context!!, R.drawable.default_background))
            .into(object : CustomTarget<Bitmap>(){
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    detailsBackground.coverBitmap = resource
                    exereciseAdapter.notifyArrayItemRangeChanged(0, exereciseAdapter.size())
                }
                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun setupDetailsOverviewRow(data:ExerciseDetailData) {
        context ?: return

        val row = DetailsOverviewRow(data)
        row.imageDrawable = ContextCompat.getDrawable(context!!, R.drawable.default_background)
        val width = CommonUtil.convertDpToPixel(context!!, DETAIL_THUMB_WIDTH)
        val height = CommonUtil.convertDpToPixel(context!!, DETAIL_THUMB_HEIGHT)
        Glide.with(this).asBitmap()
            .load(data.thumbnail).centerCrop()
            .error(ContextCompat.getDrawable(context!!, R.drawable.default_background))
            .into(object : CustomTarget<Bitmap>(width, height){
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    row.imageDrawable = BitmapDrawable(resources, resource)
                    exereciseAdapter.notifyArrayItemRangeChanged(0, exereciseAdapter.size())
                }
                override fun onLoadCleared(placeholder: Drawable?) {}
            })


        val actionAdapter = ArrayObjectAdapter()
        actionAdapter.add(
            Action(
                ACTION_PLAY,
                resources.getString(R.string.page_exercise_btn_play)
            )
        )
        row.actionsAdapter = actionAdapter

        exereciseAdapter.add(row)
        val detailsPresenter = FullWidthDetailsOverviewRowPresenter(DetailsDescriptionPresenter())
        activity?.let {  detailsPresenter.backgroundColor = ContextCompat.getColor(it, R.color.selected_background) }
        detailsPresenter.onActionClickedListener = OnActionClickedListener { action ->
            exerciseData?.let {
                val param = HashMap<String, Any>()
                param[PagePlayer.PROGRAM_ID] = programID
                param[PagePlayer.EXERCISE] = it
                viewModel.pageChange(PageID.PLAYER, param)
            }
        }
        presenterSelector.addClassPresenter(DetailsOverviewRow::class.java, detailsPresenter)
    }

    private fun setupMotionListRow(exerciseList:List<MotionData>) {

        val listRowAdapter = ArrayObjectAdapter(ExercisePresenter())
        exerciseList.forEach { listRowAdapter.add(it) }
        val header = HeaderItem(0, context!!.getString(R.string.list_exercise_title))
        exereciseAdapter.add(ListRow(header, listRowAdapter))
        presenterSelector.addClassPresenter(ListRow::class.java, ListRowPresenter())
    }


    inner class ExercisePresenter:ItemPresenter(){
        init {
            cardWidth = context?.resources?.getDimension(R.dimen.program_list_width)?.toInt() ?: cardWidth
            cardHeight = context?.resources?.getDimension(R.dimen.program_list_height)?.toInt() ?: cardHeight
        }
        override fun getItemView(): ItemImageCardView = ItemMotion(context!!)
    }


    inner class DetailsDescriptionPresenter: AbstractDetailsDescriptionPresenter() {
        override fun onBindDescription(viewHolder: ViewHolder,  item: Any) {
            val exercise = item as ExerciseDetailData
            viewHolder.title.text = exercise.title
            viewHolder.subtitle.text = exercise.subtitle
            viewHolder.body.text = exercise.description
        }
    }

    companion object {
        const val PROGRAM_ID = "programID"
        const val EXERCISE = "exercise"
        const val SHARE_IMAGE_NAME = "${EXERCISE}shareName"

        private const val DETAIL_THUMB_WIDTH = 274
        private const val DETAIL_THUMB_HEIGHT = 274
        private const val ACTION_PLAY = 1L


    }


}