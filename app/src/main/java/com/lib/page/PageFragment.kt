package com.lib.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner

abstract class PageFragment: Fragment(), Page, PageViewFragment,  ViewTreeObserver.OnGlobalLayoutListener{
    protected var delegate:PageDelegate? = null
    override var lifecycleOwner: LifecycleOwner? = null
    override val pageFragment: Fragment get() = this
    override val hasBackPressAction: Boolean
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

    @CallSuper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate( getLayoutResID() , container, false)
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.viewTreeObserver?.addOnGlobalLayoutListener { onGlobalLayout() }
        if(pageObject?.isPopup == true ) delegate?.onAddedPage(pageObject!!)
        pageViewModel?.onCreateView(this, pageObject)
        pageChileren?.forEach { it.lifecycleOwner = this }
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
    override fun onPause() {
        super.onPause()
        pageChileren?.forEach { it.onPagePause() }
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        pageChileren?.forEach { it.onPageResume() }
    }
}