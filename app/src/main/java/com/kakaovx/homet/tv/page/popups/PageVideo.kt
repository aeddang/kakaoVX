package com.kakaovx.homet.tv.page.popups

import android.media.MediaDataSource
import android.media.browse.MediaBrowser
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kakaovx.homet.tv.page.viewmodel.BasePageViewModel
import com.kakaovx.homet.tv.page.viewmodel.PageError
import com.kakaovx.homet.tv.page.viewmodel.PageID
import com.kakaovx.homet.tv.page.viewmodel.VideoError
import com.kakaovx.homet.tv.store.api.wecandeo.MovieSignedData
import com.kakaovx.homet.tv.store.api.wecandeo.PlayData
import com.kakaovx.homet.tv.store.api.wecandeo.WecandeoApiType
import com.lib.page.PageNetworkStatus
import com.lib.page.PageObject
import com.lib.util.Log
import com.skeleton.module.ViewModelFactory
import com.skeleton.page.PagePlayBackFragment
import dagger.android.support.AndroidSupportInjection
import java.util.*
import javax.inject.Inject

class PageVideo : PagePlayBackFragment(){
    private val appTag = javaClass.simpleName

    data class VideoData(var path:String){
        var title:String? = null
        var subtitle:String? = null
    }

    companion object {
        const val VIDEO = "video"
        const val PLAY_DATA = "playData"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    protected lateinit var viewModel: BasePageViewModel
    private var videoData:VideoData? = null
    private var playData:PlayData? = null
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
        transportControlGlue.pause()
    }

    override fun onPageParams(params: Map<String, Any?>) {
        super.onPageParams(params)
        videoData = params[VIDEO] as? VideoData
        playData = params[PLAY_DATA] as? PlayData
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
    }

    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
        videoData ?: return
        transportControlGlue.title = videoData?.title
        transportControlGlue.subtitle = videoData?.subtitle
        transportControlGlue.playWhenPrepared()
        loadData()
    }

    private fun loadData(){
        if(playData != null) viewModel.repo.wecandeoManager.loadPlayData(this, playData!!)
        else loadVideo()
    }

    private fun loadVideo(){
        playerAdapter?.setDataSource(Uri.parse(videoData?.path))
    }

    override fun onPageVideoError(errorCode: Int, errorMessage: CharSequence?) {
        val error = PageError<VideoError>(VideoError.PLAY_BACK, errorCode.toString(), errorMessage.toString())
        val param = HashMap<String, Any>()
        param[PageErrorSurport.PAGE_ERROR] = error
        viewModel.openPopup(PageID.ERROR_SURPORT, param)
    }




}