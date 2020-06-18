package com.kakaovx.homet.tv.page.program
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
import com.kakaovx.homet.tv.page.component.items.ItemExercise
import com.kakaovx.homet.tv.page.component.items.ItemProgram
import com.kakaovx.homet.tv.page.error.PageError
import com.kakaovx.homet.tv.page.viewmodel.PageID
import com.kakaovx.homet.tv.page.viewmodel.PageViewModelEvent
import com.kakaovx.homet.tv.store.api.HomeTResponse
import com.kakaovx.homet.tv.store.api.homet.ExerciseData
import com.kakaovx.homet.tv.store.api.homet.HometApiType
import com.kakaovx.homet.tv.store.api.homet.ProgramData
import com.kakaovx.homet.tv.store.api.homet.ProgramDetailData
import com.lib.util.CommonUtil
import com.lib.util.Log
import com.skeleton.component.item.ItemImageCardView
import com.skeleton.component.item.ItemPresenter
import com.skeleton.module.ViewModelFactory
import com.skeleton.page.PageDetailsSupportFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.flow.flowOf
import java.util.*
import javax.inject.Inject

class PageProgram : PageDetailsSupportFragment(){
    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    protected lateinit var viewModel: PageProgramViewModel
    private val appTag = javaClass.simpleName

    private var programData:ProgramData? = null
    private lateinit var detailsBackground: DetailsSupportFragmentBackgroundController
    private lateinit var presenterSelector: ClassPresenterSelector
    private lateinit var programAdapter: ArrayObjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(PageProgramViewModel::class.java)
        pageViewModel = viewModel
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onPageParams(params: Map<String, Any?>) {
        super.onPageParams(params)
        programData = params[PROGRAM] as ProgramData
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        detailsBackground = DetailsSupportFragmentBackgroundController(this)
        presenterSelector = ClassPresenterSelector()
        programAdapter = ArrayObjectAdapter(presenterSelector)
        programData?.let{
            initializeBackground(it)
        }
        adapter = programAdapter
    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()
        val programID = programData?.programId
        programID ?: return
        viewModel.programDetailData.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer
            setupDetailsOverviewRow(it) })
        viewModel.exerciseList.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer
            setupExerciseListRow(it) })
        viewModel.programList.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer
            setupRecentProgramRow(it)  })

        viewModel.loadData(programID)

        onItemViewClickedListener = OnItemViewClickedListener { itemViewHolder , item, _, _ -> onItemClicked(item) }
    }

    private fun onItemClicked(item:Any?){
        val program = item as? ProgramData
        program?.let {
            Log.i(appTag, program.toString())
        }
        val exercise = item as? ExerciseData
        exercise?.let {
            Log.i(appTag, exercise.toString())
        }
    }

    private fun initializeBackground(data:ProgramData) {
        detailsBackground.enableParallax()
        context ?: return
        Glide.with(this).asBitmap()
            .load(data.thumbnail).centerCrop()
            .error(ContextCompat.getDrawable(context!!, R.drawable.default_background))
            .into(object : CustomTarget<Bitmap>(){
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    detailsBackground.coverBitmap = resource
                    programAdapter.notifyArrayItemRangeChanged(0, programAdapter.size())
                }
                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun setupDetailsOverviewRow(data:ProgramDetailData) {
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
                    programAdapter.notifyArrayItemRangeChanged(0, programAdapter.size())
                }
                override fun onLoadCleared(placeholder: Drawable?) {}
            })


        val actionAdapter = ArrayObjectAdapter()
        actionAdapter.add(
            Action(
                ACTION_PLAY,
                resources.getString(R.string.page_program_btn_play)
            )
        )
        row.actionsAdapter = actionAdapter

        programAdapter.add(row)
        val detailsPresenter = FullWidthDetailsOverviewRowPresenter(DetailsDescriptionPresenter())
        activity?.let {  detailsPresenter.backgroundColor = ContextCompat.getColor(it, R.color.selected_background) }
        detailsPresenter.onActionClickedListener = OnActionClickedListener { action ->

        }
        presenterSelector.addClassPresenter(DetailsOverviewRow::class.java, detailsPresenter)

    }



    private fun setupExerciseListRow(exerciseList:List<ExerciseData>) {

        val listRowAdapter = ArrayObjectAdapter(ExercisePresenter())
        exerciseList.forEach { listRowAdapter.add(it) }
        val header = HeaderItem(0, context!!.getString(R.string.list_exercise_title))
        programAdapter.add(ListRow(header, listRowAdapter))
        presenterSelector.addClassPresenter(ListRow::class.java, ListRowPresenter())
    }

    private fun setupRecentProgramRow(exerciseList:List<ProgramData>) {
        val listRowAdapter = ArrayObjectAdapter(ProgramPresenter())
        exerciseList.forEach { listRowAdapter.add(it) }
        val header = HeaderItem(0, context!!.getString(R.string.list_recent_title))
        programAdapter.add(ListRow(header, listRowAdapter))
        presenterSelector.addClassPresenter(ListRow::class.java, ListRowPresenter())
    }

    inner class ExercisePresenter:ItemPresenter(){
        override fun getItemView(): ItemImageCardView = ItemExercise(context!!)
    }
    inner class ProgramPresenter:ItemPresenter(){
        override fun getItemView(): ItemImageCardView = ItemProgram(context!!)
    }

    inner class DetailsDescriptionPresenter: AbstractDetailsDescriptionPresenter() {
        override fun onBindDescription(viewHolder: ViewHolder,  item: Any) {
            val program = item as ProgramDetailData
            viewHolder.title.text = program.title
            viewHolder.subtitle.text = program.subtitle
            viewHolder.body.text = program.description
        }
    }

    companion object {
        const val PROGRAM = "program"
        const val SHARE_IMAGE_NAME = "${PROGRAM}shareName"

        private const val DETAIL_THUMB_WIDTH = 274
        private const val DETAIL_THUMB_HEIGHT = 274

        private const val ACTION_PLAY = 1L


    }


}