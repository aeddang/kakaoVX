package com.lib.page
import android.view.View

open class PageActivityPresenter : PagePresenter{
    override lateinit var activity: PageActivity
    override var isFullScreen: Boolean
        get() = activity.isFullScreen
        set(value) {
            activity.isFullScreen = value
        }
    override val currentPage: PageObject?
        get() = activity.currentPage
    override val lastPage: PageObject?
        get() = activity.lastPage
    override val prevPage: PageObject?
        get() = activity.prevPage

    override fun getPageFragment(pageObject: PageObject?) = activity.getPageFragment(pageObject)
    override fun goHome(idx: Int): PagePresenter {
        activity.goHome(idx)
        return this
    }

    override fun goBack(pageObject: PageObject?): PagePresenter {
        activity.goBack(pageObject)
        return this
    }

    override fun clearPageHistory(pageObject: PageObject?): PagePresenter {
        activity.clearPageHistory(pageObject)
        return this
    }

    override fun closePopup(pageObject: PageObject, isAni: Boolean): PagePresenter {
        activity.closePopup(pageObject,isAni)
        return this
    }

    override fun closeAllPopup(isAni: Boolean): PagePresenter {
        activity.closeAllPopup(isAni)
        return this
    }

    override fun openPopup(pageObject: PageObject, sharedElement: View?, transitionName: String?): PagePresenter {
        activity.openPopup(pageObject,sharedElement,transitionName)
        return this
    }

    override fun pageInit(): PagePresenter {
        activity.pageInit()
        return this
    }

    override fun pageStart(pageObject: PageObject): PagePresenter {
        activity.pageStart(pageObject)
        return this
    }

    override fun pageChange(pageObject: PageObject, sharedElement: View?, transitionName: String?): PagePresenter {
        activity.pageChange(pageObject,sharedElement,transitionName)
        return this
    }

    override fun hasPermissions(permissions: Array<out String>) = activity.hasPermissions(permissions)

    override fun requestPermission(
        permissions: Array<out String>,
        requester: PageRequestPermission
    ) {
        activity.requestPermission(permissions,requester)
    }

    override fun loading(isRock: Boolean): PagePresenter {
        activity.loading(isRock)
        return this
    }

    override fun loaded(): PagePresenter {
        activity.loaded()
        return this
    }

    override fun finishApp() {
        activity.finishApp()
    }

}