package com.lib.util
import com.kakaovx.homet.tv.BuildConfig

object Log {
    const val TAG = "Page :"
    var enable = 0
    fun i(tag: String, vararg objects: Any) {
        if (enable == 1) android.util.Log.i(TAG + tag, toString(*objects))
        else {
            when (BuildConfig.BUILD_TYPE) {
                "debug", "release_debug"-> android.util.Log.i(TAG + tag, toString(*objects))
            }
        }
    }

    fun d(tag: String, vararg objects: Any) {
        if (enable == 1) android.util.Log.d(TAG + tag, toString(*objects))
        else {
            when (BuildConfig.BUILD_TYPE) {
                "debug", "release_debug" -> android.util.Log.d(TAG + tag, toString(*objects))
            }
        }
    }

    fun w(tag: String, vararg objects: Any) {
        android.util.Log.w(TAG + tag, toString(*objects))
    }

    fun e(tag: String, vararg objects: Any) {
        android.util.Log.e(TAG + tag, toString(*objects))
    }

    fun v(tag: String, vararg objects: Any) {
        android.util.Log.v(TAG + tag, toString(*objects))
    }

    private fun toString(vararg objects: Any): String {
        val sb = StringBuilder()
        for (o in objects) {
            sb.append(o)
        }
        return sb.toString()
    }
}

