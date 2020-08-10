package com.kakaovx.homet.tv.page.player
import android.os.Bundle
import android.view.View
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.leanback.widget.BrowseFrameLayout.OnFocusSearchListener
import androidx.lifecycle.Observer
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.player.model.*
import com.kakaovx.homet.tv.page.player.view.ItemFlag
import com.kakaovx.homet.tv.page.player.view.ItemMultiView
import com.kakaovx.homet.tv.store.api.homet.MovieUrlData
import com.lib.util.Log
import com.skeleton.component.item.ItemImageCardView
import com.skeleton.component.item.ItemPresenter
import com.skeleton.page.PageBrowseSupportFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PagePlayerList : PageBrowseSupportFragment(){

    private val appTag = javaClass.simpleName
    private var viewModel: PagePlayerViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUIElements()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentMovie?.disposeLifecycleOwner(this)
        exercise?.movieObservable?.removeObservers(this)
        exercise = null
        exitFocusView = null
        currentMovie = null
        rowsAdapter= null
        multiViewRowAdapter= null
        initContent = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleView.visibility = View.GONE
        workaroundFocus(view)
    }

    fun onPlayerViewModel(playerViewModel:PagePlayerViewModel){
        viewModel = playerViewModel
        playerViewModel.player.uiEvent.observe(this, Observer {evt->

        })
    }

    private var currentMovie:Movie? = null
        set(value){
            field?.let { it.disposeLifecycleOwner(this) }
            field = value
            value?.lifecycleOwner = this
        }
    private var exercise:Exercise? = null
    fun onExercise(exercise: Exercise){
        this.exercise = exercise
        setupExerciseRow(exercise.getListFrags())
        exercise.movieObservable.observe(this, Observer { movie->
            currentMovie = movie
            val num = movie.motionMovieUrls?.size ?: 0
            if (num <= 1) return@Observer
            val listRowAdapter = ArrayObjectAdapter(MoviePresenter())
            movie.motionMovieUrls!!.forEachIndexed{ idx,mv ->
                mv.idx = idx
                listRowAdapter.add(mv) }
            val header = HeaderItem(0, context!!.getString(R.string.page_player_multiview_list))
            multiViewRowAdapter = ListRow(header, listRowAdapter)
        })
    }

    private fun setupUIElements() {
        headersState = BrowseSupportFragment.HEADERS_DISABLED
        isHeadersTransitionOnBackEnabled = false
    }

    var exitFocusView:View? = null
    private fun workaroundFocus(view: View) {
        val browseFrameLayout: BrowseFrameLayout =
            view.findViewById(androidx.leanback.R.id.browse_frame)
        val origin = browseFrameLayout.onFocusSearchListener
        browseFrameLayout.onFocusSearchListener =
            OnFocusSearchListener { focused: View?, direction: Int ->
                when(direction){
                    View.FOCUS_BACKWARD -> Log.i(appTag, "FOCUS_BACKWARD ${focused.toString()}")
                    View.FOCUS_FORWARD -> Log.i(appTag, "FOCUS_UP ${focused.toString()}")
                    View.FOCUS_UP -> Log.i(appTag, "FOCUS_BACKWARD ${focused.toString()}")
                    View.FOCUS_DOWN -> Log.i(appTag, "FOCUS_DOWN ${focused.toString()}")
                    View.FOCUS_LEFT -> Log.i(appTag, "FOCUS_LEFT ${focused.toString()}")
                    View.FOCUS_RIGHT -> Log.i(appTag, "FOCUS_RIGHT ${focused.toString()}")
                   else -> Log.i(appTag, "FOCUS ${direction.toString()} ${focused.toString()}")
                }

                if (direction == View.FOCUS_UP || direction == View.FOCUS_BACKWARD) {
                    viewModel?.player?.uiEvent?.value = PlayerUIEvent.ListHidden
                    return@OnFocusSearchListener exitFocusView
                }
                else if(direction == View.FOCUS_LEFT){
                    return@OnFocusSearchListener initContent
                }
                else {
                        return@OnFocusSearchListener  origin.onFocusSearch(focused, direction)
                }
            }
    }

    override fun onSuperBackPressAction() {
        viewModel?.repo?.pagePresenter?.superBackPressAction()
    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()
        onItemViewSelectedListener = OnItemViewSelectedListener { _, item, _, _ -> onItemSelected(item) }
        onItemViewClickedListener = OnItemViewClickedListener { _ , item, _, _ -> onItemClicked(item) }
    }

    private fun onItemSelected(item:Any?){
        val flag = item as? Flag
        flag ?: return
    }
    private fun onItemClicked(item:Any?){
        val flag = item as? Flag
        val movie = item as? MovieUrlData
        flag?.let {
            exercise?.changeFrag(it.idx)
        }
        movie?.let {
            currentMovie?.multiViewIndex?.value = it.idx
        }
        viewModel?.player?.uiEvent?.value = PlayerUIEvent.ListHidden
        exitFocusView?.requestFocus()

    }

    private var rowsAdapter:ArrayObjectAdapter? = null
    private var multiViewRowAdapter:ListRow? = null
    set(value) {
        field?.let{ rowsAdapter?.remove(it) }
        field = value
        value?.let { rowsAdapter?.add(it)}
    }
    private fun setupExerciseRow(list:List<Flag>) {
        initContent = null
        rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        val listRowAdapter = ArrayObjectAdapter(MotionPresenter())
        list.forEach {
            it.lifecycleOwner = this
            listRowAdapter.add(it) }
        val header = HeaderItem(0, context!!.getString(R.string.page_player_list))
        rowsAdapter?.add(ListRow(header, listRowAdapter))
        adapter = rowsAdapter
        exitFocusView?.requestFocus()
    }



    private var initContent:ItemFlag? = null
    inner class MotionPresenter:ItemPresenter(){
        init {
            cardWidth = context?.resources?.getDimension(R.dimen.flag_list_width)?.toInt() ?: cardWidth
            cardHeight = context?.resources?.getDimension(R.dimen.flag_list_height)?.toInt() ?: cardHeight
        }
        override fun getItemView(): ItemImageCardView{
            val item = ItemFlag(context!!)
            if(initContent == null) initContent = item
            item.setOnFocusChangeListener { v, hasFocus ->
                viewModel?.player?.let{
                    if(hasFocus)  {
                        if( it.playerListStatus != PlayerListStatus.Initate) it.uiEvent.value = PlayerUIEvent.ListView
                    }
                }

            }
            return item
        }
    }

    inner class MoviePresenter:ItemPresenter(){
        init {
            cardWidth = context?.resources?.getDimension(R.dimen.flag_list_width)?.toInt() ?: cardWidth
            cardHeight = context?.resources?.getDimension(R.dimen.flag_list_height)?.toInt() ?: cardHeight
        }
        override fun getItemView(): ItemImageCardView {
            val item = ItemMultiView(context!!)
            item.movie = currentMovie

            item.setOnFocusChangeListener { v, hasFocus ->
                viewModel?.player?.let{
                    if(hasFocus)  {
                        if( it.playerListStatus != PlayerListStatus.Initate) it.uiEvent.value = PlayerUIEvent.ListView
                    }
                }
            }

            return item
        }
    }



}