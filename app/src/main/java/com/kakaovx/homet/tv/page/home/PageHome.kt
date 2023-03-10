package com.kakaovx.homet.tv.page.home
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.viewmodel.BasePageViewModel
import com.kakaovx.homet.tv.page.viewmodel.PageID
import com.kakaovx.homet.tv.store.api.HomeTResponse
import com.kakaovx.homet.tv.store.api.homet.HometApiType
import com.lib.page.PageFragmentCoroutine
import com.skeleton.module.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject


class PageHome : PageFragmentCoroutine(){
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

    override fun getLayoutResID(): Int = R.layout.page_home
    private var pageList: PageHomeList? = null

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        pageList = null
        focusHandler.removeCallbacks(focusRunable)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if( pageList == null){
            try {
                val supportFragmentManager = childFragmentManager
                val transaction = supportFragmentManager.beginTransaction()
                val page = viewModel.repo.pageProvider.getPageObject(PageID.HOME_LIST)
                val fragment = viewModel.repo.pageProvider.getPageView(page)
                pageList = fragment as PageHomeList
                transaction.add(R.id.listArea, fragment.pageFragment)
                transaction.commit()

            }catch(e:IllegalStateException){

            }
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()

        viewModel.repo.hometManager.success.observe(this,Observer { e ->
            e ?: return@Observer
            val type = e.type as? HometApiType
            val response = e.data as? HomeTResponse<*>
            response ?: return@Observer
            type ?: return@Observer
            when(type){
                HometApiType.PROGRAMS_RECENT -> {}
                else -> {}
            }
        })
    }

    private var focusHandler = Handler()
    private var focusRunable = Runnable {
        viewModel.getLeftFocusTab(PageID.HOME)?.let{
            it.requestFocus()
        }
    }

    companion  object {
        var isInit = false
    }

    override fun onTransactionCompleted() {
        super.onTransactionCompleted()

        viewModel.getLeftFocusTab(PageID.HOME)?.let{
            pageList?.exitFocusView = it
            if(!isInit){
                isInit = true
                focusHandler.postDelayed(focusRunable, 300)
            }
           // btnGuide.nextFocusLeftId = it.id
            //btnGuide.nextFocusUpId = it.id
        }
    }
}