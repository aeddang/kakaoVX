package com.kakaovx.homet.tv.store.api.homet

import androidx.lifecycle.LifecycleOwner
import com.kakaovx.homet.tv.store.api.ApiField
import com.kakaovx.homet.tv.store.api.ApiPath
import com.kakaovx.homet.tv.store.api.HomeTResponse
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
data class HometApiData(val owner: LifecycleOwner,
                        val type:HometApiType,
                        val params:Map<String, Any?>? = null )

enum class HometApiType{
    CATEGORY,
    PROGRAMS,PROGRAMS_RECENT,PROGRAM,
    PROGRAM_EXERCISE
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
    ): HomeTResponse< List<ProgramData>? >?

    @GET(ApiPath.HOMET_API_PROGRAMS_RECENT)
    suspend fun getProgramsRecent(
        @Query(ApiField.DEVICE_KEY) deviceKey: String?
    ): HomeTResponse<ProgramList?>?

    @GET(ApiPath.HOMET_API_PROGRAM)
    suspend fun getProgram(
        @Path(ApiField.PROGRAM_ID) programId: String?,
        @Query(ApiField.DEVICE_KEY) deviceKey: String?
    ): HomeTResponse<ProgramData?>?

    @GET(ApiPath.HOMET_API_PROGRAM_EXERCISE)
    suspend fun getProgramExercise(
        @Path(ApiField.PROGRAM_ID) programId: String?,
        @Query(ApiField.DEVICE_KEY) deviceKey: String?
    ): HomeTResponse< List<ExerciseData>?>?
}