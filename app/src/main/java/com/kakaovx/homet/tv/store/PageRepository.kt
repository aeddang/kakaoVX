package com.kakaovx.homet.tv.store

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.kakaovx.homet.tv.store.api.account.AccountManager
import com.kakaovx.homet.tv.store.api.homet.HometApiType
import com.kakaovx.homet.tv.store.api.homet.HometManager
import com.kakaovx.homet.tv.store.database.DataBaseManager
import com.kakaovx.homet.tv.store.preference.SettingPreference
import com.lib.page.PagePresenter
import com.skeleton.module.ImageFactory
import com.skeleton.module.Repository
import com.skeleton.module.network.NetworkFactory

class PageRepository (ctx: Context,
                      networkFactory: NetworkFactory,
                      imageFactory: ImageFactory,
                      val settingPreference: SettingPreference,
                      val dataBaseManager: DataBaseManager,
                      val accountManager: AccountManager,
                      val hometManager: HometManager,
                      val pageModel: ActivityModel,
                      val pageProvider: FragmentProvider,
                      val pagePresenter: PagePresenter

) : Repository(ctx, networkFactory, imageFactory){

    fun loadApi(owner: LifecycleOwner, type: HometApiType, params:Map<String, Any?>? = null)  = hometManager.loadApi(owner, type, params)


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