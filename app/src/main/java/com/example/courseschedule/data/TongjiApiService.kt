package com.example.courseschedule.data

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface TongjiApiService {
    @GET("api/electionservice/reportManagement/findStudentTimetab")
    suspend fun getTimetable(
        @Query("calendarId") calendarId: String,
        @Query("studentCode") studentCode: String,
        @Query("_t") timestamp: Long,
        @Header("X-Token") token: String
    ): String
}
