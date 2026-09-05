package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Attendance
import com.example.data.model.AttendanceStatus
import com.example.ui.theme.*
import com.example.ui.viewmodel.HRViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(
    viewModel: HRViewModel,
    modifier: Modifier = Modifier
) {
    val allAttendances by viewModel.allAttendances.collectAsStateWithLifecycle()
    val holidays by viewModel.holidays.collectAsStateWithLifecycle()

    var calendarMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDayRecords by remember { mutableStateOf<Pair<String, List<Attendance>>?>(null) }

    val currentMonthYear = remember(calendarMonth) {
        SimpleDateFormat("MMMM yyyy", Locale.US).format(calendarMonth.time)
    }

    // Days in current month
    val daysInMonth = remember(calendarMonth) {
        val cal = calendarMonth.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed Sun
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        Pair(firstDayOfWeek, maxDays)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Month Selector
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val cal = calendarMonth.clone() as Calendar
                    cal.add(Calendar.MONTH, -1)
                    calendarMonth = cal
                }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                }

                Text(
                    text = currentMonthYear,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = {
                    val cal = calendarMonth.clone() as Calendar
                    cal.add(Calendar.MONTH, 1)
                    calendarMonth = cal
                }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Legend row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CalendarLegend("Present", StatusPresentGreen)
            CalendarLegend("Late", StatusLateAmber)
            CalendarLegend("Absent", StatusAbsentRed)
            CalendarLegend("Holiday", StatusLeaveBlue)
            CalendarLegend("Weekend", Color(0xFF64748B))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Calendar Grid Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Days of week header
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                text = day,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (day == "Fri" || day == "Sat") HRPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                val (firstDayOffset, totalDays) = daysInMonth
                val totalCells = ((firstDayOffset + totalDays + 6) / 7) * 7

                for (week in 0 until (totalCells / 7)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in 0..6) {
                            val cellIndex = week * 7 + col
                            val dayNum = cellIndex - firstDayOffset + 1

                            if (dayNum in 1..totalDays) {
                                val cal = calendarMonth.clone() as Calendar
                                cal.set(Calendar.DAY_OF_MONTH, dayNum)
                                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
                                val isWeekend = (col == 5 || col == 6) // Friday/Saturday weekend standard in BD/corporate

                                val dayAttendances = allAttendances.filter { it.date == dateStr }
                                val isHoliday = holidays.any { it.date == dateStr }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isHoliday) StatusLeaveBg
                                            else if (isWeekend) Color(0xFFF1F5F9)
                                            else Color.Transparent
                                        )
                                        .clickable {
                                            selectedDayRecords = Pair(dateStr, dayAttendances)
                                        }
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = dayNum.toString(),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isHoliday) StatusLeaveBlue else if (isWeekend) Color(0xFF64748B) else MaterialTheme.colorScheme.onSurface
                                        )

                                        if (dayAttendances.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                if (dayAttendances.any { it.status == AttendanceStatus.PRESENT }) {
                                                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(StatusPresentGreen))
                                                }
                                                if (dayAttendances.any { it.status == AttendanceStatus.LATE }) {
                                                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(StatusLateAmber))
                                                }
                                                if (dayAttendances.any { it.status == AttendanceStatus.ABSENT }) {
                                                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(StatusAbsentRed))
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick tip
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.TouchApp, contentDescription = null, tint = HRPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tap on any calendar date to view the full employee attendance breakdown.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Day detail dialog
    if (selectedDayRecords != null) {
        val (dateStr, records) = selectedDayRecords!!
        val presentCount = records.count { it.status == AttendanceStatus.PRESENT }
        val lateCount = records.count { it.status == AttendanceStatus.LATE }
        val absentCount = records.count { it.status == AttendanceStatus.ABSENT }
        val onLeaveCount = records.count { it.status == AttendanceStatus.ON_LEAVE }

        AlertDialog(
            onDismissRequest = { selectedDayRecords = null },
            title = {
                Column {
                    Text("Attendance for $dateStr", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Summary: $presentCount Present, $lateCount Late, $absentCount Absent, $onLeaveCount Leave", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            text = {
                if (records.isEmpty()) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("No logs found for this date (e.g. Weekend or Holiday).")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(260.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(records) { r ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(r.employeeName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("In: ${r.checkInTime ?: "--"} | Out: ${r.checkOutTime ?: "--"}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                StatusBadge(status = r.status)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedDayRecords = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun CalendarLegend(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(3.dp))
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
