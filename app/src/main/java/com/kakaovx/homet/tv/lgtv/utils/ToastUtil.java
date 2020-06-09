package com.kakaovx.homet.tv.lgtv.utils;

import android.content.Context;
import android.widget.Toast;


/**
 * title : Toast 를 표시 한다.
 * description :
 *
 * @author sky3098@pineone.com
 * @since 2018-09-14 오후 1:09
 */
public class ToastUtil {
    private static Toast mToast = null;

    public static void makeToast(Context context, int resId) {
        makeToast(context, context.getString(resId));
    }

    public static void makeToast(Context context, String resId) {
        if (mToast == null) {
            mToast = Toast.makeText(context, resId, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(resId);
        }
        mToast.show();

        // 5초동안 1초씩 감소
        /*new CountDownTimer(6000, 1000) {
            public void onTick(long millisUntilFinished) {
                mToast.show();
            }

            public void onFinish() {
                mToast.show();
            }
        }.start();*/
    }
}
