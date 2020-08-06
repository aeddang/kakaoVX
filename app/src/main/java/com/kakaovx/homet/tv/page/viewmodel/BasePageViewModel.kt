package com.kakaovx.homet.tv.page.viewmodel

import android.view.View
import androidx.annotation.CallSuper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kakaovx.homet.tv.store.PageRepository
import com.lib.page.PageAppViewModel

import com.lib.page.PageObject
import com.lib.page.PagePresenter
import com.lib.page.PageViewModel
import com.skeleton.module.Repository


open class BasePageViewModel(val repo: PageRepository) : ViewModel(), PageViewModel {
    override val repository: Repository get() = repo
    override val observable: PageAppViewModel get() = repo.pagePresenter.observable
    override val presenter:PagePresenter get() = repo.pagePresenter
    protected var owner: LifecycleOwner? = null


    @CallSuper
    override fun onCreateView(owner: LifecycleOwner, pageObject: PageObject?) {
        this.owner = owner
        repo.clearEvent()
    }

    @CallSuper
    override fun onDestroyView(owner: LifecycleOwner , pageObject: PageObject?) {
        if(this.owner != owner) return
        repository.disposeLifecycleOwner(owner)
        this.owner = null
        onDestroyOwner(owner, pageObject)
    }

    protected open fun onDestroyOwner(owner: LifecycleOwner , pageObject: PageObject?) {}

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        this.owner = null
    }


    fun pageChange(pageID: PageID, params:Map<String, Any?>? = null, sharedElement: View? = null, transitionName:String? = null){
        val po = repo.pageProvider.getPageObject(pageID)
        po.params = params
        repo.pagePresenter.pageChange(po, sharedElement, transitionName)
    }

    fun openPopup(pageID: PageID, params:Map<String, Any?>? = null, sharedElement: View? = null, transitionName:String? = null){
        val po = repo.pageProvider.getPageObject(pageID)
        po.params = params
        repo.pagePresenter.openPopup(po)
    }

    fun closePopup(pageID: PageID){
        val po = repo.pageProvider.getPageObject(pageID)
        repo.pagePresenter.closePopup(po)
    }


    fun goBack() = repo.pagePresenter.goBack()

    fun getLeftFocusTab(id:PageID):View?{
        return repo.pageModel.leftTab?.getFocusTab(id)
    }

    fun loading(){
        presenter.loading()
    }
    fun loaded(){
        presenter.loaded()
    }
}