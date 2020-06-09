package com.kakaovx.homet.tv.page.viewmodel

import android.view.View
import androidx.annotation.CallSuper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kakaovx.homet.tv.store.PageID
import com.kakaovx.homet.tv.store.PageRepository

import com.lib.page.PageObject
import com.lib.page.PageViewModel
import com.skeleton.module.Repository

open class BasePageViewModel(val repo: PageRepository) : ViewModel(), PageViewModel {
    override val repository: Repository get() = repo

    override fun onCreateView(owner: LifecycleOwner, pageObject: PageObject?) {}

    @CallSuper
    override fun onDestroyView(owner: LifecycleOwner , pageObject: PageObject?) {
        repository.disposeLifecycleOwner(owner)
    }

    override fun onCleared() {
        super.onCleared()
    }


    fun pageChange(pageID:PageID, params:Map<String, Any?>? = null, sharedElement: View? = null, transitionName:String? = null){
        val po = repo.pageProvider.getPageObject(pageID)
        po.params = params
        repo.pagePresenter.pageChange(po, sharedElement, transitionName)
    }

    fun openPopup(pageID:PageID, params:Map<String, Any?>? = null, sharedElement: View? = null, transitionName:String? = null){
        val po = repo.pageProvider.getPageObject(pageID)
        po.params = params
        repo.pagePresenter.openPopup(po)
    }

    fun goBack() = repo.pagePresenter.goBack()


}