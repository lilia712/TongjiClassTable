package com.example.courseschedule.data

import android.content.Context
import com.google.gson.Gson
import java.io.File


class TimetableStorage(
    private val context:Context
){
    private val gson=Gson()
    private val file
        get()=File(
            context.filesDir,
            "timetable.json"
        )

    fun save(
        timetable:Timetable
    ){
        file.writeText(
            gson.toJson(timetable)
        )
    }

    fun load():Timetable?{
        if(!file.exists())
            return null

        return gson.fromJson(
            file.readText(),
            Timetable::class.java
        )
    }

    fun clear(){
        file.delete()
    }
}