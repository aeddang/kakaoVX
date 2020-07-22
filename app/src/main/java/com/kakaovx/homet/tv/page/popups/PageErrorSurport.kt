package com.kakaovx.homet.tv.page.popups

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.viewmodel.BasePageViewModel
import com.kakaovx.homet.tv.page.viewmodel.PageError
import com.kakaovx.homet.tv.page.viewmodel.PageID
import com.kakaovx.homet.tv.page.viewmodel.VideoError
import com.kakaovx.homet.tv.store.api.ApiCode
import com.kakaovx.homet.tv.store.api.ApiError
import com.lib.page.PageEvent
import com.lib.page.PageEventType
import com.lib.page.PageFragmentCoroutine
import com.lib.page.PageNetworkStatus
import com.skeleton.module.ViewModelFactory
import com.skeleton.page.PageErrorSupportFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.page_error.*
import javax.inject.Inject

class PageErrorSurport : PageFragmentCoroutine(){
    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    protected lateinit var viewModel: BasePageViewModel

    override fun getLayoutResID(): Int = R.layout.page_error
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(BasePageViewModel::class.java)
        pageViewModel = viewModel
    }

    override fun onDestroyView() {
        super.onDestroyView()
        apiError = null
        redirectPage = null
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.presenter.loaded()
        setErrorContent()

        btnConfirm.setOnClickListener{
            if( redirectPage != null) viewModel.pageChange( redirectPage!! )
            else {
                viewModel.goBack()
                pageObject?.let {
                    delegate?.onEvent(it, eventID, ErrorActionType.Confirm )
                }
            }
        }
        btnRetry.setOnClickListener{
            if( redirectPage != null) viewModel.pageChange( redirectPage!! )
            else {
                viewModel.goBack()
                pageObject?.let {
                    delegate?.onEvent(it, eventID, ErrorActionType.Retry )
                }
            }
        }
        btnFinish.setOnClickListener {
            viewModel.repo.pagePresenter.finishApp()
        }
    }

    override fun onPageParams(params: Map<String, Any?>) {
        super.onPageParams(params)
        apiError = params[API_ERROR] as? ApiError<*>
        pageError = params[PAGE_ERROR] as? PageError<*>
        redirectPage = params[REDIRECT_PAGE] as? PageID
        eventID = params[PAGE_EVENT_ID] as? String ?: eventID

    }

    private var eventID:String = ""
    private var apiError:ApiError<*>? = null
    private var pageError:PageError<*>? = null
    private var redirectPage: PageID? = null

    private fun setErrorContent() {
        context ?: return
        var msg =  ""
        var actionType:ErrorActionType = ErrorActionType.Retry
        var msgData : Pair<String, ErrorActionType>? = null
        apiError?.let { msgData = getApiErrorMessage(it.code, it.msg, context!!, viewModel.observable.networkStatus.value) }
        pageError?.let { msgData = getErrorMessage(it.type , it.code, it.msg, context!!, viewModel.observable.networkStatus.value) }
        msg = msgData?.first ?: ""
        actionType = msgData?.second ?: ErrorActionType.Confirm
        title.text = msg

        btnConfirm.visibility = View.GONE
        btnRetry.visibility = View.GONE
        btnFinish.visibility = View.GONE

        when(actionType){
            ErrorActionType.Retry -> {
                btnRetry.visibility = View.VISIBLE
            }
            ErrorActionType.Confirm -> {
                btnConfirm.visibility = View.VISIBLE
            }
            ErrorActionType.Finish -> {
                btnFinish.visibility = View.VISIBLE
            }
        }
    }

    enum class ErrorActionType(@StringRes val msg:Int) {
        Retry(R.string.btn_action_retry),
        Finish(R.string.btn_action_finish),
        Confirm(R.string.btn_action_confirm)
    }


    companion object {
        const val API_ERROR = "apiError"
        const val PAGE_ERROR = "pageError"
        const val PAGE_EVENT_ID = "pageEventID"
        const val REDIRECT_PAGE = "redirectPage"

        fun getErrorMessage(
            type: Any?,
            code: String?,
            msg: String? = null,
            context: Context,
            networkStatus: PageNetworkStatus? = PageNetworkStatus.UNDEFINED
        ): Pair<String, ErrorActionType> {

            return if(networkStatus == PageNetworkStatus.LOST)
                Pair(
                    msg ?: context.getString(R.string.error_network_client),
                    ErrorActionType.Retry
                )
            else when (type) {
                VideoError.HOST -> Pair(
                    context.getString(R.string.error_host) + "-" + code,
                    ErrorActionType.Confirm
                )
                VideoError.PLAY_BACK -> Pair(
                    context.getString(R.string.error_play_back) + "-" + code,
                    ErrorActionType.Confirm
                )
                else -> Pair(
                    "$msg-$code",
                    ErrorActionType.Confirm
                )
            }
        }

        fun getApiErrorMessage(
            code: String?,
            msg: String? = null,
            context: Context,
            networkStatus: PageNetworkStatus? = PageNetworkStatus.UNDEFINED
        ): Pair<String, ErrorActionType> {

            when (code) {
                ApiCode.ERROR_NO_DATA,
                ApiCode.ERROR_USER_UNDEFINED,
                ApiCode.ERROR_UPLOAD_FAIL,
                ApiCode.ERROR_REQUEST_FIELD,
                ApiCode.ERROR_REQUEST_WRONG,
                ApiCode.ERROR_JWT_WRONG,
                ApiCode.ERROR_JWT_REFRESH,
                ApiCode.ERROR_JWT_UNDEFINED,
                ApiCode.ERROR_JWT_ACCESS_DENIED,
                ApiCode.ERROR_NOT_FOUND,
                ApiCode.ERROR_SERVER,
                ApiCode.ERROR_UNDEFINED -> {
                    return Pair(
                        context.getString(R.string.error_api) + "-" + code,
                        ErrorActionType.Confirm
                    )
                }
                ApiCode.ERROR_STRING -> {
                    return Pair(
                        context.getString(R.string.error_string),
                        ErrorActionType.Confirm
                    )
                }
                ApiCode.ERROR_MAINTAINANCE -> {
                    return Pair(
                        context.getString(R.string.error_check_service),
                        ErrorActionType.Finish
                    )
                }
                ApiCode.ERROR_NO_SERVICE -> {
                    return Pair(
                        context.getString(R.string.error_stop_program),
                        ErrorActionType.Confirm
                    )
                }
                else -> {
                    val codeStr = if (code == null || code == "") ApiCode.ERROR_NONE else code
                    return if(networkStatus == PageNetworkStatus.LOST)
                            Pair(
                                msg ?: context.getString(R.string.error_network_client),
                                ErrorActionType.Retry
                            )
                        else
                            Pair(
                                msg ?: (context.getString(R.string.error_network_server) + "-" + codeStr),
                                ErrorActionType.Confirm
                            )
                }
            }
        }


    }
}