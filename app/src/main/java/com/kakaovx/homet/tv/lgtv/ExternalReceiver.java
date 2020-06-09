package com.kakaovx.homet.tv.lgtv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kakaovx.homet.tv.lgtv.utils.LogUtil;


/**
 * title : 외부연동 요청 수신 리시버
 *
 * description :
 * 연동 규격서 참고
 *
 * @author sky3098@pineone.com
 * @since 2019-01-07 오후 1:59
 */
public class ExternalReceiver extends BroadcastReceiver {

     @Override
    public void onReceive(final Context context, Intent intent) {

        LogUtil.d(LogUtil.DEBUG_LEVEL_3, intent.getAction() == null ? "intent.getAction() is null"
                : "action name :" + intent.getAction());

        OMAReceiver.sendAppVersionCheck(context,  true);
    }

}
