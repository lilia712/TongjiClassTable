package com.example.courseschedule.data
import com.google.gson.Gson
object TongjiMapper {
    private val gson = Gson()
    fun convert(
        json:String
    ):List<Course>{
        val response =
            gson.fromJson(
                json,
                TongjiResponse::class.java
            )

        val result = mutableListOf<Course>()

        response.data.forEach { course ->
            course.timeTableList.forEach { time ->
                result.add(
                    Course(
                        id = course.teachingClassId.toString() + "_" + time.dayOfWeek.toString(),
                        name = course.courseName,
                        teacher = course.teacherName,
                        room = if(time.roomIdI18n.isNotEmpty()) time.roomIdI18n
                            else "待定",
                        dayOfWeek = time.dayOfWeek,
                        startSection = time.timeStart,
                        endSection = time.timeEnd,
                        weeks = time.weeks
                    )
                )
            }
        }
        return result
    }
}