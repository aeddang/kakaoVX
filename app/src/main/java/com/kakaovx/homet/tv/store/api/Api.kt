package com.kakaovx.homet.tv.store.api
import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.kakaovx.homet.tv.BuildConfig
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.store.api.homet.HometApiType
import java.util.ArrayList

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
    private const val  GROUP_SYSTEM = "system"

    /**
     * API PATH
     */
    const val ACCOUNT_API_JWT = "$VERSION_V1/$GROUP_SYSTEM/jwt"
    const val ACCOUNT_API_JWT_REFRESH = "$VERSION_V1/$GROUP_SYSTEM/jwt/refresh"

    const val HOMET_API_CATEGORY = "$VERSION_V1/$GROUP_PROGRAMS/category"

    const val HOMET_API_PROGRAMS = "$VERSION_V1/$GROUP_PROGRAMS"
    const val HOMET_API_PROGRAMS_RECENT = "$VERSION_V1/$GROUP_PROGRAMS/recent"
    const val HOMET_API_PROGRAM = "$VERSION_V1/$GROUP_PROGRAMS/{${ApiField.PROGRAM_ID}}"
    const val HOMET_API_PROGRAM_EXERCISE = "$VERSION_V1/$GROUP_PROGRAMS/{${ApiField.PROGRAM_ID}}/exercise"

    const val HOMET_API_EXERCISE = "$VERSION_V1/$GROUP_EXERCISE/{${ApiField.EXERCISE_ID}}"
    const val HOMET_API_EXERCISE_MOTION = "$VERSION_V1/$GROUP_EXERCISE/{${ApiField.EXERCISE_ID}}/motion"
    const val HOMET_API_EXERCISE_PLAY = "$VERSION_V1/$GROUP_EXERCISE/{${ApiField.EXERCISE_ID}}/play"
    const val HOMET_API_EXERCISE_BREAK_TIME = "$VERSION_V1/$GROUP_EXERCISE/{${ApiField.EXERCISE_ID}}/breaktime"

    const val HOMET_API_MOVIE_START = "$VERSION_V1/$GROUP_MOVIES/play/start"
    const val HOMET_API_MOVIE_MOTION_END = "$VERSION_V1/$GROUP_MOVIES/play/motion/end"
    const val HOMET_API_MOVIE_EXERCISE_END = "$VERSION_V1/$GROUP_MOVIES/play/exercise/end"
    const val HOMET_API_MOVIE_EXERCISE_EXEC = "$VERSION_V1/$GROUP_MOVIES/play/exercise/exec"

    const val WECANDEO_ADDRESS = "http://api.wecandeo.com/"

    const val STORAGE_GUIDE = "tutorial/tv_guide.json"
}

data class ApiSuccess<T>(val type:T, var data:Any?, val id: String? = null)
data class ApiError<T>(val type:T , val code:String?, val msg:String? = null, val id: String? = null)
data class ApiGroup<T>(val type:T, var group: ArrayList<ApiSuccess<T>>, var complete:Int, var params:ArrayList<Map<String, Any?>?>? = null,
                       val isSerial:Boolean = false, val owner: LifecycleOwner? = null){
        var process:Int = 0 ;private set
        fun finish():Boolean{
            complete --
            process ++
            return complete <= 0
        }
    }

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
    const val  ERROR_MAINTAINANCE = "E898" //점검중
    const val  ERROR_NO_SERVICE = "E300" //서비스 제공이 잠시 중단된 프로그램 입니다.
}

object ApiField{
    const val DEVICE_KEY = "deviceKey"
    const val PRE_JWT = "preJwt"
    const val PROGRAM_ID = "programId"
    const val EXERCISE_ID = "exerciseId"
    const val ROUND_ID = "roundId"
    const val FILTER_PURPOSE = "filterPurpose"
    const val PAGE = "page"
    const val COUNT = "count"
    const val MOVIE_TYPE = "movieType"
    const val PLAY_TYPE = "playType"
    const val MOTION_MOVIE_ROUND_ID = "motionMovieRoundId"
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
    const val  KEY = "key"
    const val  ACCESS_KEY = "access_key"
    const val  EXPIRE = "expire"

}

