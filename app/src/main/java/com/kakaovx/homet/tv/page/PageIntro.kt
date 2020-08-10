package com.kakaovx.homet.tv.page

import android.Manifest
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.viewmodel.BasePageViewModel
import com.kakaovx.homet.tv.page.viewmodel.PageID
import com.lib.page.PageFragmentCoroutine
import com.lib.page.PageRequestPermission
import com.skeleton.module.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


class PageIntro : PageFragmentCoroutine(){
    private val appTag = javaClass.simpleName
    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    protected lateinit var viewModel: BasePageViewModel
    override fun getLayoutResID(): Int = R.layout.page_intro

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(BasePageViewModel::class.java)
        pageViewModel = viewModel
    }

    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
        scope.launch {
            delay(1500)
            viewModel.presenter.requestPermission(arrayOf(Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                object : PageRequestPermission {
                    override fun onRequestPermissionResult(resultAll: Boolean, permissions: List<Boolean>?) {
                        viewModel.pageChange(PageID.HOME)
                    }
                })
        }
    }
}