package com.example.courseschedule.data

data class Course(

    val id:String,
    val name:String,
    val teacher:String,
    val room:String,

    // 星期几
    val dayOfWeek:Int,
    // 第几节开始
    val startSection:Int,
    // 第几节结束
    val endSection:Int,
    // 上课周
    val weeks:List<Int>
)