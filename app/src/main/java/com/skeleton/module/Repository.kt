package com.skeleton.module

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.lib.page.PageLifecycleUser
import com.skeleton.module.network.NetworkFactory

abstract class Repository(
    val ctx: Context
): PageLifecycleUser