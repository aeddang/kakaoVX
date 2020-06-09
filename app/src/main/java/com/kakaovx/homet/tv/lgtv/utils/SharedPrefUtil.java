package com.kakaovx.homet.tv.lgtv.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.VisibleForTesting;

/**
 * title : SharedPrefUtil.class
 * <p>
 * description : 타입별 테스트
 * 아래 SAMPLE_KEY 를 참고..
 *
 * @author kimsj26@pineone.com
 * @since 2018. 7. 19.
 */

final public class SharedPrefUtil {

    public static final String PREFERENCE_NAME = "SharedPrefUtil";

    /**
     * 정의 방법
     * // variable type, comment
     * enum key,
     */
    @VisibleForTesting
    public enum Key {
        // Boolean, 업그레이드 요청 여부
        LastUpgradeReq,

        // Boolean, 처음 업그레이드 날짜
        FirstUpgrade,

        // Long, 다음 업그레이드 날짜
        NextUpdateDate,
    }

    /**
     * preference에 key에 변수 Object를 저장 한다.
     * @param cxt
     * @param key
     * @param value
     */
    public static void setValue(Context cxt, Key key, Object value) {
        put(cxt, PREFERENCE_NAME, key.name(), value);
    }

    /**
     * preference의 key에 저장된 갑을 불러 온다.
     * @param cxt
     * @param key
     * @param valueIfKeyNotFound
     *  key에 값이 없을 경우에 전달 받을 값.
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getValue(Context cxt, Key key, final T valueIfKeyNotFound) {
        return (T) get(cxt, PREFERENCE_NAME, key.name(), valueIfKeyNotFound);
    }

    private static boolean put(final Context context, final String preferenceName,
                               final String key, final Object value) {
        if (null == context) {
            LogUtil.e(LogUtil.DEBUG_LEVEL_1, "context is null...");
            return false;
        }

        SharedPreferences sp = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        if (null == sp) {
            LogUtil.e(LogUtil.DEBUG_LEVEL_1, "sp is null...");
            return false;
        }
        SharedPreferences.Editor et = sp.edit();

        if (value instanceof Integer) {
            et.putInt(key, (Integer) value);
        } else if (value instanceof String) {
            et.putString(key, (String) value);
        } else if (value instanceof Boolean) {
            et.putBoolean(key, (Boolean) value);
        } else if (value instanceof Long) {
            et.putLong(key, (Long) value);
        } else if (value instanceof Float) {
            et.putFloat(key, (Float) value);
        }

        return et.commit();
    }

    private static Object get(final Context context, final String preferenceName, final String key,
                              final Object valueIfKeyNotFound) {
        if (null == context) {
            LogUtil.e(LogUtil.DEBUG_LEVEL_1, "context is null...");
            return valueIfKeyNotFound;
        }

        SharedPreferences sp = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        if (null == sp) {
            LogUtil.e(LogUtil.DEBUG_LEVEL_1, "sp is null...");
            return valueIfKeyNotFound;
        }

        if (valueIfKeyNotFound instanceof Integer) {
            return sp.getInt(key, (Integer) valueIfKeyNotFound);
        } else if (valueIfKeyNotFound instanceof String) {
            return sp.getString(key, (String) valueIfKeyNotFound);
        } else if (valueIfKeyNotFound instanceof Boolean) {
            return sp.getBoolean(key, (Boolean) valueIfKeyNotFound);
        } else if (valueIfKeyNotFound instanceof Long) {
            return sp.getLong(key, (Long) valueIfKeyNotFound);
        } else if (valueIfKeyNotFound instanceof Float) {
            return sp.getFloat(key, (Float) valueIfKeyNotFound);
        } else {
            return valueIfKeyNotFound;
        }
    }

}
