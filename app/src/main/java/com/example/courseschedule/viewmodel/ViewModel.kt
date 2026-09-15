package com.example.courseschedule.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.courseschedule.data.Course
import com.example.courseschedule.data.TimetableStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject


class TimetableViewModel(
    private val storage: TimetableStorage,
    context: Context
): ViewModel() {
    private val appContext = context.applicationContext
    private val prefs =
        appContext.getSharedPreferences(
            "timetable_prefs",
            Context.MODE_PRIVATE
        )

    //全部课程
    private val _courses =
        MutableStateFlow<List<Course>>(emptyList())
    val courses:StateFlow<List<Course>>
        get() = _courses

    //当前周
    private val _currentWeek =
        MutableStateFlow(1)

    val currentWeek:StateFlow<Int>
        get()=_currentWeek

    //开学日期（第一周周一的 0 点时间戳，0 表示未设置）
    private val _semesterStart =
        MutableStateFlow(
            prefs.getLong("semester_start", 0L)
        )
    val semesterStart:StateFlow<Long>
        get()=_semesterStart

    init {
        loadTimetable()
        if (_semesterStart.value > 0L) {
            _currentWeek.value = weekFor(System.currentTimeMillis())
        }
        loadCourseTimes()
    }

    // 每节课时间段 (section -> "HH:mm-HH:mm")
    private val _courseTimes =
        MutableStateFlow<Map<Int, String>>(emptyMap())
    val courseTimes: StateFlow<Map<Int, String>>
        get() = _courseTimes

    private fun loadCourseTimes() {
        val json = prefs.getString("course_times", null) ?: return
        try {
            val obj = JSONObject(json)
            val map = mutableMapOf<Int, String>()
            for (key in obj.keys()) {
                map[key.toInt()] = obj.getString(key)
            }
            _courseTimes.value = map
        } catch (_: Exception) {}
    }

    fun setCourseTime(section: Int, startHH: String, startMM: String, endHH: String, endMM: String) {
        val value = "$startHH:$startMM-$endHH:$endMM"
        val newMap = _courseTimes.value.toMutableMap().apply {
            put(section, value)
        }
        _courseTimes.value = newMap
        val obj = JSONObject()
        newMap.forEach { (k, v) -> obj.put(k.toString(), v) }
        prefs.edit().putString("course_times", obj.toString()).commit()
    }

    // 固定课程时长（分钟），null 表示未启用
    private val _fixedDuration =
        MutableStateFlow(
            prefs.getInt("fixed_duration", -1).takeIf { it > 0 }
        )
    val fixedDuration: StateFlow<Int?>
        get() = _fixedDuration

    fun setFixedDuration(minutes: Int?) {
        _fixedDuration.value = minutes
        val editor = prefs.edit()
        if (minutes == null) editor.remove("fixed_duration")
        else editor.putInt("fixed_duration", minutes)
        editor.commit()
    }

    /**
     * 从本地json读取
     */
    fun loadTimetable(){
        viewModelScope.launch {
            val timetable =
                storage.load()
            if(timetable!=null){
                _courses.value =
                    timetable.courses
            }
        }
    }


    /**
     * 修改当前周
     */
    fun changeWeek(
        week:Int
    ){
        _currentWeek.value=week
    }

    /**
     * 设置开学日期并跳转到当前周
     */
    fun setSemesterStart(
        dateMillis:Long
    ){
        prefs.edit()
            .putLong("semester_start", dateMillis)
            .apply()
        _semesterStart.value=dateMillis
        _currentWeek.value = weekFor(System.currentTimeMillis())
    }

    /**
     * 计算某天（时间戳）处于第几周（从开学日算起）
     */
    fun weekFor(
        dateMillis:Long
    ):Int{
        val start=_semesterStart.value
        if(start<=0L) return 1
        val dayMillis=24*60*60*1000L
        val days=(dateMillis-start)/dayMillis
        return ((days/7)+1)
            .toInt()
            .coerceIn(1, MAX_WEEK)
    }

    /**
     * 当前周课程
     */
    fun getWeekCourses()
            :List<Course>{
        return _courses.value.filter {
            it.weeks.contains(
                _currentWeek.value
            )
        }
    }

    /**
     * 同步完成后调用
     */
    fun refresh(){
        loadTimetable()
    }

    /**
     * 清除全部课表数据
     */
    fun clearTimetable(){
        storage.clear()
        _courses.value = emptyList()
    }

    companion object {
        const val MAX_WEEK = 20
    }
}