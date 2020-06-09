package com.kakaovx.homet.tv.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Base64
import android.util.DisplayMetrics
import android.view.WindowManager
import com.lib.util.Log
import java.security.MessageDigest

object AppUtil {

    const val TAG = "AppUtil"

    fun getAppVersion(context: Context): String {
        var appVersion = "unknown"
        val appPackageName = context.packageName
        try {
            val pi = context.packageManager.getPackageInfo(appPackageName, 0)
            appVersion = pi.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return appVersion
    }

    fun clearAppData(ctx: Context) {
        // clearing appTag data
        if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
            // note: it has a return value!
            val activityManager = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.clearApplicationUserData()
        } else {
            val packageName = ctx.packageName
            val runtime = Runtime.getRuntime()
            runtime.exec("pm clear $packageName")
        }
    }

    fun goStore(context: Context){
        val appPackageName = context.packageName
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")
                )
            )
        } catch (e: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                )
            )
        }
    }



    @SuppressLint("PackageManagerGetSignatures")
    fun getApplicationSignature(context:Context) {
        val packageName: String = context.packageName
        val signatureList: List<String>
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val sig = context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES).signingInfo
                if (sig.hasMultipleSigners()) {
                    // Send all with apkContentsSigners
                    sig.apkContentsSigners.map {
                        val digest = MessageDigest.getInstance("SHA")
                        digest.update(it.toByteArray())
                        Log.d(TAG, "ApplicationSignature ${Base64.encodeToString(digest.digest(), Base64.NO_WRAP)}")
                        Log.d(TAG, "ApplicationSignature ${byte2HexFormatted(digest.digest())}")

                    }
                } else {
                    sig.signingCertificateHistory.map {
                        val digest = MessageDigest.getInstance("SHA")
                        digest.update(it.toByteArray())
                        Log.d(TAG, "ApplicationSignature ${Base64.encodeToString(digest.digest(), Base64.NO_WRAP)}")
                        Log.d(TAG, "ApplicationSignature ${byte2HexFormatted(digest.digest())}")

                    }
                }
            } else {
                val sig = context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures
                sig.map {
                    val digest = MessageDigest.getInstance("SHA")
                    digest.update(it.toByteArray())
                    Log.d(TAG, "ApplicationSignature ${Base64.encodeToString(digest.digest(), Base64.NO_WRAP)}")
                    Log.d(TAG, "ApplicationSignature ${byte2HexFormatted(digest.digest())}")

                }
            }

        } catch (e: Exception) {
            // Handle error
        }

    }

    fun byte2HexFormatted(arr: ByteArray): String {
        val str = StringBuilder(arr.size * 2)
        for (i in arr.indices) {
            var h = Integer.toHexString(arr[i].toInt())
            val l = h.length
            if (l == 1) h = "0$h"
            if (l > 2) h = h.substring(l - 2, l)
            str.append(h.toUpperCase())
            if (i < arr.size - 1) str.append(':')
        }
        return str.toString()
    }

    fun debugDisplayInfo(activity: Activity) {
        activity.run {
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            Log.d(TAG, "device[${Build.DEVICE}, model[${Build.MODEL}]")
            Log.d(TAG, "density[${metrics.density}], densityDpi[${metrics.densityDpi}], scaledDensity[${metrics.scaledDensity}]")
            Log.d(TAG, "widthPixel[${metrics.widthPixels}], heightPixel[${metrics.heightPixels}]")
            Log.d(TAG, "xdpi[${metrics.xdpi}], ydpi[${metrics.ydpi}]")
        }
    }

    fun setStatusBarTranslucent(activity: Activity, makeTranslucent: Boolean) {
        activity.apply {
            if (makeTranslucent) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            }
        }
    }

    fun isWifi(context: Context): Boolean {
        val cm = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        if (null != activeNetwork) {
            if (activeNetwork.type == ConnectivityManager.TYPE_WIFI)
                return true
        }
        return false
    }

    fun getNetworkInfo(context: Context): NetworkInfo?  {
        val cm = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
         return cm.activeNetworkInfo
    }

    fun isNetworkConnected(context: Context): Boolean  {
        val activeNetwork = getNetworkInfo( context )
        activeNetwork ?: return false
        return activeNetwork.isConnected
    }

    fun getNetworkOperatorName(context: Context): String {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return tm.networkOperatorName ?: "get networkOperatorName fail"
    }

    fun getNetworkOperator(context: Context): String {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return tm.networkOperator ?: "get networkOperator fail"
    }


    fun getCellMCC(context: Context): Int {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val operator = tm.networkOperator
        var mcc = 0
        if (operator != null && operator.isNotEmpty()) {
            try {
                val strmcc = operator.substring(0, 3)
                mcc = Integer.parseInt(strmcc)
            } catch (e: NumberFormatException) {
                e.printStackTrace()
                mcc = 0
            }
        }
        return mcc
    }

    fun getCellMNC(context: Context): Int {
        val tm = context
            .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val operator = tm.networkOperator
        var mnc = 0

        if (operator != null && operator.isNotEmpty()) {
            try {
                val strmnc = operator.substring(3, 5)
                mnc = Integer.parseInt(strmnc)
            } catch (e: NumberFormatException) {
                e.printStackTrace()
                mnc = 0
            }

        }

        return mnc
    }

    @SuppressLint("PrivateApi")
    fun getDebugLevel(): Int {
        return try {
            val systemProperties = Class.forName("android.os.SystemProperties")
            val md = systemProperties.getMethod("get", String::class.java, String::class.java)
            val result = md.invoke(systemProperties, "persist.sys.homet.debug", "0") as String
//            Log.e(appTag, "getDebugLevel() value [$result]")
            if (result != "1") 0
            else 1
        } catch (e: Exception) {
            Log.e(TAG, "getDebugLevel() exception ${e.message}")
            0
        }
    }
}
