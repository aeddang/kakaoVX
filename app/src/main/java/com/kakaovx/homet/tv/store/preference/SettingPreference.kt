package com.kakaovx.homet.tv.store.preference

import android.content.Context
import com.lib.module.CachedPreference

class SettingPreference(context: Context) : CachedPreference(context, PreferenceName.SETTING) {
    companion object {
        private const val AI_VOICE_COACH = "aiVoiceCoach"
        private const val AI_VOICE_COACH_REVIEW = "aiVoiceCoachReview"
        private const val PUSH_KEY = "push_key"
        private const val INIT_MARKETING_SHOW = "init_marketing_show"
        private const val MARKETING_PUSH_KEY = "marketing_push"
        private const val SERVICE_PUSH_KEY = "service_push"
        private const val PREV_PUSH_KEY = "prev_push_key"
        private const val EVENT_KEY = "event_key"
        private const val ACTION = "action_description"
        private const val AUTO_KEY = "auto_key"
        private const val INIT_EXERCISE_KEY = "initExerciseKey3"
        private const val PAPER_WORK_CHECK_KEY = "paper_work_check_key"
    }


    fun putAIVoiceCoach(able: Boolean) = put(AI_VOICE_COACH, able)
    fun getAIVoiceCoach(): Boolean = get(AI_VOICE_COACH, true) as Boolean

    fun putAIVoiceCoachReview(able: Boolean) = put(AI_VOICE_COACH_REVIEW, able)
    fun getAIVoiceCoachReview(): Boolean = get(AI_VOICE_COACH_REVIEW, true) as Boolean


    fun setPushKey(key: String) = put(PUSH_KEY, key)
    fun getPushKey(): String? = get(PUSH_KEY, "") as String

    fun setPrevPushKey(key: String) = put(PREV_PUSH_KEY, key)
    fun getPrevPushKey(): String = get(PREV_PUSH_KEY, "") as String

    fun setEventKey(key: String, value: Boolean = true) = put(EVENT_KEY + key, value)
    fun getEventKey(key: String): Boolean = get(EVENT_KEY + key, false) as Boolean

    //콘텐츠 설정 -> 동작설명 건너뛰기
    fun setSkipActionDescription(able: Boolean) = put(ACTION, able)
    fun getSkipActionDescription(): Boolean = get(ACTION, false) as Boolean

    //동영상 자동 재생
    fun setAutoMovieStart(able: Boolean) = put(AUTO_KEY, able)
    fun getAutoMovieStart(): Boolean = get(AUTO_KEY, true) as Boolean

    //앱 최초 실행일때, 광고성 푸시 동의 안내창
    fun setInitMarketingPushKey(key: Boolean) = put(INIT_MARKETING_SHOW, key)
    fun getInitMarketingPushKey(): Boolean = get(INIT_MARKETING_SHOW, true) as Boolean

    //광고성 정보 알림
    fun setMarketingPushKey(key: Boolean) = put(MARKETING_PUSH_KEY, key)
    fun getMarketingPushKey(): Boolean = get(MARKETING_PUSH_KEY, true) as Boolean

    //서비스 알림
    fun setServicePushKey(key: Boolean) = put(SERVICE_PUSH_KEY, key)
    fun getServicePushKey(): Boolean = get(SERVICE_PUSH_KEY, true) as Boolean

    //운동실행 여부
    fun setInitExercise(isInit: Boolean) = put(INIT_EXERCISE_KEY, isInit)
    fun getInitExercise(): Boolean = get(INIT_EXERCISE_KEY, false) as Boolean

    //문진하기 첫페이지 동의 체크 여부
    fun setPaperWorkCheck(key: Boolean) = put(PAPER_WORK_CHECK_KEY, key)
    fun getPaperWorkCheck(): Boolean = get(PAPER_WORK_CHECK_KEY, false) as Boolean

}