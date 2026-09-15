package com.example.courseschedule.data

data class TongjiResponse(
    val code:Int,
    val msg:String,
    val data:List<TongjiCourse>
)


data class TongjiCourse(
    val teachingClassId:Long,
    val courseName:String,
    val teacherName:String,
    val timeTableList:List<TongjiTime>
)

data class TongjiTime(
    val dayOfWeek:Int,
    val timeStart:Int,
    val timeEnd:Int,
    val roomId:String,
    val roomIdI18n:String,
    val weeks:List<Int>,
    val teacherName:String
)