package com.kakaovx.homet.tv.page.guide
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.popups.PageErrorSurport
import com.kakaovx.homet.tv.page.viewmodel.BasePageViewModel
import com.kakaovx.homet.tv.page.viewmodel.PageID
import com.kakaovx.homet.tv.store.api.homet.HometApiType
import com.lib.page.PageFragmentCoroutine
import com.skeleton.module.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.page_guide.*
import java.util.HashMap
import javax.inject.Inject


class PageGuide : PageFragmentCoroutine(){
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

    override fun getLayoutResID(): Int = R.layout.page_guide
    private var pageList: PageGuideList? = null

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        pageList = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        listError.visibility = View.GONE
        if( pageList == null){
            try {
                val supportFragmentManager = childFragmentManager
                val transaction = supportFragmentManager.beginTransaction()
                val page = viewModel.repo.pageProvider.getPageObject(PageID.GUIDE_LIST)
                val fragment = viewModel.repo.pageProvider.getPageView(page)
                pageList = fragment as PageGuideList
                transaction.add(R.id.listArea, fragment.pageFragment)
                transaction.commit()

            }catch(e:IllegalStateException){

            }
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()
        viewModel.repo.hometManager.error.observe(viewLifecycleOwner ,Observer { e ->
            e ?: return@Observer
            if( e.type != HometApiType.GUIDE_IMAGES ) return@Observer
            listError.visibility = View.VISIBLE
        })
    }

    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
    }
}