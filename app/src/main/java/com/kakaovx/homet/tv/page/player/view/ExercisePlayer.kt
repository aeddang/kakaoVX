package com.kakaovx.homet.tv.page.player.view

import android.content.Context
import android.util.AttributeSet
import androidx.lifecycle.Observer
import com.google.android.exoplayer2.ui.PlayerView
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.player.PagePlayerViewModel
import com.kakaovx.homet.tv.page.player.model.*
import com.kakaovx.homet.tv.page.popups.PageErrorSurport
import com.kakaovx.homet.tv.page.viewmodel.PageError
import com.kakaovx.homet.tv.page.viewmodel.PageID
import com.kakaovx.homet.tv.page.viewmodel.VideoError
import com.kakaovx.homet.tv.store.api.wecandeo.PlayData
import com.kakaovx.homet.tv.store.api.wecandeo.WecandeoApiType
import com.lib.util.Log

import com.skeleton.component.player.ExoVideoPlayer
import kotlinx.android.synthetic.main.cp_exercise_player.view.*
import java.util.HashMap

class ExercisePlayer : ExoVideoPlayer, PlayerChildComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
    private val appTag = javaClass.simpleName
    override fun getAppName(): Int = R.string.app_name
    override fun getPlayerView(): PlayerView = playerView
    override fun getLayoutResID(): Int = R.layout.cp_exercise_player
    private var playerViewModel:PagePlayerViewModel? = null

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        playerViewModel = null
        exercise = null
    }

    override fun onPlayerViewModel(playerViewModel:PagePlayerViewModel){
        this.playerViewModel = playerViewModel
        lifecycleOwner?.let { owner ->
            playerViewModel.repo.wecandeoManager.success.observe(
                owner,
                Observer { data ->
                    if (data?.type != WecandeoApiType.PLAY_DATA) return@Observer
                    val pass = data.data as? String
                    pass ?: return@Observer
                    videoPath = pass
                    loadVideo()
                })

            playerViewModel.repo.wecandeoManager.error.observe(owner, Observer { e ->
                e ?: return@Observer
                val param = HashMap<String, Any>()
                param[PageErrorSurport.API_ERROR] = e
                param[PageErrorSurport.PAGE_EVENT_ID] = appTag
                playerViewModel.openPopup(PageID.ERROR_SURPORT, param)
            })

            playerViewModel.observable.event.observe(owner, Observer { evt ->
                if (evt?.id != PageID.ERROR_SURPORT.value) return@Observer
                if( evt.type.id != appTag) return@Observer
                val type = evt.data as? PageErrorSurport.ErrorActionType?
                type ?: return@Observer
                when (type) {
                    PageErrorSurport.ErrorActionType.Retry -> loadData()
                    PageErrorSurport.ErrorActionType.Confirm -> playerViewModel.goBackImmediately()
                    else -> { }
                }
            })

            playerViewModel.player.uiEvent.observe(owner, Observer { evt ->
                when(evt){
                    PlayerUIEvent.Pause -> pause()
                    PlayerUIEvent.Resume -> resume()
                    PlayerUIEvent.Seek -> seek(evt.value)
                    PlayerUIEvent.SeekMove -> seekMove(evt.value)
                }
            })
        }
    }
    private var exercise:Exercise? = null
    private var currentIdx:Int = 0
    override fun onExercise(exercise:Exercise){
        this.exercise = exercise
        lifecycleOwner?.let { owner->
            exercise.movieObservable.observe(owner, Observer { onMovie(it)})
            exercise.changeFlagObservable.observe(owner, Observer {onFlag(it)})
        }

    }

    private var isMovieReset = false
    private fun onMovie(movie:Movie){
        exercise ?: return
        lifecycleOwner?.let { owner ->
            val currentMovieNum = movie.motionMovieUrls?.size ?: 1
            currentIdx =
                if (currentMovieNum > currentIdx) currentIdx else movie.multiViewIndex.value ?: 0
            movie.multiViewIndex.value = currentIdx
            pause()
            isMovieReset = true
            movie.multiViewIndex.observe(owner, Observer { idx ->
                currentIdx = idx
                exercise ?: return@Observer
                playData = PlayData(movie.screenPath).apply {
                    mediaAccessApiUrl = exercise!!.info.mediaAccessApiUrl
                    mediaAccessApiKey = exercise!!.info.mediaAccessApiKey
                    mediaAccesskey = movie.mediaAccesskey
                }
                loadData()
            })
        }
    }
    private fun onFlag(flag:Flag){
        exercise ?: return
        Log.d(appTag, "onFlag")
        playerViewModel?.player?.playerStatus = PlayerStatus.Stop
        if(flag.type == FlagType.Break) {
            pause()
            return
        }
        lifecycleOwner?.let {
            Log.d(appTag, "flag.isMovieChange ${flag.isMovieChange} ${flag.getFlagActionTitle()} ")
            if(flag.isMovieChange || exercise!!.needSeek(flag.progressTime)) seek(flag.movieStartTime)
            else {
                playerViewModel?.player?.playerStatus = PlayerStatus.Resume
                Log.d(appTag, "onFlag completed")
            }
        }
    }

    override fun onInit() {
        super.onInit()
        exercise?.synchronizedFlag()
        playerViewModel?.apply{
            player.streamEvent.value = PlayerStreamEvent.Loaded
        }

    }

    override fun onCompleted() {
        super.onCompleted()
        exercise?.completeMovie()
        playerViewModel?.apply{
            player.playerStatus = PlayerStatus.Completed
            player.streamEvent.value = PlayerStreamEvent.Completed
            player.streamStatus.value = PlayerStreamStatus.Stop
        }
    }


    override fun pause() {
        super.pause()
        playerViewModel?.apply{
            if(player.playerStatus == PlayerStatus.Resume)  player.playerStatus = PlayerStatus.Pause
            player.streamEvent.value = PlayerStreamEvent.Paused
        }
    }

    override fun resume() {
        super.resume()
        playerViewModel?.apply{

            if(player.playerStatus == PlayerStatus.Pause) player.playerStatus = PlayerStatus.Resume
            player.streamEvent.value = PlayerStreamEvent.Resumed
        }
    }

    override fun onBuffering() {
        super.onBuffering()
        playerViewModel?.apply{
            player.streamStatus.value = PlayerStreamStatus.Buffering
        }
    }

    override fun onSeekProcessed() {
        super.onSeekProcessed()
        playerViewModel?.apply{
            player.playerStatus = PlayerStatus.Resume
            player.streamEvent.value = PlayerStreamEvent.Seeked
            Log.d(appTag, "onFlag seeked completed")
        }
    }

    override fun onTimeChange(t: Long) {
        if(playerViewModel?.player?.playerStatus == PlayerStatus.Stop) return
        super.onTimeChange(t)
    }

    override fun onReady() {
        super.onReady()
        playerViewModel?.apply{
            player.streamStatus.value = PlayerStreamStatus.Playing
        }
    }

    override fun onError(e: Any?) {
        super.onError(e)
        val param = HashMap<String, Any>()
        param[PageErrorSurport.PAGE_ERROR] = PageError(VideoError.PLAY_BACK, null, null)
        param[PageErrorSurport.PAGE_EVENT_ID] = appTag
        playerViewModel?.openPopup(PageID.ERROR_SURPORT, param)

        playerViewModel?.apply{
            player.playerStatus = PlayerStatus.Error
            player.streamEvent.value = PlayerStreamEvent.Error
            player.streamStatus.value = PlayerStreamStatus.Stop
        }
    }



    private var videoPath: String = ""
    private var playData: PlayData? = null
    private fun loadData(){
        playerViewModel ?: return
        lifecycleOwner ?: return
        playerViewModel?.apply{
            player.streamEvent.value = PlayerStreamEvent.Load
        }
        if(playData != null) playerViewModel!!.repo.wecandeoManager.loadPlayData(lifecycleOwner!!, playData!!)
        else loadVideo()
    }

    private fun loadVideo(){
        val t = if( isMovieReset ) 0L else currentPlayer?.currentPosition ?: 0
        isMovieReset = false
        load(videoPath, t)
        resume()
    }


}