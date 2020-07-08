package com.kakaovx.homet.tv.page.home
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.viewmodel.BasePageViewModel
import com.kakaovx.homet.tv.page.viewmodel.PageID
import com.lib.page.PageFragmentCoroutine
import com.skeleton.module.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.page_home.*
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
    private var pageListID:String? = null

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        pageListID = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if( pageListID != null) return
        try {
            val supportFragmentManager = childFragmentManager
            val transaction = supportFragmentManager.beginTransaction()
            val page = viewModel.repo.pageProvider.getPageObject(PageID.HOME_LIST)
            val fragment = viewModel.repo.pageProvider.getPageView(page)
            pageListID = page.fragmentID
            transaction.add(R.id.listArea, fragment.pageFragment, pageListID )
            transaction.commit()
        }catch(e:IllegalStateException){

        }
        btnGuide.addFocusables(arrayListOf(viewModel.getLeftFocusTab(PageID.HOME)), View.FOCUS_LEFT)
    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()
    }
}