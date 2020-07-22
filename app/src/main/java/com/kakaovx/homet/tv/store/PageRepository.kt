package com.kakaovx.homet.tv.store

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.kakaovx.homet.tv.page.viewmodel.ActivityModel
import com.kakaovx.homet.tv.page.viewmodel.FragmentProvider
import com.kakaovx.homet.tv.store.api.ApiSuccess
import com.kakaovx.homet.tv.store.api.account.AccountManager
import com.kakaovx.homet.tv.store.api.homet.HometApiType
import com.kakaovx.homet.tv.store.api.homet.HometManager
import com.kakaovx.homet.tv.store.api.storage.GuideImageData
import com.kakaovx.homet.tv.store.api.storage.StorageApiType
import com.kakaovx.homet.tv.store.api.storage.StorageManager
import com.kakaovx.homet.tv.store.api.wecandeo.WecandeoManager
import com.kakaovx.homet.tv.store.database.DataBaseManager
import com.kakaovx.homet.tv.store.preference.SettingPreference
import com.lib.page.PagePresenter
import com.skeleton.module.Repository

class PageRepository (ctx: Context,
                      val settingPreference: SettingPreference,
                      val dataBaseManager: DataBaseManager,
                      val accountManager: AccountManager,
                      val hometManager: HometManager,
                      val wecandeoManager: WecandeoManager,
                      val storageManager: StorageManager,
                      val pageModel: ActivityModel,
                      val pageProvider: FragmentProvider,
                      val pagePresenter: PagePresenter

) : Repository(ctx){

    fun clearEvent(){
        hometManager.clearEvent()
        wecandeoManager.clearEvent()
        storageManager.clearEvent()
    }
    fun loadApi(owner: LifecycleOwner, type: HometApiType, params:Map<String, Any?>? = null)  = hometManager.loadApi(owner, type, params)
    fun loadPrograms(owner: LifecycleOwner, filterPurpose: String = "", page:Int = 1)  = hometManager.loadPrograms(owner, filterPurpose, page)
    fun loadProgramDetail(owner:LifecycleOwner, programID:String) = hometManager.loadProgramDetail(owner, programID)
    fun loadExerciseDetail(owner:LifecycleOwner, programID:String, exerciseID:String, roundID:String) = hometManager.loadExerciseDetail(owner, programID, exerciseID, roundID)
    fun loadExercisePlayer(owner:LifecycleOwner, programID:String, exerciseID:String, roundID:String, movieType:String)
            = hometManager.loadExercisePlayer(owner, programID, exerciseID, roundID, movieType)

    val guides =  MutableLiveData<GuideImageData?>()
    fun loadGuides(owner: LifecycleOwner){
        if(guides.value != null) return
        storageManager.success.observe(owner, Observer{
            if(it?.type == StorageApiType.GUIDE_IMAGES){
                guides.value = it.data as? GuideImageData
                storageManager.success.removeObservers(owner)
            }
        })
        storageManager.loadGuideImages(owner)
    }

    override fun setDefaultLifecycleOwner(owner: LifecycleOwner) {
        hometManager.setDefaultLifecycleOwner(owner)
        accountManager.setDefaultLifecycleOwner(owner)
        wecandeoManager.setDefaultLifecycleOwner(owner)
        storageManager.setDefaultLifecycleOwner(owner)
    }

    override fun disposeDefaultLifecycleOwner(owner: LifecycleOwner) {
        hometManager.disposeDefaultLifecycleOwner(owner)
        accountManager.disposeDefaultLifecycleOwner(owner)
        wecandeoManager.disposeDefaultLifecycleOwner(owner)
        storageManager.disposeDefaultLifecycleOwner(owner)
    }

    override fun disposeLifecycleOwner(owner: LifecycleOwner){
        hometManager.disposeLifecycleOwner(owner)
        accountManager.disposeLifecycleOwner(owner)
        wecandeoManager.disposeLifecycleOwner(owner)
        storageManager.disposeLifecycleOwner(owner)
    }
}

