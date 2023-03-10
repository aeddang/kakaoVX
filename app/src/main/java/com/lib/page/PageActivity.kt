package com.lib.page
import android.content.Context
import android.content.pm.PackageManager
import android.net.*
import android.net.ConnectivityManager.*
import android.os.Build
import android.os.Bundle
import android.transition.ChangeBounds
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.AnimRes
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity
import com.lib.util.Log
import java.util.*
import kotlin.math.abs

abstract class PageActivity : FragmentActivity(), Page, PageRequestPermission, PageDelegate{
    private val appTag = javaClass.simpleName
    abstract fun getPageActivityPresenter(): PagePresenter
    abstract fun getPageActivityModel(): PageModel
    abstract fun getPageViewProvider(): PageProvider
    protected lateinit var activityModel : PageModel
    protected lateinit var viewProvider : PageProvider
    @IdRes abstract fun getPageAreaId(): Int
    protected lateinit var pageArea: ViewGroup
    protected val historys = Stack< PageObject >  ()
    protected val popups = ArrayList< PageObject >()

    private var currentRequestPermissions = HashMap< Int , PageRequestPermission >()
    val pageAppViewModel = PageAppViewModel()

    val currentPage: PageObject?
        get(){
            return activityModel.currentPageObject
        }
    val lastPage: PageObject?
        get(){
            return if( popups.isEmpty() ) currentPage else popups.last()
        }
    val prevPage: PageObject?
        get(){
            return if( historys.isEmpty() ) null else historys.last()
        }

    var isFullScreen:Boolean = false
        set(value) {
            if( value == field ) return
            field = value
            if(field){
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            }else{
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_VISIBLE)
            }
        }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView( getLayoutResID() )
        onCreatedView()
        val builder = NetworkRequest.Builder()
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(builder.build(),object: NetworkCallback(){
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                pageAppViewModel.networkStatus.postValue(PageNetworkStatus.AVAILABLE)
            }

            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties)
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                pageAppViewModel.networkStatus.postValue(PageNetworkStatus.LOST)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                pageAppViewModel.networkStatus.postValue(PageNetworkStatus.LOST)
            }
        })
        pageAppViewModel.networkStatus.value = if( connectivityManager.isDefaultNetworkActive ) PageNetworkStatus.AVAILABLE else PageNetworkStatus.LOST

    }
    @CallSuper
    protected open fun onCreatedView(){
        pageArea = findViewById( getPageAreaId() )
        activityModel = getPageActivityModel()
        viewProvider = getPageViewProvider()
        getPageActivityPresenter().activity = this
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        popups.clear()
        historys.clear()
        activityModel.currentPageObject = null
        currentRequestPermissions.clear()
    }

    /*
     interface override
    */
    @CallSuper
    open fun getPageFragment(pageObject:PageObject?): PageViewFragment?{
        pageObject ?: return null
        val fragment = supportFragmentManager.findFragmentByTag(pageObject.fragmentID)
        return fragment as? PageViewFragment
    }
    @CallSuper
    open fun clearPageHistory(pageObject:PageObject? = null){
        if(pageObject == null) {
            historys.clear()
            return
        }
        var peek:PageObject? = null
        do {
            if(peek != null) historys.pop()
            peek = try { historys.peek() }catch (e:EmptyStackException){ null }
        } while (pageObject != peek  && !historys.isEmpty())
    }
    @CallSuper
    open fun openPopup(pageObject: PageObject, sharedElement: View?, transitionName:String?) {
        onOpenPopup(pageObject, sharedElement, transitionName)
    }
    @CallSuper
    open fun closePopup(pageObject: PageObject ,isAni:Boolean) {
        onClosePopup(pageObject, isAni)
    }
    @CallSuper
    open fun closeAllPopup(isAni:Boolean){
        onCloseAllPopup(isAni)
    }
    @CallSuper
    open fun pageInit() {
        activityModel.isPageInit = true

    }
    @CallSuper
    open fun pageStart(pageObject:PageObject){
        onPageChange(pageObject, true)
    }
    @CallSuper
    open fun pageChange(pageObject:PageObject, sharedElement: View? = null , transitionName:String? = null){
        onPageChange(pageObject, false, sharedElement, transitionName)
    }
    @CallSuper
    open fun goHome(idx:Int = 0){
        pageChange( activityModel.getHome(idx))
    }
    @CallSuper
    open fun goBack(pageObject:PageObject? = null){
        if(pageObject != null) clearPageHistory(pageObject)
        onBackPressed()
    }
    @CallSuper
    open fun finishApp(){ super.finish() }
    open fun loading(isRock:Boolean = false){}
    open fun loaded(){}

    /*
    BackPressed
    */
    fun superBackPressAction(){
        super.onBackPressed()
    }
    @CallSuper
    override fun onBackPressed() {
        if(popups.isNotEmpty()){
            val last = popups.last()
            val lastPopup = supportFragmentManager.findFragmentByTag(last.fragmentID) as? PageView
            lastPopup?.let { if( it.hasBackPressAction ) return }
            popups.remove(last)
            onClosePopup(last)
            return
        }
        activityModel.currentPageObject ?: return
        val currentPage = getPageFragment(activityModel.currentPageObject)
        if( currentPage?.hasBackPressAction == true ) return
        if( activityModel.isHomePage( activityModel.currentPageObject!! ) )  onExitAction() else onBackPressedAction()
    }

    private var finalExitActionTime:Long = 0L
    private fun resetBackPressedAction() { finalExitActionTime = 0L }
    protected open fun onExitAction() {
        val cTime =  Date().time
        if( abs(cTime - finalExitActionTime) < 3000L ) { finish() }
        else {
            finalExitActionTime = cTime
            Toast.makeText(this,activityModel.getPageExitMessage(), Toast.LENGTH_SHORT).show()
        }
    }

    protected open fun onBackPressedAction() {
        if( historys.isEmpty()) {
            if( activityModel.currentPageObject == null) goHome()
            else if( activityModel.isHomePage( activityModel.currentPageObject!! )) onExitAction()
            else goHome()
        }else {
            onPageChange(historys.pop()!!, false, null, null, true)
        }
    }

    /*
    Page Transaction
    */
    @CallSuper
    protected open fun onWillChangePage(prevPage:PageObject?, nextPage:PageObject?){
        nextPage ?: return
        isFullScreen = activityModel.isFullScreenPage(nextPage)
        val willChangeOrientation = activityModel.getPageOrientation(nextPage)
        if (willChangeOrientation != -1 && requestedOrientation != willChangeOrientation) requestedOrientation = willChangeOrientation
    }

    private fun getWillChangePageFragment( pageObject: PageObject, isPopup:Boolean ): PageViewFragment {
        pageObject.isPopup = isPopup
        onWillChangePage(lastPage, pageObject)
        if( activityModel.isBackStackPage(pageObject) ) {
            val backStackFragment = supportFragmentManager.findFragmentByTag( pageObject.fragmentID ) as? PageFragment
            backStackFragment?.let {
                backStackFragment.pageObject = pageObject
                return backStackFragment
            }
        }
        val newFragment = viewProvider.getPageView(pageObject)
        newFragment.pageObject = pageObject
        return newFragment
    }

    private fun getSharedTransitionName(sharedElement: android.view.View,  transitionName:String):String{
        val name = ViewCompat.getTransitionName(sharedElement)
        if(name == null) ViewCompat.setTransitionName(sharedElement, transitionName)
        return transitionName
    }
    protected open fun getSharedChange():Any { return ChangeBounds() }


    private fun onPageChange( pageObject: PageObject, isStart:Boolean = false, sharedElement: android.view.View? = null, transitionName:String? = null, isBack:Boolean = false) {
        if( !activityModel.isChangePageAble(pageObject) ) return
        if( activityModel.currentPageObject?.pageID == pageObject.pageID ) {
            if(pageObject.params == null){
                getPageFragment(activityModel.currentPageObject)?.onPageReload()
                return
            }else {
                val currentValues = activityModel.currentPageObject?.params?.map { it.toString() }
                val values = pageObject.params?.map { it.toString() }
                if(currentValues == values){
                    getPageFragment(activityModel.currentPageObject)?.onPageReload()
                    return
                }
            }
        }
        onCloseAllPopup()
        resetBackPressedAction()
        val willChangePage = getWillChangePageFragment(pageObject, false)
        if(activityModel.isChangedCategory(currentPage, pageObject)) willChangePage.onCategoryChanged(currentPage)
        willChangePage.setOnPageDelegate( this )
        getPageFragment(currentPage)?.onWillDestory(pageObject)
        try {
            val transaction = supportFragmentManager.beginTransaction()
            if (isStart) {
                transaction.setCustomAnimations(getPageStart(), getPageOut(false))
            } else {
                if (sharedElement == null) {
                    val currentPos = activityModel.currentPageObject?.pageIDX ?: 9999
                    var isReverse = currentPos > pageObject.pageIDX
                   // if(isBack) isReverse = !isReverse
                    transaction.setCustomAnimations(getPageIn(isReverse), getPageOut(isReverse))
                } else {
                    transaction.setReorderingAllowed(true)
                    transitionName?.let {
                        transaction.addSharedElement(
                            sharedElement,
                            getSharedTransitionName(sharedElement, it)
                        )
                    }
                    willChangePage.pageFragment.sharedElementEnterTransition = getSharedChange()
                }
            }
            activityModel.currentPageObject?.let {
                if (activityModel.isBackStackPage(it)) transaction.addToBackStack(it.fragmentID)
            }
            transaction.replace(getPageAreaId(), willChangePage.pageFragment, pageObject.fragmentID)
            transaction.commit()
        }catch(e:IllegalStateException){ }

        if( !isBack ) {
            activityModel.currentPageObject?.let {
                if( activityModel.isHistoryPage(it) ) historys.push(it)
            }
        }
        if(activityModel.currentPageObject == null) pageAppViewModel.event.value = PageEvent(PageEventType.INIT, pageObject.pageID, pageObject.params)
        pageAppViewModel.event.value = PageEvent(PageEventType.CHANGE_PAGE, pageObject.pageID, pageObject.params)
        activityModel.currentPageObject = pageObject
    }

    private var finalAddedPopupID:String? = null
    private var finalOpenPopupTime:Long = 0L
    private fun onOpenPopup(pageObject: PageObject, sharedElement: android.view.View?, transitionName:String?) {
        if( !activityModel.isChangePageAble(pageObject) ) return
        val cTime =  Date().time
        if( finalAddedPopupID == pageObject.pageID && (abs(cTime - finalOpenPopupTime) < 500 ) ) return
        finalAddedPopupID = pageObject.pageID
        finalOpenPopupTime = cTime
        resetBackPressedAction()
        val popup = getWillChangePageFragment(pageObject, true)
        popup.setOnPageDelegate( this )
        try{

            val transaction = supportFragmentManager.beginTransaction()
            if(sharedElement == null) {
                transaction.setCustomAnimations(getPopupIn(), getPopupOut())
            }else {
                transaction.setReorderingAllowed(true)
                transitionName?.let { transaction.addSharedElement(sharedElement, getSharedTransitionName(sharedElement,it)) }
                popup.pageFragment.sharedElementEnterTransition = getSharedChange()
                getPageFragment(activityModel.currentPageObject)?.let { transaction.hide(it.pageFragment) }
            }
            transaction.add(getPageAreaId(), popup.pageFragment, pageObject.fragmentID)
            transaction.commit()
            if(sharedElement != null) {
                getPageFragment(activityModel.currentPageObject)?.let { supportFragmentManager.beginTransaction().show(it.pageFragment).commit()}
            }
        } catch(e:IllegalStateException){ }
        popups.add(pageObject)
        pageAppViewModel.event.value = PageEvent(PageEventType.ADD_POPUP, pageObject.pageID, pageObject.params)

    }
    private fun onCloseAllPopup(isAni:Boolean = false) {
        try {
            val transaction = supportFragmentManager.beginTransaction()
            popups.forEach { p ->
                getPageFragment(p)?.let { f ->
                    if (activityModel.isBackStackPage(p)) transaction.addToBackStack(p.fragmentID)
                    if (isAni) transaction.setCustomAnimations(getPopupIn(), getPopupOut())
                    transaction.remove(f.pageFragment)
                }
                pageAppViewModel.event.value = PageEvent(PageEventType.REMOVE_POPUP, p.pageID, p.params)
            }
            transaction.commitNow()
        }catch(e:IllegalStateException){ }
        onWillChangePage(null, activityModel.currentPageObject)
        popups.clear()
    }

    private fun onClosePopup(pageObject: PageObject ,isAni:Boolean = true){
        popups.remove(pageObject)
        val fragment = supportFragmentManager.findFragmentByTag(pageObject.fragmentID)
        fragment ?: return
        val nextPage = if(popups.isNotEmpty()) popups.last() else activityModel.currentPageObject
        onWillChangePage(null, nextPage)
        try{
            val transaction = supportFragmentManager.beginTransaction()
            if( activityModel.isBackStackPage(pageObject)) transaction.addToBackStack( pageObject.fragmentID )
            if(isAni) transaction.setCustomAnimations(getPopupIn(), getPopupOut())
            transaction.remove(fragment).commit()

        } catch(e:IllegalStateException){
            Log.i(appTag, "onClosePopup ${e.message}")
        }
        pageAppViewModel.event.value = PageEvent(PageEventType.REMOVE_POPUP, pageObject.pageID, pageObject.params)
    }

    @CallSuper
    override fun onAddedPage(pageObject: PageObject){
        pageAppViewModel.event.value =
            if(pageObject.isPopup) PageEvent(PageEventType.ADDED_POPUP, pageObject.pageID, pageObject.params)
            else PageEvent(PageEventType.CHANGED_PAGE, pageObject.pageID, pageObject.params)
        getPageFragment(activityModel.currentPageObject)?.onPageAdded(pageObject)
        popups.forEach { getPageFragment(it)?.onPageAdded( pageObject ) }
    }

    @CallSuper
    override fun onRemovedPage(pageObject: PageObject){
        if(pageObject.isPopup) pageAppViewModel.event.value = PageEvent(PageEventType.REMOVED_POPUP, pageObject.pageID, pageObject.params)
        getPageFragment(activityModel.currentPageObject)?.onPageRemoved(pageObject)
        popups.forEach { getPageFragment(it)?.onPageRemoved(pageObject) }
    }

    @CallSuper
    override fun onEvent(pageObject: PageObject, type:String, data:Any?){
        val eventType = PageEventType.EVENT
        eventType.id = type
        pageAppViewModel.event.value = PageEvent(eventType, pageObject.pageID, data)
        getPageFragment(activityModel.currentPageObject)?.onPageEvent(pageObject, type, data)
        popups.forEach { getPageFragment(it)?.onPageEvent(pageObject, type, data) }
    }

    /*
    Animation
     */
    @AnimRes protected open fun getPageStart(): Int { return android.R.anim.fade_in }
    @AnimRes protected open fun getPageIn(isBack:Boolean): Int { return if(isBack) android.R.anim.fade_in else android.R.anim.fade_out}
    @AnimRes protected open fun getPageOut(isBack:Boolean): Int { return if(isBack) android.R.anim.fade_out else android.R.anim.fade_in }
    @AnimRes protected open fun getPopupIn(): Int { return android.R.anim.fade_in }
    @AnimRes protected open fun getPopupOut(): Int { return android.R.anim.fade_in }

    /*
    Permission
    */
    open fun hasPermissions(permissions: Array<out String>): Pair<Boolean, List<Boolean>>? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return null
        val permissionResults = ArrayList< Boolean >()
        var resultAll = true
        for (permission in permissions) {
            val grant =  checkSelfPermission( permission ) == PackageManager.PERMISSION_GRANTED
            permissionResults.add ( grant )
            if( !grant ) resultAll = false
        }
        return Pair(resultAll, permissionResults )
    }

    open fun requestPermission(permissions: Array<out String>, requester:PageRequestPermission )
    {
        val grantResult = currentRequestPermissions.size
        currentRequestPermissions[ grantResult ] = requester
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            requestPermissionResult( grantResult, true )
            return
        }
        hasPermissions(permissions)?.let {
            if ( !it.first ) requestPermissions( permissions, grantResult) else requestPermissionResult(grantResult, true )
        }
    }
    private fun requestPermissionResult(requestCode: Int, resultAll:Boolean , permissions: List<Boolean>? = null )
    {
        currentRequestPermissions[ requestCode ]?.onRequestPermissionResult(resultAll, permissions)
        currentRequestPermissions.remove(requestCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        hasPermissions(permissions)?.let { requestPermissionResult(requestCode, it.first, it.second) }
    }



}