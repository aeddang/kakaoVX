package com.kakaovx.homet.tv.page.player

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.component.factory.TTSFactory
import com.kakaovx.homet.tv.page.player.model.*
import com.kakaovx.homet.tv.page.player.view.PlayerChildComponent
import com.kakaovx.homet.tv.page.viewmodel.PageID
import com.kakaovx.homet.tv.store.api.homet.ExerciseData
import com.lib.page.PageFragmentCoroutine
import com.lib.page.PageView
import com.lib.util.Log
import com.lib.util.animateAlpha
import com.lib.util.animateY
import com.skeleton.component.alert.CustomDialog
import com.skeleton.component.player.PlayBack
import com.skeleton.component.player.PlayBackTimeDelegate
import com.skeleton.module.ViewModelFactory
import dagger.android.support.AndroidSupportInjection

import kotlinx.android.synthetic.main.page_player.*

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

class PagePlayer : PageFragmentCoroutine(){
    private val appTag = javaClass.simpleName
    override fun getLayoutResID(): Int = R.layout.page_player

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var viewModel: PagePlayerViewModel

    @Inject
    lateinit var ttsFactory: TTSFactory

    private var programID:String = ""
    private var exerciseData: ExerciseData? = null

    override val pageChileren = ArrayList<PageView>()
    private var pageList:PagePlayerList? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(PagePlayerViewModel::class.java)
        pageViewModel = viewModel
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        pageList = null
    }

    override fun onResume() {
        super.onPause()
        player.onResume()
    }

    override fun onPause() {
        super.onPause()
        player.onPause()
    }

    override fun onPageParams(params: Map<String, Any?>) {
        super.onPageParams(params)
        programID = params[PROGRAM_ID] as? String ?: programID
        exerciseData = params[EXERCISE] as ExerciseData
    }


    override val hasBackPressAction: Boolean
        get(){
             if(!viewModel.isExitChecked) {
                 context ?: return false
                 CustomDialog.makeDialog(context!!, null, R.string.page_player_exit)
                     .setNegativeButton(R.string.btn_action_cancel)
                     {  _,_ -> }
                     .setPositiveButton(R.string.btn_action_confirm)
                     {  _,_ ->
                         viewModel.goBackImmediately()
                     }
                     .show()
                 return true
             }
             return super.hasBackPressAction
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        pageChileren.add(player)
        pageChileren.add(videoProgress)
        pageChileren.add(exerciseProgress)
        pageChileren.add(exerciseInfo)
        pageChileren.add(breakTime)
        if( pageList == null){
            try {
                val supportFragmentManager = childFragmentManager
                val transaction = supportFragmentManager.beginTransaction()
                val page = viewModel.repo.pageProvider.getPageObject(PageID.PLAYER_LIST)
                val fragment = viewModel.repo.pageProvider.getPageView(page)
                pageList = fragment as PagePlayerList
                pageList?.onPlayerViewModel(viewModel)

                transaction.add(R.id.listArea, fragment.pageFragment)
                transaction.commit()
            }catch(e:IllegalStateException){
            }
        }

        super.onViewCreated(view, savedInstanceState)
        breakTime.exitFocusView = btnPlayStop
        exerciseInfo.exitFocusView = btnPlayStop
        pageList?.exitFocusView = btnPlayStop
        viewModel.player.playerListStatus = PlayerListStatus.Initate
        btnPlayStop.requestFocus()
    }

    override fun onGlobalLayout() {
        super.onGlobalLayout()
        view?.let {
            val layout = listArea.layoutParams as ViewGroup.MarginLayoutParams
            layout.bottomMargin = - it.height
            layout.height = it.height
            listArea.layoutParams = layout
        }

    }

    override fun onTransactionCompleted() {
        super.onTransactionCompleted()

    }

    private fun onExerciseStart(exercise:Exercise){
        if(exercise.info.isRelayPlay){
            context ?: return
            CustomDialog.makeDialog(context!!, null, R.string.page_player_relay_confirm)
                .setNegativeButton(R.string.page_player_btn_replay)
                {  _,_ ->
                    exercise.start(false)
                }
                .setPositiveButton(R.string.page_player_btn_reset)
                { _,_ ->
                    exercise.info.isRelayPlay = false
                    exercise.start(false)
                }.show()
        }else{
            exercise.start(false)
        }
    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()
        pageChileren.map { it as? PlayerChildComponent }.forEach {
            it?.onPlayerViewModel(viewModel)
            it?.onTTSFactory(ttsFactory)
        }

        viewModel.exercise.observe(this, Observer { exercise ->
            exercise ?: return@Observer
            pageChileren.map { it as? PlayerChildComponent }.forEach { it?.onExercise(exercise) }
            pageList?.onExercise(exercise)
            viewModel.player.playerListStatus = PlayerListStatus.Playing
            player.setOnPlayTimeListener(object : PlayBackTimeDelegate {
                override fun onTimeChanged(player: PlayBack, t: Long) {
                    exercise.changeVideoTime(t)
                }
            })
            var currentMovie:Movie? = null
            exercise.movieObservable.observe(this, Observer {
                currentMovie?.disposeLifecycleOwner(this)
                currentMovie = it
            })
            exercise.changeFlagObservable.observe(this, Observer {

            })
            exercise.changedFlagObservable.observe(this, Observer {flag->
                //viewModel.player.uiEvent.value = PlayerUIEvent.ListHidden
                flag.speech?.let { ttsFactory.effect(it) }
            })

            exercise.currentResultObservable.observe(this, Observer {

            })

            exercise.resultObservable.observe(this, Observer {
                viewModel.putExerciseProgressResult(it)
            })

            exercise.completedObservable.observe(this, Observer {
                viewModel.player.playerStatus = PlayerStatus.Completed
                viewModel.player.streamStatus.value = PlayerStreamStatus.Stop
                viewModel.putExerciseCompleted(it.result)
            })
            onExerciseStart(exercise)

        })

        viewModel.player.streamEvent.observe(this, Observer {evt->
            when(evt){
                PlayerStreamEvent.Resumed ->{ btnPlayStop.setImageResource(R.drawable.ic_pause) }
                PlayerStreamEvent.Paused ->{ btnPlayStop.setImageResource(R.drawable.ic_resume) }
                else -> {}
            }
        })

        viewModel.player.streamStatus.observe(this, Observer {evt->
            when(evt){
                PlayerStreamStatus.Buffering -> loading()
                else -> loaded()
            }
        })


        viewModel.player.uiEvent.observe(this, Observer {evt->
            when(evt){
                PlayerUIEvent.ListView -> openList()
                PlayerUIEvent.ListHidden -> hideList()
                PlayerUIEvent.UIUse -> viewUI(false)
                PlayerUIEvent.UIView -> viewUI()
                PlayerUIEvent.UIHidden -> hideUI()
                else -> {}
            }
        })

        arrayOf(btnPlayStop, btnPrev, btnNext).forEach {
            it.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) viewModel.player.uiEvent.value = PlayerUIEvent.UIView
            }
            it.setOnClickListener {btn->
                if(viewModel.player.playerUIStatus == PlayerUiStatus.Hidden) {
                    viewModel.player.uiEvent.value = PlayerUIEvent.UIView
                    return@setOnClickListener
                }
                when(btn){
                    btnPlayStop ->{
                        when(viewModel.player.playerStatus){
                            PlayerStatus.Resume -> viewModel.player.uiEvent.value = PlayerUIEvent.Pause
                            PlayerStatus.Pause -> viewModel.player.uiEvent.value = PlayerUIEvent.Resume
                            else ->{}
                        }
                    }
                    btnPrev -> viewModel.exercise.value?.prev()
                    btnNext -> viewModel.exercise.value?.next()
                    else ->{}
                }
                viewModel.player.uiEvent.value = PlayerUIEvent.UIView
            }
        }

        exerciseData?.let {
            viewModel.loadData(
                programID,
                it.exerciseId ?: "",
                it.roundId ?: "",
                it.movieType ?: ""
            )
        }

        scope.launch() {
            delay(Exercise.MIN_EXERCISE_TIME)
            viewModel.putExerciseRecord()
        }
    }

    private fun openList(){
        context ?: return
        if( viewModel.player.playerListStatus == PlayerListStatus.ListSearch) return
        viewModel.player.playerListStatus = PlayerListStatus.ListSearch
        viewModel.player.uiEvent.value = PlayerUIEvent.UIUse
    }

    private fun hideList(){
        Log.i(appTag, "openList ${viewModel.player.playerListStatus}")
        if( viewModel.player.playerListStatus == PlayerListStatus.Playing) return
        viewModel.player.playerListStatus = PlayerListStatus.Playing
        viewModel.player.uiEvent.value = PlayerUIEvent.UIView

    }


    private var autoHiddenJob:Job? = null

    private fun viewUI(isAutoHidden:Boolean = true){
        Log.i(appTag, "viewUI $isAutoHidden")
        autoHiddenJob?.cancel()
        if(isAutoHidden){
            autoHiddenJob = scope.launch {
                delay(3000)
                viewModel.player.uiEvent.value = PlayerUIEvent.UIHidden
            }
        }

        val willStatus = if(isAutoHidden) PlayerUiStatus.View else PlayerUiStatus.Use
        if( viewModel.player.playerUIStatus == willStatus) return
        viewModel.player.playerUIStatus = willStatus

        var pos = context!!.resources.getDimension(R.dimen.page_player_ui_pos).roundToInt()
        var opc = 1.0f

        if( viewModel.player.playerListStatus == PlayerListStatus.ListSearch){
            pos = 0
            opc = 0.0f
        }
        btnArea.animateAlpha(opc, false)
        uiArea.animateY(pos, true).apply {
            interpolator = AccelerateInterpolator()
            uiArea.startAnimation(this)
        }
    }

    private fun hideUI(){
        Log.i(appTag, "hideUI")
        autoHiddenJob?.cancel()
        if( viewModel.player.playerUIStatus == PlayerUiStatus.Hidden) return
        viewModel.player.playerUIStatus = PlayerUiStatus.Hidden
        btnArea.animateAlpha(0.0f, false)
        val pos = context!!.resources.getDimension(R.dimen.page_player_ui_hidden_pos).roundToInt()
        uiArea.animateY(pos, true).apply {
            interpolator = AccelerateInterpolator()
            uiArea.startAnimation(this)
        }
    }

    private fun loading(){
        loadingSpinner.isLoading = true
    }
    private fun loaded(){
        loadingSpinner.isLoading = false
    }

    companion object {
        const val PROGRAM_ID = "programID"
        const val EXERCISE = "exercise"

    }

}