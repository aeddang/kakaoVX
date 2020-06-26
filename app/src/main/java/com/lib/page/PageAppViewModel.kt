package com.lib.page

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData


enum class PageEventType(var id:String? = null){
    INIT,
    ADD_POPUP, REMOVE_POPUP, CHANGE_PAGE,
    ADDED_POPUP, REMOVED_POPUP, CHANGED_PAGE,
    EVENT
}

enum class PageStatus{
    FREE, BUSY
}

enum class PageNetworkStatus{
    AVAILABLE, LOST, UNDEFINED
}

data class PageEvent(val type:PageEventType, val id: String, var data:Any? = null)

class PageAppViewModel {
    val event = MutableLiveData<PageEvent?>()
    val networkStatus = MutableLiveData<PageNetworkStatus>()
    val status = MutableLiveData<PageStatus>()

    init {
        status.value = PageStatus.FREE
        networkStatus.value = PageNetworkStatus.UNDEFINED
    }

    fun onDestroyView(owner: LifecycleOwner, pageObject: PageObject?) {
        event.removeObservers(owner)
        status.removeObservers(owner)
        networkStatus.removeObservers(owner)
    }
}