package com.kakaovx.homet.tv.page.error

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.viewmodel.BasePageViewModel
import com.kakaovx.homet.tv.store.PageID
import com.kakaovx.homet.tv.store.api.ApiCode
import com.kakaovx.homet.tv.store.api.ApiError
import com.kakaovx.homet.tv.store.api.homet.HometApiType
import com.skeleton.module.ViewModelFactory
import com.skeleton.page.PageErrorSupportFragment
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class PageError : PageErrorSupportFragment(){
    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    protected lateinit var viewModel: BasePageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(BasePageViewModel::class.java)
        pageViewModel = viewModel
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title = resources.getString(R.string.app_name)
        setErrorContent()
    }

    override fun onPageParams(params: Map<String, Any?>) {
        super.onPageParams(params)
        homeTApiError = params[API_ERROR] as? ApiError<HometApiType>
        redirectPage = params[REDIRECT_PAGE] as? PageID
    }

    private var homeTApiError:ApiError<HometApiType>? = null
    private var redirectPage:PageID? = null

    private fun setErrorContent() {
        context ?: return
        var msg =  ""
        var actionType:ErrorActionType = ErrorActionType.Retry
        homeTApiError?.let {
            val statusCode = it.code
            val msgData = getErrorMessage(statusCode, it.msg, context!!)
            msg = msgData.first
            actionType = msgData.second
            if(actionType == ErrorActionType.Confirm && redirectPage == null) actionType = ErrorActionType.Retry
        }
        imageDrawable = ContextCompat.getDrawable(context!!, R.drawable.lb_ic_sad_cloud)
        message = msg
        setDefaultBackground(true)
        buttonText = resources.getString(actionType.msg)
        buttonClickListener = View.OnClickListener {
            when(actionType){
                ErrorActionType.Retry -> viewModel.goBack()
                ErrorActionType.Confirm -> viewModel.pageChange( redirectPage!! )
                ErrorActionType.Finish -> viewModel.repo.pagePresenter.finishApp()
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
        const val REDIRECT_PAGE = "redirectPage"

        fun getErrorMessage(
            code: String?,
            msg: String? = null,
            context: Context
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
                    return Pair(context.getString(R.string.error_string), ErrorActionType.Confirm)
                }
                ApiCode.ERROR_MAINTAINANCE -> {
                    return Pair(context.getString(R.string.error_check_service), ErrorActionType.Finish)
                }
                ApiCode.ERROR_NO_SERVICE -> {
                    return Pair(context.getString(R.string.error_stop_program), ErrorActionType.Finish)
                }
                else -> {
                    return Pair(
                        msg ?: (context.getString(R.string.error_api) + "-" + ApiCode.ERROR_NONE),
                        ErrorActionType.Confirm
                    )
                }
            }
        }
    }


}