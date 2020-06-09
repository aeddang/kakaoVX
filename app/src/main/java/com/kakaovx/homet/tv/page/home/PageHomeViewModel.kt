package com.kakaovx.homet.tv.page.home

import androidx.lifecycle.ViewModel
import com.kakaovx.homet.tv.page.viewmodel.BasePageViewModel
import com.kakaovx.homet.tv.store.PageRepository
import com.lib.page.PageObject
import com.lib.page.PageViewModel
import com.skeleton.module.Repository


class PageHomeViewModel(repo: PageRepository) : BasePageViewModel( repo ) {

    override fun onCleared() {
        super.onCleared()
    }
}