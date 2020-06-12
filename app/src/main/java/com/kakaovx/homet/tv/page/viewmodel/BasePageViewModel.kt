package com.kakaovx.homet.tv.page.viewmodel

import android.view.View
import androidx.annotation.CallSuper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kakaovx.homet.tv.store.PageRepository
import com.kakaovx.homet.tv.store.api.ApiEvent
import com.kakaovx.homet.tv.store.api.homet.HometApiType

import com.lib.page.PageObject
import com.lib.page.PageViewModel
import com.skeleton.module.Repository

enum class PageViewModelEvent{
    DataLoad, DataLoaded, DataLoadError
}


open class BasePageViewModel(val repo: PageRepository) : ViewModel(), PageViewModel {
    override val repository: Repository get() = repo
    protected var owner: LifecycleOwner? = null


    val event = MutableLiveData<PageViewModelEvent>()

    @CallSuper
    override fun onCreateView(owner: LifecycleOwner, pageObject: PageObject?) {
        this.owner = owner
        repo.clearEvent()
    }

    @CallSuper
    override fun onDestroyView(owner: LifecycleOwner , pageObject: PageObject?) {
        repository.disposeLifecycleOwner(owner)
        this.owner = null
    }

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

    fun goBack() = repo.pagePresenter.goBack()


}