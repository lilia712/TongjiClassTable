package com.example.courseschedule

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TutorialDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("我知道了")
            }
        },
        title = {
            Text(text = "新手教程", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TutorialSection(
                    title = "1. 同步课表",
                    items = listOf(
                        "点击右上角同步图标",
                        "登录同济一站式服务",
                        "进入课表页面后，点击底部「点击同步课表」按钮",
                        "同步成功后自动返回主界面"
                    )
                )
                TutorialSection(
                    title = "2. 设置开学日期",
                    items = listOf(
                        "点击左上角齿轮进入设置",
                        "点击「开学日期」，选择本学期开学第一天",
                        "设置后自动计算当前是第几周"
                    )
                )
                TutorialSection(
                    title = "3. 查看课表",
                    items = listOf(
                        "左右箭头切换不同周次",
                        "顶部显示当前周对应的日期",
                        "不同课程自动分配不同颜色"
                    )
                )
                TutorialSection(
                    title = "4. 查看课程详情",
                    items = listOf(
                        "点击课表中的任意课程块",
                        "可查看教师、地点、上课时间、周数等信息"
                    )
                )
                TutorialSection(
                    title = "5. 设置课程时间段",
                    items = listOf(
                        "齿轮 → 课程时间段",
                        "逐节设置每节课的起止时间",
                        "设置后左侧时间列会显示具体时间"
                    )
                )
                TutorialSection(
                    title = "6. 固定上课时长",
                    items = listOf(
                        "齿轮 → 固定上课时长",
                        "设置每节课的固定时长（如45分钟）",
                        "启用后设置每节课只需输入开始时间",
                        "结束时间自动计算"
                    )
                )
                TutorialSection(
                    title = "7. 其他功能",
                    items = listOf(
                        "回到当前周：一键跳转到当前实际周次",
                        "清除全部课表：清除所有已同步的课程数据"
                    )
                )
            }
        }
    )
}

@Composable
private fun TutorialSection(
    title: String,
    items: List<String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.primary
        )
        items.forEach { item ->
            Text(
                text = "• $item",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}