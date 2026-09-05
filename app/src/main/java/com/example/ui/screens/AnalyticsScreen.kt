package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AttendanceStatus
import com.example.ui.theme.*
import com.example.ui.viewmodel.HRViewModel

@Composable
fun AnalyticsScreen(
    viewModel: HRViewModel,
    modifier: Modifier = Modifier
) {
    val allAttendances by viewModel.allAttendances.collectAsStateWithLifecycle()
    val departments by viewModel.departments.collectAsStateWithLifecycle()
    val employees by viewModel.employees.collectAsStateWithLifecycle()

    // 7-day attendance trend data
    val recentDates = remember(allAttendances) {
        allAttendances.map { it.date }.distinct().sortedDescending().take(7).reversed()
    }

    val trendData = remember(recentDates, allAttendances) {
        recentDates.map { d ->
            val records = allAttendances.filter { it.date == d }
            val present = records.count { it.status == AttendanceStatus.PRESENT || it.status == AttendanceStatus.LATE || it.status == AttendanceStatus.CHECKED_OUT }
            val late = records.count { it.status == AttendanceStatus.LATE }
            val absent = records.count { it.status == AttendanceStatus.ABSENT }
            Triple(d.takeLast(5), present, absent)
        }
    }

    // Status breakdown across all records
    val totalRecords = allAttendances.size.coerceAtLeast(1)
    val presentCount = allAttendances.count { it.status == AttendanceStatus.PRESENT }
    val lateCount = allAttendances.count { it.status == AttendanceStatus.LATE }
    val absentCount = allAttendances.count { it.status == AttendanceStatus.ABSENT }
    val leaveCount = allAttendances.count { it.status == AttendanceStatus.ON_LEAVE }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            SectionHeader(
                title = "Workforce Attendance Analytics",
                subtitle = "Trends, department compliance & status distribution"
            )
        }

        // 7-Day Trend Canvas Chart
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "7-Day Attendance Trend",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ChartLegend("Present", StatusPresentGreen)
                            ChartLegend("Absent", StatusAbsentRed)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bar Chart drawn with Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val barCount = if (trendData.isNotEmpty()) trendData.size else 1
                            val slotWidth = w / barCount
                            val barWidth = slotWidth * 0.35f
                            val maxCount = 35f // scaling max

                            // Draw baseline
                            drawLine(
                                color = Color(0xFFE2E8F0),
                                start = Offset(0f, h - 20f),
                                end = Offset(w, h - 20f),
                                strokeWidth = 2f
                            )

                            trendData.forEachIndexed { i, (_, pres, abs) ->
                                val xCenter = i * slotWidth + slotWidth / 2f

                                // Present bar (Green)
                                val presHeight = ((pres / maxCount) * (h - 30f)).coerceAtMost(h - 30f)
                                drawRoundRect(
                                    color = StatusPresentGreen,
                                    topLeft = Offset(xCenter - barWidth, h - 20f - presHeight),
                                    size = Size(barWidth * 0.9f, presHeight),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                                )

                                // Absent bar (Red)
                                val absHeight = ((abs / maxCount) * (h - 30f)).coerceAtMost(h - 30f)
                                drawRoundRect(
                                    color = StatusAbsentRed,
                                    topLeft = Offset(xCenter + 2f, h - 20f - absHeight),
                                    size = Size(barWidth * 0.9f, absHeight),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                                )
                            }
                        }
                    }

                    // Date labels row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        trendData.forEach { (dateLabel, _, _) ->
                            Text(
                                text = dateLabel,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Status Distribution Breakdown
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Overall Status Distribution",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Historical distribution across ${allAttendances.size} logged sessions",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    DistributionBar("Present", presentCount, totalRecords, StatusPresentGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    DistributionBar("Late Entries", lateCount, totalRecords, StatusLateAmber)
                    Spacer(modifier = Modifier.height(8.dp))
                    DistributionBar("Absences", absentCount, totalRecords, StatusAbsentRed)
                    Spacer(modifier = Modifier.height(8.dp))
                    DistributionBar("Approved Leaves", leaveCount, totalRecords, StatusLeaveBlue)
                }
            }
        }

        // Department Performance Ranking
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Department Attendance Compliance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    departments.forEach { dept ->
                        val deptRecords = allAttendances.filter { it.department == dept.name }
                        val deptPresent = deptRecords.count { it.status == AttendanceStatus.PRESENT || it.status == AttendanceStatus.LATE || it.status == AttendanceStatus.CHECKED_OUT }
                        val deptTotal = deptRecords.size.coerceAtLeast(1)
                        val rate = (deptPresent.toDouble() / deptTotal * 100).toInt()

                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(dept.name, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text("$rate%", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (rate >= 80) StatusPresentGreen else StatusLateAmber)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (rate / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = if (rate >= 80) StatusPresentGreen else StatusLateAmber,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DistributionBar(label: String, count: Int, total: Int, color: Color) {
    val percent = (count.toDouble() / total * 100).toInt()
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("$count ($percent%)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (percent / 100f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun ChartLegend(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
