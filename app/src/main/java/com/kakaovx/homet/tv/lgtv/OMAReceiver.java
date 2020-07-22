package com.kakaovx.homet.tv.lgtv;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;


import com.kakaovx.homet.tv.lgtv.utils.LogUtil;
import com.kakaovx.homet.tv.lgtv.utils.SharedPrefUtil;
import com.kakaovx.homet.tv.lgtv.utils.SystemSetting;
import com.kakaovx.homet.tv.lgtv.utils.ToastUtil;


import java.io.File;
import java.util.Date;

public class OMAReceiver extends BroadcastReceiver {

    //상용 PID
    public static final String MARKET_PID = "Q13012966598";
    public static final String COMMON_PATH_TMP = "/data/lgu_app/tmp/";

    // 7일주기 업데이트로 수정함
    public final long updatePeriod = 1000 * 60 * 60 * 24 * 7;


    private Context mContext;
    private boolean isSuccess = false;

    /**
    *검색앱 발화에 의해서 업데이트 체크를 하는 경우 체크 필드
     *  true : 검색앱 런처 호출 (LaunchedByUrlActivity, ExternalReceiver)
     *  false : booting, alarm..
     */
    private static boolean mIsUpdateChecklaunch = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        mContext = context;
        Bundle extras = intent.getExtras();
        String action = intent.getAction();

        LogUtil.d(3, "[kimsj26@] ** : "+action);

        // DESC: kimsj26@ 2018. 8. 16. 알람매니저 등록시
        if(action.contains("WAKE_UP_COMPLETED_ALARM")) {
            LogUtil.d(3, "[kimsj26@] artvstb WakeUpALARM Completed!");
            sendAppVersionCheck(context, false);
        }

        // DESC: kimsj26@ 2018. 8. 16. LIVE_UPDATE_AUTO 리시버..
        else if(action.contains("LIVE_UPDATE_AUTO")) {
            LogUtil.d(3, "[kimsj26@] artvstb LIVE_UPDATE_AUTO");
            updateLogic(extras, action);
        }

        // DESC: kimsj26@ 2018. 8. 14.
        // 1. 콜드 부팅 후 시드앱 업그레이드 존재여부 시작... (무조건)
        // 2019.02.25
        // Seed 앱 콜드 부팅 후 최초는 바로 업데이트 시도하지 말고 업데이트 시간만 결정하도록 요청 사항 전달되어 수정
        else if(action.contains("BOOT_COMPLETED") || action.contains("INITIATED")) {
            LogUtil.d(3, "[kimsj26@] artvstb Boot Completed!");
            alarmSetting();
            // sendAppVersionCheck(context, false);
        }
    }

    public static void sendAppVersionCheck(Context context, boolean isLauncher) {
        mIsUpdateChecklaunch = isLauncher;

        String versionName = "00.00.00";
        int versionCode = 00000;

        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = pinfo.versionName;
            versionCode = pinfo.versionCode;
        }
        catch (PackageManager.NameNotFoundException e) {
            versionName = "00.00.00";
            versionCode = 000000;
            e.printStackTrace();
        }

        String path = "ozstore://LIVEUPDATE_MANUAL/" + MARKET_PID;
        path += "/" + versionName;
        path += "/" + versionCode;
        path += "/" + context.getPackageName();
        path += "/com.kakaovx.homet.tv.lgtv.OMAReceiver";
        LogUtil.i(LogUtil.DEBUG_LEVEL_2, "[OMA] Uri = " + path);


        try {

            String pkgName = "com.lguplus.iptv3.updatecheck";
            String clsName = pkgName + ".UpdateService";

            Intent tmpIntent = new Intent();
            tmpIntent.setAction("ozstore.external.linked");
            tmpIntent.setComponent(new ComponentName(pkgName, clsName));
            tmpIntent.setData(Uri.parse(path));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(tmpIntent);
            }else{
                context.startService(tmpIntent);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // DESC: kimsj26@ 2018. 8. 16. 업데이트 로직만 체크
    private void updateLogic(Bundle extras, String action) {
        final String receivedPid = extras.getString("PID");

        if(receivedPid == null){ return; }

        if(receivedPid.equals(MARKET_PID) && action.equals("android.lgt.appstore.LIVE_UPDATE_AUTO")) {

            isSuccess = extras.getBoolean("IS_SUCCESS");

            if (isSuccess) {

                boolean isUpdate = extras.getBoolean("IS_UPDATE");
                int newVersionCode = extras.getInt("UPDATE_VERSION_CODE");


                if (isUpdate){
                    PackageInfo pi = null;

                    try{
                        pi = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);

                    }
                    catch (PackageManager.NameNotFoundException e){
                        e.printStackTrace();
                    }
                    String tempFile = "smart_homet_update";
                    SystemSetting.createTempFile(tempFile, "");

                    if (mIsUpdateChecklaunch) {
                        ToastUtil.makeToast(mContext, "업데이트 중입니다.");
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            LogUtil.d(LogUtil.DEBUG_LEVEL_3,"startAppUpgrade");
                            //업그레이드 시작 요청
                            startAppUpgrade(mContext, receivedPid);
                        }
                    }, 2000);

                    // DESC: kimsj26@ 2018. 8. 16. 3분 후 지움
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            File file = new File(COMMON_PATH_TMP+tempFile);
                            if(file.exists()) {
                                SystemSetting.deleteTempFile(tempFile);
                                LogUtil.e(3, "[kimsj26@] updateFile Deleted..");
                            }
                        }
                    }, 30*1000);
                }
                else {
                    LogUtil.i(LogUtil.DEBUG_LEVEL_3, "[kimsj26@] ");
                    if(mIsUpdateChecklaunch) {
                        ToastUtil.makeToast(mContext, "서비스 준비중입니다.");
                    }
                    alarmSetting();
                }
            }
            else {
                LogUtil.i(LogUtil.DEBUG_LEVEL_3, "[kimsj26@] ");
                if (mIsUpdateChecklaunch) {
                    ToastUtil.makeToast(mContext, "서비스 준비중입니다.");
                }
                alarmSetting();
            }
        }

        if (mIsUpdateChecklaunch) {
            mIsUpdateChecklaunch = false;
        }
    }

    private void alarmSetting() {
        // DESC: kimsj26@ 2018. 8. 14.
        // 2. 콜드 부팅 후 업그레이드 가능한 앱이 없을 때 알람매니저 실행
        LogUtil.d(3, "[kimsj26@] Check Update Date");
        LogUtil.d(3, "[kimsj26@] 남은 시간 알람매니저 등록..");
        Intent intent = new Intent(mContext, OMAReceiver.class);
        intent.setAction("WAKE_UP_COMPLETED_ALARM");

        // DESC: kimsj26@ 2018. 8. 14. CANCEL_CURRENT : 이전 pendingIntent 취소하고 재생성
        PendingIntent sender = PendingIntent.getBroadcast(mContext, 100, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        long term = 0L;
        // DESC: kimsj26@ 2018. 8. 16. 현재 시간이 다음 알람보다 넘겼으면...
        boolean isFirstUpgrade = SharedPrefUtil.getValue(mContext, SharedPrefUtil.Key.FirstUpgrade, true);
        LogUtil.i(3, "[kimsj26@] isFirstUpgrade : " + isFirstUpgrade);
        if (isFirstUpgrade) {
            LogUtil.d(3, "[kimsj26@] AppFirst UpReq");
            term = (long) (Math.random() * updatePeriod) + (1000 * 60 * 60);
            // term = (long)(Math.random() * updatePeriod); // + (1000 * 60 * 60); // Test 용 배포 시 사용
            LogUtil.d(3, "[kimsj26@] every 2days update");
            SharedPrefUtil.setValue(mContext, SharedPrefUtil.Key.FirstUpgrade, false);
        }
        else {
            if (System.currentTimeMillis() <= (long) SharedPrefUtil.getValue(mContext, SharedPrefUtil.Key.NextUpdateDate, 0L)) {
                // DESC: kimsj26@ 2018. 8. 16.
                // 3. 콜드 부팅 후 현재 시간이 다음 업글까지 알람까지 재등록
                term = (long) SharedPrefUtil.getValue(mContext, SharedPrefUtil.Key.NextUpdateDate, 0L) - System.currentTimeMillis();
                LogUtil.d(3, "[kimsj26@] remainDate : " + term);
            }
			else {
                LogUtil.d(3, "[kimsj26@] 2일 이상일 땐 새로운 2일로 지정..");
                term = updatePeriod;
            }
        }
        // DESC: kimsj26@ 2018. 8. 16. 다음 날짜를 저장하자..
        long updateDate = System.currentTimeMillis() + term;
        SharedPrefUtil.setValue(mContext, SharedPrefUtil.Key.NextUpdateDate, updateDate);
        SharedPrefUtil.setValue(mContext, SharedPrefUtil.Key.LastUpgradeReq, true);
        am.set(AlarmManager.RTC_WAKEUP, updateDate, sender);

        LogUtil.d(3, "[kimsj26@] update false.. Remain : " + (float) ((term) / (1000)) + "초 남음");
        LogUtil.d(3, "[kimsj26@] update : " + DateFormat.format("yyyy.MM.dd HH:mm:ss", new Date(updateDate)));
    }

    private void startAppUpgrade(Context context, String receivedPid){

        LogUtil.i(2, "[OMAReceiver] start App Upgrade");

        try {
            String pkgName = "com.lguplus.iptv3.updatecheck";
            String clsName = pkgName + ".UpdateService";
            String down = "ozstore://UPDATE/" + receivedPid;

            Intent tmpIntent = new Intent();
            tmpIntent.setAction("ozstore.external.linked");
            tmpIntent.setComponent(new ComponentName(pkgName, clsName));
            tmpIntent.setData(Uri.parse(down));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(tmpIntent);
            }else{
                context.startService(tmpIntent);
            }


        } catch (Exception e) {
            LogUtil.i(2, "[OMAReceiver] startAppUpgrade err");
            LogUtil.e(LogUtil.DEBUG_LEVEL_2, e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}


