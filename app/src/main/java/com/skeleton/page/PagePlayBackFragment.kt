package com.skeleton.page

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.MediaPlayerAdapter
import androidx.leanback.media.PlaybackGlue
import androidx.leanback.media.PlaybackGlueHost
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.leanback.widget.PlaybackControlsRow
import androidx.lifecycle.LifecycleOwner
import com.lib.page.*
import kotlinx.coroutines.*

abstract class PagePlayBackFragment: VideoSupportFragment() , PageViewFragment,
    PageViewCoroutine ,  ViewTreeObserver.OnGlobalLayoutListener{

    protected val scope = PageCoroutineScope()
    protected var delegate: PageDelegate? = null
    override var lifecycleOwner: LifecycleOwner? = null
    override val pageFragment: Fragment get() = this
    override val hasBackPressAction: Boolean
        @CallSuper
        get(){
            val f = pageChileren?.find { it.hasBackPressAction }
            f ?: return false
            return true
        }
    final override var pageObject: PageObject? = null
        set(value) {
            if(field == value) return
            field = value
            field?.let {f->
                f.params?.let { onPageParams(it) }
            }
        }

    final override var pageViewModel: PageViewModel?  = null
        set(value) {
            if(field == value) return
            field = value
            value ?: return
            onPageViewModel(value)
        }


    protected lateinit var transportControlGlue: PlaybackTransportControlGlue<MediaPlayerAdapter>
    protected var playerAdapter : MediaPlayerAdapter? = null
    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.viewTreeObserver?.addOnGlobalLayoutListener (this)
        if(pageObject?.isPopup == true ) delegate?.onAddedPage(pageObject!!)
        scope.createJob()
        pageViewModel?.onCreateView(this, pageObject)
        pageChileren?.forEach { it.lifecycleOwner = this }
        createTransportControlGlue()
        onCoroutineScope()
        scope.launch {
            delay(transactionTime)
            onTransactionCompleted()
        }
    }

    protected open fun createTransportControlGlue(){
        val glueHost = GlueHost(this)
        glueHost.setHostCallback( object : PlaybackGlueHost.HostCallback(){
            override fun onHostStart() = onPageHostStart()
            override fun onHostStop() = onPageHostStop()
            override fun onHostPause() = onPageHostPause()
            override fun onHostResume() = onPageHostResume()
            override fun onHostDestroy() = onPageHostDestroy()
        })
        playerAdapter = MediaPlayerAdapter(activity)
        playerAdapter?.setRepeatAction(PlaybackControlsRow.RepeatAction.INDEX_NONE)
        transportControlGlue = PlaybackTransportControlGlue(activity, playerAdapter!!)
        transportControlGlue.host = glueHost

        transportControlGlue.addPlayerCallback( object : PlaybackGlue.PlayerCallback(){
            override fun onPreparedStateChanged(glue: PlaybackGlue?) {onPagePreparedStateChanged(glue)}
            override fun onPlayStateChanged(glue: PlaybackGlue?) {onPagePlayStateChanged(glue)}
            override fun onPlayCompleted(glue: PlaybackGlue?) {onPagePlayCompleted()}
        })
    }

    @CallSuper
    override fun onGlobalLayout(){
        view?.viewTreeObserver?.removeOnGlobalLayoutListener( this )
    }
    @CallSuper
    override fun setOnPageDelegate(delegate: PageDelegate) {
        this.delegate = delegate
    }

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        transportControlGlue.pause()
        playerAdapter?.release()
        playerAdapter = null
        scope.destoryJob()
        pageViewModel?.onDestroyView(this, pageObject)
        if( pageObject?.isPopup == true ) delegate?.onRemovedPage(pageObject!!)
        delegate = null
        pageObject = null
        pageViewModel = null
    }


    @CallSuper
    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
        pageChileren?.forEach { it.onTransactionCompleted() }
    }

    @CallSuper
    override fun onCategoryChanged(pageObject: PageObject?) {
        super.onCategoryChanged(pageObject)
        pageChileren?.forEach { it.onCategoryChanged(pageObject) }
    }

    @CallSuper
    override fun onPageAdded(pageObject: PageObject) {
        super.onPageAdded(pageObject)
        pageChileren?.forEach { it.onPageAdded(pageObject) }
    }

    @CallSuper
    override fun onPageViewModel(vm: PageViewModel) {
        super.onPageViewModel(vm)
        pageChileren?.forEach { it.onPageViewModel(vm) }
    }

    @CallSuper
    override fun onPageParams(params: Map<String, Any?>) {
        super.onPageParams(params)
        pageChileren?.forEach { it.onPageParams(params) }
    }

    @CallSuper
    override fun onPageEvent(pageObject: PageObject?, type: String, data: Any?) {
        super.onPageEvent(pageObject, type, data)
        pageChileren?.forEach { it.onPageEvent(pageObject, type, data) }
    }

    @CallSuper
    override fun onPageReload() {
        super.onPageReload()
        pageChileren?.forEach { it.onPageReload() }
    }

    @CallSuper
    override fun onPageRemoved(pageObject: PageObject) {
        super.onPageRemoved(pageObject)
        pageChileren?.forEach { it.onPageRemoved(pageObject) }
    }

    @CallSuper
    override fun onPagePause() {
        pageChileren?.forEach { it.onPagePause() }
    }

    @CallSuper
    override fun onPageResume() {
        pageChileren?.forEach { it.onPageResume() }
    }

    abstract fun onPageVideoError(errorCode: Int, errorMessage: CharSequence?)
    protected open fun onPageVideoSizeChanged(videoWidth: Int, videoHeight: Int){}
    protected open fun onPageBufferingStateChanged(start: Boolean){}
    protected open fun onPageHostStart() {}
    protected open fun onPageHostStop() {}
    protected open fun onPageHostPause() {}
    protected open fun onPageHostResume() {}
    protected open fun onPageHostDestroy() {}
    protected open fun onPagePreparedStateChanged(glue: PlaybackGlue?) {}
    protected open fun onPagePlayStateChanged(glue: PlaybackGlue?) {}
    protected open fun onPagePlayCompleted(){}

    inner class GlueHost( fragment:VideoSupportFragment ): VideoSupportFragmentGlueHost(fragment){
        override fun getPlayerCallback(): PlayerCallback  = HostPlayerCallBack()

    }

    inner class HostPlayerCallBack : PlaybackGlueHost.PlayerCallback(){
        override fun onBufferingStateChanged(start: Boolean) {
            super.onBufferingStateChanged(start)
            onPageBufferingStateChanged(start)
        }

        override fun onError(errorCode: Int, errorMessage: CharSequence?) {
            super.onError(errorCode, errorMessage)
            onPageVideoError(errorCode, errorMessage)
        }

        override fun onVideoSizeChanged(videoWidth: Int, videoHeight: Int) {
            super.onVideoSizeChanged(videoWidth, videoHeight)
            onPageVideoSizeChanged(videoWidth, videoHeight)
        }
    }
}

