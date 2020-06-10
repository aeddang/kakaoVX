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
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.lgtv.OMAReceiver
import com.kakaovx.homet.tv.lgtv.utils.LogUtil
import com.kakaovx.homet.tv.store.ActivityModel
import com.kakaovx.homet.tv.store.FragmentProvider
import com.kakaovx.homet.tv.store.PageID
import com.kakaovx.homet.tv.store.PageRepository
import com.lib.page.PageActivity
import com.lib.page.PagePresenter
import com.lib.page.PageRequestPermission
import dagger.android.AndroidInjection
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

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreatedView() {
        super.onCreatedView()

        LogUtil.d(LogUtil.DEBUG_LEVEL_3, "enter")
        OMAReceiver.sendAppVersionCheck(this, true)
        //finish()
        repository.setDefaultLifecycleOwner(this)

        pagePresenter.requestPermission(arrayOf(Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            object : PageRequestPermission {
                override fun onRequestPermissionResult(resultAll: Boolean, permissions: List<Boolean>?) {
                    pageStart(pageProvider.getPageObject(PageID.HOME))
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        repository.disposeDefaultLifecycleOwner(this)
        repository.disposeLifecycleOwner(this)
    }

}
