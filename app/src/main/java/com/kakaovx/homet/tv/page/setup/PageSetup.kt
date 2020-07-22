package com.kakaovx.homet.tv.page.setup

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.lgtv.OMAReceiver
import com.kakaovx.homet.tv.page.viewmodel.BasePageViewModel
import com.kakaovx.homet.tv.page.viewmodel.PageID
import com.kakaovx.homet.tv.util.AppUtil
import com.lib.page.PageFragmentCoroutine
import com.skeleton.module.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.page_setup.*
import javax.inject.Inject

class PageSetup : PageFragmentCoroutine(){
    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    protected lateinit var viewModel: BasePageViewModel
    private val appTag = javaClass.simpleName


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(BasePageViewModel::class.java)
        pageViewModel = viewModel
    }

    override fun getLayoutResID(): Int = R.layout.page_setup

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getLeftFocusTab(PageID.SETUP)?.let{
            btnUpdate.nextFocusLeftId = it.id
        }
        btnUpdate.requestFocus()
        context?.let { ctx->
            textVersion.text =
                ctx.getString(R.string.page_setup_version, AppUtil.getAppVersion(ctx))
        }
    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()
        btnUpdate.setOnClickListener{
            OMAReceiver.sendAppVersionCheck(context, true)
        }
    }

    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
    }
}