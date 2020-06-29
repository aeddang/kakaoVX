package com.kakaovx.homet.tv.page.popups

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.viewmodel.BasePageViewModel
import com.kakaovx.homet.tv.page.viewmodel.PageID
import com.kakaovx.homet.tv.page.viewmodel.Video
import com.kakaovx.homet.tv.page.viewmodel.VideoData
import com.kakaovx.homet.tv.store.api.wecandeo.PlayData
import com.kakaovx.homet.tv.store.api.wecandeo.WecandeoApiType
import com.lib.page.PageFragmentCoroutine
import com.lib.util.Log
import com.skeleton.component.alert.CustomDialog
import com.skeleton.module.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.page_video_exo.*
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

    override fun onPause() {
        super.onPause()
        player.pause()
    }

    override fun onPageParams(params: Map<String, Any?>) {
        super.onPageParams(params)
        videoData = params[Video.VIDEO] as? VideoData
        playData = params[Video.PLAY_DATA] as? PlayData
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
                PageErrorSurport.ErrorActionType.Confirm -> pageObject?.let{ viewModel.presenter.closePopup(it)}
                else ->{}
            }
        })

        btnTest0.setOnClickListener {
            CustomDialog.makeDialog(context!!,"${it.tag}","test" ).show()
        }
        btnTest0.requestFocus()
        this.view?.setOnKeyListener { v, keyCode, event ->
            CustomDialog.makeDialog(context!!,"event","keyCode ${keyCode}" ).show()
            true
        }
    }

    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
        videoData ?: return
        //transportControlGlue.title = videoData?.title
        //transportControlGlue.subtitle = videoData?.subtitle
        //transportControlGlue.playWhenPrepared()
        loadData()
    }

    private fun loadData(){
        if(playData != null) viewModel.repo.wecandeoManager.loadPlayData(this, playData!!)
        else loadVideo()
    }

    private fun loadVideo(){
        player.load(videoData?.path ?: "")
        player.resume()
    }


}