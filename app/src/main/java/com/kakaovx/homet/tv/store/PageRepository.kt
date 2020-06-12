package com.kakaovx.homet.tv.store

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.kakaovx.homet.tv.page.viewmodel.ActivityModel
import com.kakaovx.homet.tv.page.viewmodel.FragmentProvider
import com.kakaovx.homet.tv.store.api.account.AccountManager
import com.kakaovx.homet.tv.store.api.homet.HometApiType
import com.kakaovx.homet.tv.store.api.homet.HometManager
import com.kakaovx.homet.tv.store.database.DataBaseManager
import com.kakaovx.homet.tv.store.preference.SettingPreference
import com.lib.page.PagePresenter
import com.skeleton.module.Repository

class PageRepository (ctx: Context,
                      val settingPreference: SettingPreference,
                      val dataBaseManager: DataBaseManager,
                      val accountManager: AccountManager,
                      val hometManager: HometManager,
                      val pageModel: ActivityModel,
                      val pageProvider: FragmentProvider,
                      val pagePresenter: PagePresenter

) : Repository(ctx){

    fun clearEvent() = hometManager.clearEvent()
    fun loadApi(owner: LifecycleOwner, type: HometApiType, params:Map<String, Any?>? = null)  = hometManager.loadApi(owner, type, params)
    fun loadPrograms(owner: LifecycleOwner, filterPurpose: String = "", page:Int = 1)  = hometManager.loadPrograms(owner, filterPurpose, page)
    fun loadProgramDetail(owner:LifecycleOwner, programID:String) = hometManager.loadProgramDetail(owner, programID)

    override fun setDefaultLifecycleOwner(owner: LifecycleOwner) {
        hometManager.setDefaultLifecycleOwner(owner)
        accountManager.setDefaultLifecycleOwner(owner)
    }

    override fun disposeDefaultLifecycleOwner(owner: LifecycleOwner) {
        hometManager.disposeDefaultLifecycleOwner(owner)
        accountManager.disposeDefaultLifecycleOwner(owner)
    }

    override fun disposeLifecycleOwner(owner: LifecycleOwner){
        hometManager.disposeLifecycleOwner(owner)
        accountManager.disposeLifecycleOwner(owner)
    }



}