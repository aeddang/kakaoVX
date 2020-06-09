package com.lib.page

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import kotlinx.coroutines.*

abstract class PageFragmentCoroutine: PageFragment(), PageViewCoroutine{

    protected val scope = PageCoroutineScope()

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scope.createJob()
        onCoroutineScope()
        scope.launch {
            delay(transactionTime)
            onTransactionCompleted()
        }
    }

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        scope.destoryJob()
    }
}