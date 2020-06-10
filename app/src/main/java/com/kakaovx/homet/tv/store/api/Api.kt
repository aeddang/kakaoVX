package com.kakaovx.homet.tv.store.api
import android.content.Context
import com.kakaovx.homet.tv.BuildConfig
import com.kakaovx.homet.tv.R

object ApiPath {
    /**
     * Rest Api Info
     */
    private const val VERSION_V1 = "v1"
    private const val VERSION_V2 = "v2"

    /**
     * API GROUP
     */
    private const val  GROUP_EXERCISE = "exercise"
    private const val  GROUP_MOVIES = "movies"
    private const val  GROUP_PROGRAMS = "programs"
    private const val  GROUP_RECOMMENDS = "recommends"
    private const val  GROUP_SEARCH = "search"
    private const val  GROUP_SYSTEM = "system"
    private const val  GROUP_USERS = "users"
    private const val  GROUP_JWT = "users/jwt"
    private const val  GROUP_KAKAOAPP = "user/kakao"
    private const val  GROUP_PLANNER = "planner"
    private const val  GROUP_BOOKMARKS = "bookmarks"
    private const val  GROUP_MENU = "menu"

    /**
     * API PATH
     */

    // account

    const val ACCOUNT_API_JWT = "$VERSION_V1/$GROUP_SYSTEM/jwt"
    const val ACCOUNT_API_JWT_REFRESH = "$VERSION_V1/$GROUP_SYSTEM/jwt/refresh"

    const val HOMET_API_CATEGORY = "$VERSION_V1/$GROUP_PROGRAMS/category"
    const val HOMET_API_PROGRAMS = "$VERSION_V1/$GROUP_PROGRAMS"
    const val HOMET_API_PROGRAMS_RECENT = "$VERSION_V1/$GROUP_PROGRAMS/recent"
    const val HOMET_API_PROGRAM = "$VERSION_V1/$GROUP_PROGRAMS{${ApiField.PROGRAM_ID}}"
    const val HOMET_API_PROGRAM_EXERCISE = "$VERSION_V1/$GROUP_PROGRAMS{${ApiField.PROGRAM_ID}}/exercise"
}

data class ApiEvent<T>(val type:T , val data:Any?, val id: String? = null)
data class ApiError<T>(val type:T , val code:String, val msg:String?, val id: String? = null)

object ApiValue{

    const val  OS_TYPE = BuildConfig.OS_TYPE
    const val  PAGE_COUNT = "20"
    const val  PAGE_START = "1"

    enum class FilterPurpose(val value: String) {
        All("01"),
        Weight("02"),
        BodyCare("03"),
        Flexibility("04"),
        Posture("05"),
        Strength("06"),
        Belly("09"),
    }

    enum class WakeupStatus(val value: String) {
        Start("0"),
        Continue("1"),
        End("2")
    }

    enum class MovieType(val value: String) {
        Ai("52"),
        Ar("53")
    }

    enum class MoviePlayType(val value: String) {
        All("0"),
        Unit("1")
    }

    enum class UrlType(val value: String) {
        Html("0"),
        Url("1"),
        CustomHtml("2")
    }
}
object ApiCode{
    const val  SUCCESS = "E000" //성공
    const val  ERROR_NO_DATA = "ED001" // 결과값이 없습니다 ** 정상으로 받은결과값 data가 null일때
    const val  ERROR_USER_UNDEFINED = "E001" // 카카오 Access Token이 맞지가 않습니다.
    const val  ERROR_STRING = "E802" //사용할 수 없는 단어가 포함되어 있습니다.
    const val  ERROR_UPLOAD_FAIL = "E899" //파일 업로드에 실패하였습니다.
    const val  ERROR_REQUEST_FIELD = "E900" //필수 필드값이 없습니다.
    const val  ERROR_REQUEST_WRONG = "E994" //잘못된 요청입니다.
    const val  ERROR_JWT_WRONG = "E993" //정보 조회 실패하였습니다. 없는 JWT
    const val  ERROR_JWT_REFRESH = "E995" //만료된 인증키입니다.  JWT 갱신
    const val  ERROR_JWT_UNDEFINED = "E996" //인증 토큰이 누락되었습니다.
    const val  ERROR_JWT_ACCESS_DENIED = "E997" //서버 인증에 실패하였습니다.
    const val  ERROR_NOT_FOUND = "E998" //찾을수 없는 주소입니다.
    const val  ERROR_SERVER = "E999" //서버 오류가 있습니다.
    const val  ERROR_UNDEFINED = "E100" //알수 없는 오류가 발생하였습니다.
    const val  ERROR_NONE = "E1101" //서버에서 정의안된 오류.
    const val  ERROR_PARSE = "E1102" //결과값 parse중 오류.
    const val  ERROR_MAINTAINANCE = "E898" //점검중
    const val  ERROR_NO_SERVICE = "E300" //서비스 제공이 잠시 중단된 프로그램 입니다.
}

object ApiField{
    const val DEVICE_KEY = "deviceKey"
    const val PRE_JWT = "preJwt"
    const val NICK_NAME = "nickName"
    const val PROFILE_URL = "profileUrl"
    const val PROFILE_IMG = "profileImg"
    const val APP_VERSION = "appVersion"
    const val OS_TYPE = "osType"
    const val PUSH_KEY = "pushKey"
    const val STAY_TYPE = "stayType"
    const val USIM_CORP = "usimCorp"
    const val WAKEUP_IDX = "wakeupIdx"
    const val PHONE = "phone"
    const val EMAIL = "kakaoEmail"
    const val BIRTH = "birth"
    const val BIRTH_YEAR = "birthYear"
    const val TERMS_MARKETING = "termsMarketing"
    const val TERMS_PRIVACY = "termsPrivacy3rd"
    const val REASON_CODE = "reasonCode"
    const val ETC_REASON = "etcReason"
    const val PROGRAM_ID = "programId"
    const val EXERCISE_ID = "exerciseId"
    const val ROUND_ID = "roundId"
    const val EXERCISE_IDS = "exerciseIds"
    const val TRAINER_HOME_ID = "trainerHomeId"
    const val SORT_TYPE = "sortType"
    const val FILTER_EXERCISE_TYPE = "filterExerciseType"
    const val FILTER_BODY_PARTS = "filterBodyParts"
    const val FILTER_EXERCISE_MOVIE_TYPE = "filterExerciseMovieType"
    const val FILTER_PURPOSE = "filterPurpose"
    const val FILTER_CLASS = "filterClass"
    const val FILTER_PROGRAM_LABEL = "filterProgramLabel"
    const val FILTER_DIFFICULTY = "filterDifficulty"
    const val FILTER_PLAY_TIME = "filterPlayTime"
    const val FILTER_COUNT = "filterCount"
    const val PAGE = "page"
    const val COUNT = "count"
    const val TYPE = "type"
    const val MOVIETYPE = "movieType"
    const val MOVIE_PLAYTYPE = "playType"
    const val MOTION_MOVIE_ROUND_ID = "motionMovieRoundId"
    const val PLAN_ID = "planId"
    const val SEARCH_DATA = "searchDate"
    const val EXERCISE_START_DATE = "exerciseStartDate"
    const val EXERCISE_WEEK = "exerciseWeek"
    const val IS_ALARM = "isAlarm"
    const val EXERCISE_ALARM_TIME = "exerciseAlarmTime"
    const val ALARM_METHOD = "alarmMethod"
    const val IS_RELAY_PLAY = "isRelayPlay"
    const val RELAY_POINT = "relayPoint"
    const val PLAY_ID = "playId"
    const val MOVIE_ID = "movieId"
    const val MOTION_ID = "motionId"
    const val MOTION_MOVIE_ID = "motionMovieId"
    const val  MOTION_PROGRESS = "motionProgress"
    const val  EXERCISE_TYPE = "exerciseType"
    const val  MOTION_PARTS = "motionParts"
    const val  EXERCISE_PROGRESS = "exerciseProgress"
    const val  RANGE_COUNT = "rangeCount"
    const val  REAL_RANGE_COUNT = "realRangeCount"
    const val  PLAY_TIME = "playTime"
    const val  EXERCISE_TIME = "exerciseTime"
    const val  START_TIME = "startTime"
    const val  END_TIME = "endTime"
    const val  ALL_MOTION_AVG = "allMotionAvg"
    const val  BODY_POINT = "bodyPoint"
    const val  SHOULDER_POINT = "shoulderPoint"
    const val  ARM_POINT = "armPoint"
    const val  CORE_POINT = "corePoint"
    const val  HIP_POINT = "hipPoint"
    const val  LEG_POINT = "legPoing"
    const val  SHIN_POINT = "shinPoint"
    const val  IS_MULTI_VIEW = "isMultiView"
    const val  FEELING = "feeling"
    const val  STARTDATE = "startDate"
    const val  ENDDATE = "endDate"
    const val  SEARCHTYPE = "searchType"
    const val  MOTIONS = "motions"
    const val  MEUNDATETIME = "menuDateTime"
    const val  DETECTINFO = "detectInfo"
    const val  TOTALCAL = "totalCal"
    const val  FOODS = "foods"
    const val  MENUIDX = "menuIdx"
    const val  PAGETYPE = "pageType"
    const val  PRICE = "price"
    const val  DISCOUNT_PRICE = "discountPrice"
    const val  DISCOUNT_PER = "discountPricePercent"
    const val  SEARCH_TEXT = "searchText"
    const val  SEARCH_KEYWORD_INDEX = "keywordIdx"
    const val  REVIEW = "review"
    const val  REVIEW_ID = "reviewId"
    const val  CANCEL_CODE = "cancelCode"
    const val  CANCEL_TEXT = "cancelText"
    const val  CANCEL_ETC = "cancelEtcText"
    const val  NOTICE_ID = "noticeId"
    const val  FAQ_ID = "faqId"
    const val  EVENT_ID = "eventId"
    const val  GENDER = "gender"
    const val  AGE = "age"
    const val  HEIGHT = "height"
    const val  WEIGHT = "weight"
    const val  BMI = "bmi"
    const val  PURPOSE_ID = "purposeId"
    const val  BODY_TYPE_ID = "bodyTypeId"
    const val  EXERCISE_BODYPARTS_ID = "exerciseBodyPartsId"
    const val  ACTIVE_ID = "activeId"
    const val  TITLE = "title"
    const val  CONTENT = "content"
    const val  DEVICE_MODEL = "deviceModel"
    const val  APP_TYPE = "appType"
    const val  IP = "requestIp"
    const val  KEY = "key"
    const val  ACCESS_KEY = "access_key"
    const val  EXPIRE = "expire"
    const val  MARKETING_PUSH = "marketingPush"
    const val  SERVICE_PUSH = "servicePush"
    const val  BITMAP = "bitmap"
    const val  FOODVERSION = "foodsVersion"
    const val  CARDTYPE = "cardType"
    const val  PHONECORP = "phoneCorp"
    const val  BILL_MASTER_INDEX = "billingMasterIdx"
    const val  BILL_INDEX = "billingIdx"
    const val  DELETE_MEMO = "deleteMemo"
    const val  MENU_IDX = "menuIdx"
    const val  GOAL_CAL = "goalCal"
    const val  GOAL_CAL_TYPE = "goalCalType"
}

