package com.kakaovx.homet.tv.page.popups

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.viewmodel.BasePageViewModel
import com.kakaovx.homet.tv.page.viewmodel.PageID
import com.kakaovx.homet.tv.page.viewmodel.Video
import com.kakaovx.homet.tv.page.viewmodel.VideoData
import com.kakaovx.homet.tv.store.api.homet.MotionData
import com.kakaovx.homet.tv.store.api.wecandeo.PlayData
import com.kakaovx.homet.tv.store.api.wecandeo.WecandeoApiType
import com.kakaovx.homet.tv.util.secToLong
import com.lib.page.PageFragmentCoroutine
import com.lib.util.Log
import com.lib.util.animateAlpha
import com.skeleton.component.alert.CustomToast
import com.skeleton.component.player.PlayBack
import com.skeleton.component.player.PlayBackDelegate
import com.skeleton.component.player.PlayBackTimeDelegate
import com.skeleton.module.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.page_video_exo.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.HashMap
import javax.inject.Inject


class PageVideoExo : PageFragmentCoroutine(){
    override fun getLayoutResID(): Int = R.layout.page_video_exo

    private val appTag = javaClass.simpleName

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    protected lateinit var viewModel: BasePageViewModel
    private var videoData: VideoData? = null
    private var playData: PlayData? = null
    private var motionData: MotionData? = null
    private var motionDatas: List<MotionData>? = null
    private var currentIdx: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(BasePageViewModel::class.java)
        pageViewModel = viewModel
    }

    override fun onDestroyView() {
        super.onDestroyView()
        videoData = null
        playData = null
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
        videoData = params[Video.VIDEO] as? VideoData
        playData = params[Video.PLAY_DATA] as? PlayData
        currentIdx = params[Video.PLAY_DATA_INDEX] as? Int ?: 0
        motionDatas  = params[Video.PLAY_DATAS] as? List<MotionData>
        motionDatas?.let {
            motionData = it[currentIdx]
        }

    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()
        viewModel.repo.wecandeoManager.success.observe(viewLifecycleOwner ,Observer { data ->
            if(data?.type != WecandeoApiType.PLAY_DATA) return@Observer
            val pass = data.data as? String
            pass ?: return@Observer
            Log.i(appTag, pass)
            videoData?.path = pass
            loadVideo()
        })

        viewModel.repo.wecandeoManager.error.observe(viewLifecycleOwner ,Observer { e ->
            e ?: return@Observer
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
                PageErrorSurport.ErrorActionType.Confirm -> pageObject?.let{ viewModel.presenter.goBack()}
                else ->{}
            }
        })

        player.setOnPlayTimeListener(object : PlayBackTimeDelegate {
            override fun onTimeChanged(player: PlayBack, t: Long) {
                videoData ?: return
                if(t >= videoData!!.endTime){
                    player.seek(videoData!!.startTime)
                }
            }
        })

        player.setOnPlayerListener(object : PlayBackDelegate {
            override fun onLoad(player: PlayBack, duration:Long ){
                btnPlayStop.requestFocus()
            }
            override fun onPlay(player: PlayBack){
                btnPlayStop.setImageResource(R.drawable.ic_pause)
            }
            override fun onStop(player: PlayBack ){
                btnPlayStop.setImageResource(R.drawable.ic_resume)
            }
            override fun onBuffering(player: PlayBack){
                loadingSpinner.isLoading = true
            }
            override fun onReady(player: PlayBack){
                loadingSpinner.isLoading = false
            }
        })

        if(motionDatas == null || motionDatas!!.isEmpty()){
            btnPrev.visibility = View.GONE
            btnNext.visibility = View.GONE
        } else{
            btnPrev.visibility = View.VISIBLE
            btnNext.visibility = View.VISIBLE
        }

        arrayOf(btnPlayStop, btnPrev, btnNext).forEach {
            it.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) viewUI()
            }
            it.setOnClickListener {btn->
                if(!isView) {
                    viewUI()
                    return@setOnClickListener
                }
                when(btn){
                    btnPlayStop ->{
                        if(player.playWhenReady) player.pause()
                        else player.resume()
                    }
                    btnPrev -> prev()
                    btnNext -> next()
                    else ->{}
                }
            }
        }
    }

    private fun prev(){
        context ?: return
        if(currentIdx == 0){
            CustomToast.makeToast(context!!, R.string.page_player_first).show()
            return
        }
        currentIdx --
        createNewData()
    }
    private fun next(){
        context ?: return
        motionDatas ?: return
        if(currentIdx >= motionDatas!!.size-1){
            CustomToast.makeToast(context!!, R.string.page_player_last).show()
            return
        }
        currentIdx ++
        createNewData()
    }

    private fun createNewData(){
        motionDatas ?: return
        if(currentIdx < 0) return
        if(motionDatas!!.size <= currentIdx) return
        val motion = motionDatas!![currentIdx]
        val newVideoData = VideoData("")
        newVideoData.title = motion.title
        newVideoData.subtitle = motion.getSubTitle(context)
        newVideoData.startTime = motion.timerStart?.secToLong() ?: 0
        newVideoData.endTime = motion.timerEnd?.secToLong() ?: 0
        val newPlayData = PlayData(motion.movieUrl ?: "")
        newPlayData.mediaAccessApiUrl = playData?.mediaAccessApiUrl
        newPlayData.mediaAccessApiKey = playData?.mediaAccessApiKey
        newPlayData.mediaAccesskey = motion.mediaAccesskey
        motionData = motion
        videoData = newVideoData
        playData = newPlayData
        loadData()
    }

    private var autoHiddenJob: Job? = null
    private var isView = false
    private fun viewUI(isAutoHidden:Boolean = true){
        Log.i(appTag, "viewUI $isAutoHidden")
        autoHiddenJob?.cancel()
        if(isAutoHidden){
            autoHiddenJob = scope.launch {
                delay(3000)
                hideUI()
            }
        }
        isView = true
        btnArea.animateAlpha(1.0f, false)
        infoBox.animateAlpha(1.0f, false)
    }

    private fun hideUI(){
        Log.i(appTag, "hideUI")
        autoHiddenJob?.cancel()
        isView = false
        btnArea.animateAlpha(0.0f, false)
        infoBox.animateAlpha(0.0f, false)
    }

    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
        loadData()
    }

    private fun loadData(){
        motionData?.let {
            val progress = "${currentIdx+1}"
            val text =  SpannableString("$progress/${motionDatas!!.size}")
            text.setSpan(StyleSpan(Typeface.BOLD), 0, progress.length, 0)
            textProgress.text = text
            textTitle.text = it.title
        }

        if(playData != null) viewModel.repo.wecandeoManager.loadPlayData(this, playData!!)
        else loadVideo()
    }

    private fun loadVideo(){
        videoData?.let {
            player.load(it.path, it.startTime)
            player.resume()
        }

    }


}