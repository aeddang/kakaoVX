package com.kakaovx.homet.tv.store.api.homet

import androidx.lifecycle.LifecycleOwner
import com.kakaovx.homet.tv.store.api.ApiField
import com.kakaovx.homet.tv.store.api.ApiPath
import com.kakaovx.homet.tv.store.api.HomeTResponse
import retrofit2.http.*

data class HometApiData(val owner: LifecycleOwner,
                        val type:HometApiType,
                        val params:Map<String, Any?>? = null )

enum class HometApiType{
    CATEGORY,
    PROGRAMS,PROGRAMS_RECENT,PROGRAM, PROGRAM_EXERCISE,
    PROGRAM_DETAIL,
    EXERCISE,EXERCISE_MOTION,
    EXERCISE_DETAIL,
    GROUP
}


interface HometApi{

    @GET(ApiPath.HOMET_API_CATEGORY)
    suspend fun getCategory(
        @Query(ApiField.DEVICE_KEY) deviceKey: String?
    ): HomeTResponse<List<CategoryData>>

    @GET(ApiPath.HOMET_API_PROGRAMS)
    suspend fun getPrograms(
        @Query(ApiField.DEVICE_KEY) deviceKey: String?,
        @Query(ApiField.FILTER_PURPOSE) filterPurpose: String?,
        @Query(ApiField.PAGE) page: String?,
        @Query(ApiField.COUNT ) count: String?
    ): HomeTResponse< ProgramList? >?

    @GET(ApiPath.HOMET_API_PROGRAMS_RECENT)
    suspend fun getProgramsRecent(
        @Query(ApiField.DEVICE_KEY) deviceKey: String?
    ): HomeTResponse<List<ProgramData>?>?

    @GET(ApiPath.HOMET_API_PROGRAM)
    suspend fun getProgram(
        @Path(ApiField.PROGRAM_ID) programId: String?,
        @Query(ApiField.DEVICE_KEY) deviceKey: String?
    ): HomeTResponse<ProgramDetailData?>?

    @GET(ApiPath.HOMET_API_PROGRAM_EXERCISE)
    suspend fun getProgramExercise(
        @Path(ApiField.PROGRAM_ID) programId: String?,
        @Query(ApiField.DEVICE_KEY) deviceKey: String?
    ): HomeTResponse< List<ExerciseData>?>?

    @GET(ApiPath.HOMET_API_EXERCISE)
    suspend fun getExercise(
        @Path(ApiField.EXERCISE_ID) exerciseId: String?,
        @Query(ApiField.DEVICE_KEY) deviceKey: String?,
        @Query(ApiField.PROGRAM_ID) programId: String?,
        @Query(ApiField.ROUND_ID) roundId: String?
    ): HomeTResponse<ExerciseDetailData?>?


    @GET(ApiPath.HOMET_API_EXERCISE_MOTION)
    suspend fun getExerciseMotion(
        @Path(ApiField.EXERCISE_ID) exerciseId: String?,
        @Query(ApiField.DEVICE_KEY) deviceKey: String?,
        @Query(ApiField.PROGRAM_ID) programId: String?
    ): HomeTResponse<ExerciseMotionsData?>?

    @GET(ApiPath.HOMET_API_EXERCISE_PLAY)
    suspend fun getExercisePlay(
        @Path(ApiField.EXERCISE_ID) exerciseId: String?,
        @Query(ApiField.DEVICE_KEY) deviceKey: String?,
        @Query(ApiField.PROGRAM_ID) programId: String?,
        @Query(ApiField.ROUND_ID) roundId: String?
    ): HomeTResponse<ExercisePlayData?>?

    @GET(ApiPath.HOMET_API_EXERCISE_BREAK_TIME)
    suspend fun getExerciseBreakTime(
        @Path(ApiField.EXERCISE_ID) exerciseId: String?,
        @Query(ApiField.DEVICE_KEY) deviceKey: String?
    ): HomeTResponse<BreakTimeData?>?


    @FormUrlEncoded
    @POST(ApiPath.HOMET_API_MOVIE_START)
    suspend fun putExerciseStart(
        @Field(ApiField.DEVICE_KEY) deviceKey: String?,
        @Field(ApiField.EXERCISE_ID) exerciseId: String?,
        @Field(ApiField.PROGRAM_ID) programId: String?,
        @Field(ApiField.ROUND_ID) roundId: String?,
        @Field(ApiField.MOVIE_TYPE) movieType: String?,
        @Field(ApiField.PLAY_TYPE) playType: String?
    ): HomeTResponse<ExerciseStartData?>?

    @FormUrlEncoded
    @POST(ApiPath.HOMET_API_MOVIE_MOTION_END)
    suspend fun putExerciseMotionEnd(
        @Field(ApiField.DEVICE_KEY) deviceKey: String?,
        @Field(ApiField.EXERCISE_ID) exerciseId: String?,
        @Field(ApiField.PROGRAM_ID) programId: String?,
        @Field(ApiField.ROUND_ID) roundId: String?,
        @Field(ApiField.MOTION_MOVIE_ID) motionMovieId: String?,
        @Field(ApiField.MOTION_MOVIE_ROUND_ID) motionMovieRoundId: String?,
        @Field(ApiField.PLAY_ID) playId: String?,
        @Field(ApiField.MOVIE_ID) movieId: String?,
        @Field(ApiField.MOTION_ID) motionId: String?,
        @Field(ApiField.MOTION_PROGRESS) motionProgress: String?,
        @Field(ApiField.EXERCISE_TYPE) exerciseType: String?,
        @Field(ApiField.MOTION_PARTS) motionParts: String?,
        @Field(ApiField.RANGE_COUNT) rangeCount: Int?,
        @Field(ApiField.REAL_RANGE_COUNT) realRangeCount: Int?,
        @Field(ApiField.PLAY_TIME) playTime: Double?,
        @Field(ApiField.EXERCISE_TIME) exerciseTime: Double?,
        @Field(ApiField.START_TIME) startTime: Double?,
        @Field(ApiField.END_TIME) endTime: Double?,
        @Field(ApiField.ALL_MOTION_AVG)  allMotionAvg: String?,
        @Field(ApiField.BODY_POINT) bodyPoint: Float?,
        @Field(ApiField.SHOULDER_POINT) shoulderPoint: Float?,
        @Field(ApiField.ARM_POINT) armPoint: Float?,
        @Field(ApiField.CORE_POINT) corePoint: Float?,
        @Field(ApiField.HIP_POINT) hipPoint: Float?,
        @Field(ApiField.LEG_POINT) legPoint: Float?,
        @Field(ApiField.SHIN_POINT) shinPoint: Float?,
        @Field(ApiField.IS_MULTI_VIEW ) isMultiView: Boolean?,
        @Field(ApiField.MOVIE_TYPE) movieType: String?,
        @Field(ApiField.PLAY_TYPE) playType: String?
    ): HomeTResponse<Any?>?

    @FormUrlEncoded
    @POST(ApiPath.HOMET_API_MOVIE_EXERCISE_END)
    suspend fun putExerciseEnd(
        @Field(ApiField.DEVICE_KEY) deviceKey: String?,
        @Field(ApiField.EXERCISE_ID) exerciseId: String?,
        @Field(ApiField.PROGRAM_ID) programId: String?,
        @Field(ApiField.ROUND_ID) roundId: String?,
        @Field(ApiField.PLAY_ID) playId: String?,
        @Field(ApiField.EXERCISE_PROGRESS) exerciseProgress: String?,
        @Field(ApiField.EXERCISE_TYPE) exerciseType: String?,
        @Field(ApiField.PLAY_TIME) playTime: Double?,
        @Field(ApiField.EXERCISE_TIME) exerciseTime: Double?,
        @Field(ApiField.START_TIME) startTime: Double?,
        @Field(ApiField.END_TIME) endTime: Double?,
        @Field(ApiField.ALL_MOTION_AVG)  allMotionAvg: String?,
        @Field(ApiField.BODY_POINT) bodyPoint: Float?,
        @Field(ApiField.SHOULDER_POINT) shoulderPoint: Float?,
        @Field(ApiField.ARM_POINT) armPoint: Float?,
        @Field(ApiField.CORE_POINT) corePoint: Float?,
        @Field(ApiField.HIP_POINT) hipPoint: Float?,
        @Field(ApiField.LEG_POINT) legPoint: Float?,
        @Field(ApiField.SHIN_POINT) shinPoint: Float?,
        @Field(ApiField.IS_MULTI_VIEW ) isMultiView: Boolean?
    ): HomeTResponse<Any?>?

    @FormUrlEncoded
    @POST(ApiPath.HOMET_API_MOVIE_EXERCISE_EXEC)
    suspend fun putExerciseExec(
        @Field(ApiField.DEVICE_KEY) deviceKey: String?,
        @Field(ApiField.EXERCISE_ID) exerciseId: String?,
        @Field(ApiField.PROGRAM_ID) programId: String?,
        @Field(ApiField.ROUND_ID) roundId: String?,
        @Field(ApiField.PLAY_ID) playId: String?
    ): HomeTResponse<Any?>?
}