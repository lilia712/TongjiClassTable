package com.example.courseschedule

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import com.example.courseschedule.data.Course
import com.example.courseschedule.data.TimetableStorage
import com.example.courseschedule.sync.SyncActivity
import com.example.courseschedule.viewmodel.TimetableViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = TimetableViewModel(TimetableStorage(this), this)

        setContent {
            MaterialTheme {
                val context = LocalContext.current
                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        viewModel.refresh()
                    }
                }
                TimetableScreen(
                    viewModel = viewModel,
                    onSyncClick = {
                        launcher.launch(Intent(context, SyncActivity::class.java))
                    }
                )
            }
        }
    }
}

@Composable
private fun TimetableScreen(
    viewModel: TimetableViewModel,
    onSyncClick: () -> Unit
) {
    val courses by viewModel.courses.collectAsState()
    val currentWeek by viewModel.currentWeek.collectAsState()
    val semesterStart by viewModel.semesterStart.collectAsState()
    val courseTimes by viewModel.courseTimes.collectAsState()
    val fixedDuration by viewModel.fixedDuration.collectAsState()
    val weekNow = remember(semesterStart) {
        viewModel.weekFor(System.currentTimeMillis())
    }
    var showSettings by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SyncHeader(
            onSyncClick = onSyncClick,
            onSettingsClick = { showSettings = true }
        )
        WeekSelector(
            currentWeek = currentWeek,
            onWeekChange = viewModel::changeWeek
        )
        HorizontalDivider()
        if (courses.isEmpty()) {
            EmptyTimetable(onSyncClick)
        } else {
            CourseGrid(courses = courses, currentWeek = currentWeek, semesterStart = semesterStart, courseTimes = courseTimes)
        }
    }

    if (showSettings) {
        SettingsDialog(
            currentWeekNow = weekNow,
            semesterStart = semesterStart,
            courseTimes = courseTimes,
            fixedDuration = fixedDuration,
            onReset = {
                viewModel.changeWeek(weekNow)
                showSettings = false
            },
            onSetSemesterStart = { viewModel.setSemesterStart(it) },
            onSetCourseTime = viewModel::setCourseTime,
            onSetFixedDuration = viewModel::setFixedDuration,
            onClearTimetable = viewModel::clearTimetable,
            onDismiss = { showSettings = false }
        )
    }
}

@Composable
private fun SyncHeader(
    onSyncClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "课程表",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "同济大学",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onSyncClick) {
            Icon(Icons.Filled.Sync, contentDescription = "同步课表")
        }
        IconButton(onClick = onSettingsClick) {
            Icon(Icons.Filled.Settings, contentDescription = "设置")
        }
    }
}

@Composable
private fun WeekSelector(
    currentWeek: Int,
    onWeekChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = { onWeekChange((currentWeek - 1).coerceAtLeast(1)) }) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "上一周")
        }
        Text(
            "第 $currentWeek 周",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = { onWeekChange((currentWeek + 1).coerceAtMost(MAX_WEEK)) }) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "下一周")
        }
    }
}

@Composable
private fun SettingsDialog(
    currentWeekNow: Int,
    semesterStart: Long,
    courseTimes: Map<Int, String>,
    fixedDuration: Int?,
    onReset: () -> Unit,
    onSetSemesterStart: (Long) -> Unit,
    onSetCourseTime: (Int, String, String, String, String) -> Unit,
    onSetFixedDuration: (Int?) -> Unit,
    onClearTimetable: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showClearConfirm by remember { mutableStateOf(false) }
    var showCourseTime by remember { mutableStateOf(false) }
    var showFixedDuration by remember { mutableStateOf(false) }
    var showTutorial by remember { mutableStateOf(false) }
    val startMillis = remember(semesterStart) {
        if (semesterStart > 0) semesterStart else System.currentTimeMillis()
    }
    val startCal = remember(startMillis) {
        java.util.Calendar.getInstance().apply {
            timeInMillis = startMillis
        }
    }

    fun showDatePicker() {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = java.util.Calendar.getInstance().apply {
                    clear()
                    set(year, month, dayOfMonth, 0, 0, 0)
                }
                onSetSemesterStart(cal.timeInMillis)
            },
            startCal.get(java.util.Calendar.YEAR),
            startCal.get(java.util.Calendar.MONTH),
            startCal.get(java.util.Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "关闭")
            }
        },
        title = {
            Text(
                text = "设置",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "开学日期",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { showDatePicker() }) {
                        Text(
                            if (semesterStart > 0) formatDate(semesterStart)
                            else "未设置"
                        )
                    }
                }
                HorizontalDivider()
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "回到当前周",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Row {
                        TextButton(
                            onClick = onReset,
                            enabled = semesterStart > 0
                        ) {
                            Text("第 $currentWeekNow 周")
                        }
                    }
                }
                HorizontalDivider()
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "课程时间段",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { showCourseTime = true }) {
                        Text(
                            if (courseTimes.isEmpty()) "点击设置"
                            else "${courseTimes.size} 节已设置"
                        )
                    }
                }
                HorizontalDivider()
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "固定上课时长",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { showFixedDuration = true }) {
                        Text(
                            if (fixedDuration != null) "${fixedDuration} 分钟"
                            else "未启用"
                        )
                    }
                }
                HorizontalDivider()
                TextButton(
                    onClick = { showTutorial = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "新手教程",
                        color = Color(0xFF056608)
                    )
                }
                HorizontalDivider()
                TextButton(
                    onClick = { showClearConfirm = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "清除全部课表",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            confirmButton = {
                TextButton(onClick = {
                    onClearTimetable()
                    showClearConfirm = false
                    onDismiss()
                }) {
                    Text("确认清除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("取消")
                }
            },
            title = {
                Text(
                    text = "是否清除全部课表？",
                    fontWeight = FontWeight.Bold
                )
            }
        )
    }

    if (showTutorial) {
        TutorialDialog(onDismiss = { showTutorial = false })
    }

    if (showCourseTime) {
        var editingSection by remember { mutableStateOf(-1) }
        AlertDialog(
            onDismissRequest = {
                showCourseTime = false
                editingSection = -1
            },
            confirmButton = {
                TextButton(onClick = {
                    showCourseTime = false
                    editingSection = -1
                }) {
                    Text("关闭")
                }
            },
            title = {
                Text(text = "课程时间段", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (editingSection == -1) {
                        (1..MAX_SECTION).forEach { section ->
                            val time = courseTimes[section]
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { editingSection = section }
                            ) {
                                Text(
                                    "第 $section 节",
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    time ?: "点击设置",
                                    color = if (time != null) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Text(
                            "设置第 $editingSection 节",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        if (fixedDuration != null) {
                            Text("固定时长 $fixedDuration 分钟，输入开始时间自动计算结束时间", fontSize = 13.sp)
                        } else {
                            Text("点击选择开始时间 / 结束时间", fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val currentTime = courseTimes[editingSection]
                        val parts = currentTime?.split("-")
                        val startParts = parts?.getOrNull(0)?.split(":")
                        val endParts = parts?.getOrNull(1)?.split(":")
                        val initStartH = startParts?.getOrNull(0)?.toIntOrNull() ?: 0
                        val initStartM = startParts?.getOrNull(1)?.toIntOrNull() ?: 0
                        val initEndH = endParts?.getOrNull(0)?.toIntOrNull() ?: 0
                        val initEndM = endParts?.getOrNull(1)?.toIntOrNull() ?: 0
                        val fixed = fixedDuration

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = {
                                android.app.TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        if (fixed != null) {
                                            val startTotal = h * 60 + m
                                            val endTotal = startTotal + fixed
                                            val endH = (endTotal / 60) % 24
                                            val endM = endTotal % 60
                                            onSetCourseTime(
                                                editingSection,
                                                String.format("%02d", h),
                                                String.format("%02d", m),
                                                String.format("%02d", endH),
                                                String.format("%02d", endM)
                                            )
                                        } else {
                                            onSetCourseTime(
                                                editingSection,
                                                String.format("%02d", h),
                                                String.format("%02d", m),
                                                String.format("%02d", initEndH),
                                                String.format("%02d", initEndM)
                                            )
                                        }
                                    },
                                    initStartH,
                                    initStartM,
                                    true
                                ).show()
                            }) {
                                Text(
                                    "开始：${String.format("%02d:%02d", initStartH, initStartM)}",
                                    fontSize = 13.sp
                                )
                            }
                            if (fixed == null) {
                                TextButton(onClick = {
                                    android.app.TimePickerDialog(
                                        context,
                                        { _, h, m ->
                                            val startH = initStartH
                                            val startM = initStartM
                                            onSetCourseTime(
                                                editingSection,
                                                String.format("%02d", startH),
                                                String.format("%02d", startM),
                                                String.format("%02d", h),
                                                String.format("%02d", m)
                                            )
                                        },
                                        initEndH,
                                        initEndM,
                                        true
                                    ).show()
                                }) {
                                    Text(
                                        "结束：${String.format("%02d:%02d", initEndH, initEndM)}",
                                        fontSize = 13.sp
                                    )
                                }
                            } else {
                                val endTotal = (initStartH * 60 + initStartM) + fixed
                                val selfEndH = (endTotal / 60) % 24
                                val selfEndM = endTotal % 60
                                Text(
                                    "→ 结束：${String.format("%02d:%02d", selfEndH, selfEndM)}（自动）",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { editingSection = -1 },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("← 返回列表")
                        }
                    }
                }
            }
        )
    }

    if (showFixedDuration) {
        var input by remember { mutableStateOf(fixedDuration?.toString() ?: "") }
        AlertDialog(
            onDismissRequest = { showFixedDuration = false },
            confirmButton = {
                TextButton(onClick = {
                    val minutes = input.toIntOrNull()
                    onSetFixedDuration(if (minutes != null && minutes > 0) minutes else null)
                    showFixedDuration = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFixedDuration = false }) {
                    Text("取消")
                }
            },
            title = {
                Text(text = "固定上课时长", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "启用后，设置每节课时间只需输入开始时间，" +
                            "结束时间按固定时长自动计算。留空或填 0 表示不启用。",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it.filter { c -> c.isDigit() } },
                        label = { Text("时长（分钟）") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        )
    }
}

private fun formatDate(millis: Long): String {
    val cal = java.util.Calendar.getInstance().apply { timeInMillis = millis }
    val y = cal.get(java.util.Calendar.YEAR)
    val m = cal.get(java.util.Calendar.MONTH) + 1
    val d = cal.get(java.util.Calendar.DAY_OF_MONTH)
    return "${y}年${m}月${d}日"
}

@Composable
private fun EmptyTimetable(onSyncClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.EventBusy,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "暂无课表数据",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "请登录同济一站式服务\n进入课表页面后完成同步",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onSyncClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFC8B8E0),
                    contentColor = Color.Black
            )
        ) {
            //Icon(Icons.Filled.Sync, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("点击同步课表")
        }
    }
}

private val DAYS = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
private const val MAX_SECTION = 12
private const val MAX_WEEK = 20

@Composable
private fun CourseGrid(
    courses: List<Course>,
    currentWeek: Int,
    semesterStart: Long,
    courseTimes: Map<Int, String> = emptyMap(),
    modifier: Modifier = Modifier
) {
    var selectedCourse by remember { mutableStateOf<Course?>(null) }

    val weekCourses = remember(courses, currentWeek) {
        courses.filter { it.weeks.contains(currentWeek) }
    }

    // 同一门课（按课程名）固定一种颜色；随机打乱色板后按出现顺序分配，避免相邻课程同色
    val courseColors = remember(courses) {
        val shuffled = COURSE_COLORS.shuffled()
        val map = LinkedHashMap<String, Color>()
        courses.forEach { course ->
            if (!map.containsKey(course.name)) {
                map[course.name] = shuffled[map.size % shuffled.size]
            }
        }
        map
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val timeColumnWidth = 64.dp
        val dayWidth = (maxWidth - timeColumnWidth) / 7f
        val headerHeight = 20.dp
        val rowHeight = 60.dp

        selectedCourse?.let { course ->
            CourseDetailDialog(
                course = course,
                selectedWeek = currentWeek,
                onDismiss = { selectedCourse = null }
            )
        }

        Column {
            Row(
                Modifier
                    .height(headerHeight)
                    .fillMaxWidth()
            ) {
                Spacer(Modifier.width(timeColumnWidth))
                DAYS.forEach { day ->
                    Box(
                        Modifier
                            .width(dayWidth)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            day,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(
                Modifier
                    .height(20.dp)
                    .fillMaxWidth()
            ) {
                Spacer(Modifier.width(timeColumnWidth))
                for (dayIndex in 0 until DAYS.size) {
                    val cal = remember(semesterStart, currentWeek, dayIndex) {
                        java.util.Calendar.getInstance().apply {
                            timeInMillis = semesterStart
                            add(java.util.Calendar.DAY_OF_MONTH, (currentWeek - 1) * 7 + dayIndex)
                        }
                    }
                    Box(
                        Modifier
                            .width(dayWidth)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${cal.get(java.util.Calendar.MONTH) + 1}/${cal.get(java.util.Calendar.DAY_OF_MONTH)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Box {
                Column {
                    for (section in 1..MAX_SECTION) {
                        Row(
                            Modifier
                                .height(rowHeight)
                                .fillMaxWidth()
                        ) {
                            Box(
                                Modifier
                                    .width(timeColumnWidth)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                val timeRange = courseTimes[section]
                                if (timeRange != null) {
                                    val parts = timeRange.split("-")
                                    val start = parts.getOrNull(0) ?: ""
                                    val end = parts.getOrNull(1) ?: ""
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            "${section}",
                                            fontSize = 15.sp,
                                            lineHeight = 11.sp,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            start,
                                            fontSize = 10.sp,
                                            lineHeight = 10.sp,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            end,
                                            fontSize = 10.sp,
                                            lineHeight = 10.sp,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    Text(
                                        "${section}",
                                        fontSize = 15.sp,
                                        lineHeight = 12.sp,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            repeat(DAYS.size) { index ->
                                Box(
                                    Modifier
                                        .width(dayWidth)
                                        .fillMaxHeight()
                                        .border(
                                            width = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant
                                        )
                                        .background(
                                            if (index >= 5)
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                            else
                                                Color.Transparent
                                        )
                                )
                            }
                        }
                    }
                }

                weekCourses.forEach { course ->
                    CourseBlock(
                        course = course,
                        color = courseColors[course.name] ?: COURSE_COLORS.first(),
                        onClick = { selectedCourse = course },
                        modifier = Modifier
                            .offset(
                                x = timeColumnWidth + dayWidth * (course.dayOfWeek - 1),
                                y = rowHeight * (course.startSection - 1)
                            )
                            .width(dayWidth)
                            .height(rowHeight * (course.endSection - course.startSection + 1))
                    )
                }
            }
        }
    }
}

private val COURSE_COLORS = listOf(
    Color(0xFFFFB7C5), // 樱花粉
    Color(0xFFB5CC88), // 抹茶绿
    Color(0xFFFFF0A8), // 柠檬黄
    Color(0xFFA8D8EA), // 天空蓝
    Color(0xFFD4B8E0), // 香芋紫
    Color(0xFFFFCBA4), // 蜜桃橙
    Color(0xFFA8E6CF), // 薄荷青
    Color(0xFFF4A7BB), // 玫瑰红
    Color(0xFFFFF8E7), // 奶油白
    Color(0xFFB8D4E3), // 海盐蓝
    Color(0xFFE0C9A6), // 焦糖棕
    Color(0xFFF2A6B8), // 覆盆子粉
    Color(0xFFC5D9A4), // 开心果绿
    Color(0xFFFFE4A8), // 芒果黄
    Color(0xFFB8C8E8), // 矢车菊蓝
    Color(0xFFC8B8E0), // 薰衣草紫
    Color(0xFFFFB8A8), // 珊瑚橘
    Color(0xFFB8D8C8), // 抹茶青
    Color(0xFFF0E0C8), // 杏仁米
    Color(0xFFC0A8D8)  // 蓝莓紫
)

@Composable
private fun CourseBlock(
    course: Course,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sectionCount = course.endSection - course.startSection + 1

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.88f))
            .clickable(onClick = onClick)
            .padding(horizontal = 3.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = course.name,
                color = Color(0xFF3D3D3D),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 12.sp
            )
            if (sectionCount > 1) {
                Text(
                    text = course.room,
                    color = Color(0xFF5C5C5C).copy(alpha = 0.9f),
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CourseDetailDialog(
    course: Course,
    selectedWeek: Int,
    onDismiss: () -> Unit
) {
    val sectionCount = course.endSection - course.startSection + 1
    val startWeek = course.weeks.minOrNull() ?: 1
    val endWeek = course.weeks.maxOrNull() ?: 1
    val allEven = course.weeks.isNotEmpty() && course.weeks.all { it % 2 == 0 }
    val allOdd = course.weeks.isNotEmpty() && course.weeks.all { it % 2 == 1 }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "关闭")
            }
        },
        title = {
            Text(
                text = course.name,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailRow(
                    label = "教师",
                    value = course.teacher.ifBlank { "—" }
                )

                DetailRow(
                    label = "地点",
                    value = course.room.ifBlank { "—" }
                )

                DetailRow(
                    label = "上课时间",
                    value = "${DAYS[course.dayOfWeek - 1]} " +
                            "第 ${course.startSection}～${course.endSection} 节"
                )

                DetailRow(
                    label = "上课周数",
                    value = if (startWeek == endWeek) "第 $startWeek 周"
                            else "第 $startWeek～$endWeek 周"
                )

                DetailRow(
                    label = "单双周",
                    value = when {
                        allEven -> "双周"
                        allOdd -> "单周"
                        else -> "每周"
                    }
                )

                DetailRow(
                    label = "当前周",
                    value = "第 $selectedWeek 周"
                )
            }
        }
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF8475C5)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

