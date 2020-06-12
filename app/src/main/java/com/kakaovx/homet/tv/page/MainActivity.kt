/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.kakaovx.homet.tv.page
import android.Manifest
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.core.content.ContextCompat
import androidx.leanback.app.BackgroundManager
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.lgtv.OMAReceiver
import com.kakaovx.homet.tv.lgtv.utils.LogUtil
import com.kakaovx.homet.tv.page.error.PageError
import com.kakaovx.homet.tv.page.viewmodel.ActivityModel
import com.kakaovx.homet.tv.page.viewmodel.FragmentProvider
import com.kakaovx.homet.tv.page.viewmodel.PageID
import com.kakaovx.homet.tv.store.PageRepository
import com.kakaovx.homet.tv.store.api.account.AccountManager
import com.lib.page.PageActivity
import com.lib.page.PageCoroutineScope
import com.lib.page.PagePresenter
import com.lib.page.PageRequestPermission
import com.lib.util.Log
import dagger.android.AndroidInjection
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.HashMap
import javax.inject.Inject


class MainActivity : PageActivity() {
    @Inject lateinit var repository: PageRepository
    @Inject lateinit var pageProvider: FragmentProvider
    @Inject lateinit var pageModel: ActivityModel
    @Inject lateinit var pagePresenter: PagePresenter
    override fun getPageActivityPresenter(): PagePresenter = pagePresenter
    override fun getPageActivityModel() = pageModel
    override fun getPageViewProvider() = pageProvider
    override fun getPageAreaId() = R.id.area
    override fun getLayoutResID() = R.layout.activity_main
    private val appTag = javaClass.simpleName
    val scope = PageCoroutineScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreatedView() {
        super.onCreatedView()
        scope.createJob()
        prepareBackgroundManager()
        LogUtil.d(LogUtil.DEBUG_LEVEL_3, "enter")
        OMAReceiver.sendAppVersionCheck(this, true)
        //finish()
        repository.setDefaultLifecycleOwner(this)
        repository.accountManager.event.observe(this, Observer{
            when(it){
                AccountManager.AccountEvent.onJwtError ->{
                    repository.accountManager.error?.let { e->
                        val param = HashMap<String, Any>()
                        param[PageError.API_ERROR] = e
                        val po = pageProvider.getPageObject(PageID.ERROR)
                        po.params = param
                        pageChange( po )
                    }

                }
                else ->{}
            }
        })

        pagePresenter.requestPermission(arrayOf(Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            object : PageRequestPermission {
                override fun onRequestPermissionResult(resultAll: Boolean, permissions: List<Boolean>?) {
                    pageStart(pageProvider.getPageObject(PageID.HOME))
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        backgroundUpdateJob = null
        scope.destoryJob()
        repository.disposeDefaultLifecycleOwner(this)
        repository.disposeLifecycleOwner(this)
        backgroundManager.release()

    }


    private lateinit var backgroundManager:BackgroundManager
    private lateinit var backgroundMetrics: DisplayMetrics
    private var defaultBackground:Drawable? = null
    private var backgroundUpdateJob:Job? = null
    private fun prepareBackgroundManager() {
        backgroundManager = BackgroundManager.getInstance(this)
        backgroundManager.attach(window)
        defaultBackground = ContextCompat.getDrawable(this, R.drawable.default_background)
        backgroundMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(backgroundMetrics)
        pageModel.backGroundImage.observe(this, Observer { path ->
            backgroundUpdateJob?.cancel()
            Log.i(appTag, "updateBackground path = $path")
            backgroundUpdateJob = scope.launch {
                delay(300)
                updateBackground(path)
            }
        })
    }

    private fun updateBackground(uri: String?) {
        val width = backgroundMetrics.widthPixels
        val height = backgroundMetrics.heightPixels
        Glide.with(this).asBitmap()
            .load(uri).centerCrop()
            .error(defaultBackground)
            .into(object : CustomTarget<Bitmap>(width, height){
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    backgroundManager.drawable = BitmapDrawable(resources, resource)
                }
                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

}
