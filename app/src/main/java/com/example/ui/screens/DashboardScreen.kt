package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.model.AttendanceStatus
import com.example.ui.theme.*
import com.example.ui.viewmodel.HRViewModel
import com.example.ui.viewmodel.NavScreen

@Composable
fun DashboardScreen(
    viewModel: HRViewModel,
    modifier: Modifier = Modifier
) {
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val todayAttendances by viewModel.todayAttendances.collectAsStateWithLifecycle()
    val departments by viewModel.departments.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    var showPunchModal by remember { mutableStateOf(false) }
    var isPunchInAction by remember { mutableStateOf(true) }

    // Calculate real-time statistics
    val totalEmployees = employees.size
    val presentCount = todayAttendances.count { it.status == AttendanceStatus.PRESENT }
    val lateCount = todayAttendances.count { it.status == AttendanceStatus.LATE || it.isLate }
    val absentCount = todayAttendances.count { it.status == AttendanceStatus.ABSENT }
    val onLeaveCount = todayAttendances.count { it.status == AttendanceStatus.ON_LEAVE }
    val checkedOutCount = todayAttendances.count { it.status == AttendanceStatus.CHECKED_OUT }
    val earlyExitCount = todayAttendances.count { it.isEarlyExit }
    val notCheckedInCount = todayAttendances.count { it.status == AttendanceStatus.NOT_CHECKED_IN }
    val currentlyWorking = (presentCount + lateCount) - checkedOutCount

    val totalActiveToday = presentCount + lateCount + checkedOutCount
    val attendanceRate = if (totalEmployees > 0) (totalActiveToday.toDouble() / totalEmployees * 100) else 0.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Bengali Tagline & Welcome Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = HRPrimary),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "Welcome, ${currentUser?.fullName ?: "Admin"}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Role: ${currentUser?.role?.name ?: "ADMIN"} • ${currentUser?.department ?: "Corporate"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "LIVE MONITOR",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "“এক ক্লিকেই আপনার প্রতিষ্ঠানের উপস্থিতি, অনুপস্থিতি, লেট এন্ট্রি ও ছুটির সম্পূর্ণ আপডেট।”",
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Quick Attendance Punch Action Row
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            isPunchInAction = true
                            showPunchModal = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusPresentGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("dashboard_quick_check_in")
                    ) {
                        Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clock In", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            isPunchInAction = false
                            showPunchModal = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusCheckedOutPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("dashboard_quick_check_out")
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clock Out", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        // 8 Key Statistics Cards Grid
        item {
            SectionHeader(
                title = "Today's Workforce Statistics",
                subtitle = "Real-time metrics calculated from active employee records"
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Row 1: Total & Present Today
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Total Employees",
                        value = totalEmployees.toString(),
                        icon = Icons.Default.Groups,
                        color = HRPrimary,
                        bgColor = Color(0xFFDBEAFE),
                        subtitle = "Active Organization",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setScreen(NavScreen.EMPLOYEES) }
                    )
                    StatCard(
                        title = "Present Today",
                        value = presentCount.toString(),
                        icon = Icons.Default.CheckCircle,
                        color = StatusPresentGreen,
                        bgColor = StatusPresentBg,
                        subtitle = "${String.format("%.1f", attendanceRate)}% rate",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setScreen(NavScreen.LIVE_STATUS) }
                    )
                }

                // Row 2: Late Employees & Currently Working
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Late Employees",
                        value = lateCount.toString(),
                        icon = Icons.Default.AccessTime,
                        color = StatusLateAmber,
                        bgColor = StatusLateBg,
                        subtitle = "Grace period: 15m",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setScreen(NavScreen.LIVE_STATUS) }
                    )
                    StatCard(
                        title = "Currently Working",
                        value = currentlyWorking.coerceAtLeast(0).toString(),
                        icon = Icons.Default.WorkHistory,
                        color = HRSecondary,
                        bgColor = Color(0xFFE0F2FE),
                        subtitle = "Active in Office",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 3: Absent Today & On Leave
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Absent Today",
                        value = absentCount.toString(),
                        icon = Icons.Default.Cancel,
                        color = StatusAbsentRed,
                        bgColor = StatusAbsentBg,
                        subtitle = "Unexcused absence",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setScreen(NavScreen.LIVE_STATUS) }
                    )
                    StatCard(
                        title = "On Leave",
                        value = onLeaveCount.toString(),
                        icon = Icons.Default.FlightTakeoff,
                        color = StatusLeaveBlue,
                        bgColor = StatusLeaveBg,
                        subtitle = "Approved leaves",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setScreen(NavScreen.LEAVES) }
                    )
                }

                // Row 4: Early Exit & Not Checked In
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Early Exit",
                        value = earlyExitCount.toString(),
                        icon = Icons.Default.DirectionsWalk,
                        color = Color(0xFFD97706),
                        bgColor = Color(0xFFFEF3C7),
                        subtitle = "Before shift close",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Not Checked In",
                        value = notCheckedInCount.toString(),
                        icon = Icons.Default.HourglassEmpty,
                        color = StatusNotCheckedInSlate,
                        bgColor = StatusNotCheckedInBg,
                        subtitle = "Awaiting arrival",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Attendance Percentage Bar Overview
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
                            text = "Today's Attendance Overview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${String.format("%.1f", attendanceRate)}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = StatusPresentGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { (attendanceRate / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = StatusPresentGreen,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LegendItem("Present", "$presentCount", StatusPresentGreen)
                        LegendItem("Late", "$lateCount", StatusLateAmber)
                        LegendItem("On Leave", "$onLeaveCount", StatusLeaveBlue)
                        LegendItem("Absent", "$absentCount", StatusAbsentRed)
                        LegendItem("Not In", "$notCheckedInCount", StatusNotCheckedInSlate)
                    }
                }
            }
        }

        // Department-wise Attendance Breakdown
        item {
            SectionHeader(
                title = "Department-wise Attendance",
                subtitle = "Present vs Total per department",
                actionText = "Manage Depts",
                onAction = { viewModel.setScreen(NavScreen.DEPARTMENTS) }
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(departments) { dept ->
                    val deptAttendances = todayAttendances.filter { it.department == dept.name }
                    val deptPresent = deptAttendances.count { it.status == AttendanceStatus.PRESENT || it.status == AttendanceStatus.LATE || it.status == AttendanceStatus.CHECKED_OUT }
                    val deptTotal = dept.employeeCount.coerceAtLeast(deptAttendances.size).coerceAtLeast(1)
                    val percent = (deptPresent.toDouble() / deptTotal * 100).toInt()

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier.width(160.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = dept.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Head: ${dept.headName.split(" ").firstOrNull() ?: ""}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = "$deptPresent / $deptTotal",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HRPrimary
                                )
                                Text(
                                    text = "$percent%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (percent >= 80) StatusPresentGreen else StatusLateAmber
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = { (percent / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (percent >= 80) StatusPresentGreen else StatusLateAmber,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Live Employee Check-in / Check-out Timeline
        item {
            SectionHeader(
                title = "Recent Attendance Activity",
                subtitle = "Latest punches and live events",
                actionText = "View All",
                onAction = { viewModel.setScreen(NavScreen.LIVE_STATUS) }
            )
        }

        val recentActivity = todayAttendances
            .filter { it.checkInTime != null }
            .take(6)

        if (recentActivity.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No check-in activity recorded yet for today.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(recentActivity) { record ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatarBadge(
                            name = record.employeeName,
                            colorHex = 0xFF2563EB,
                            size = 38
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = record.employeeName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${record.department} • ${record.employeeId}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "In: ${record.checkInTime ?: "--"}${if (record.checkOutTime != null) " | Out: ${record.checkOutTime}" else ""} (${record.captureMethod})",
                                fontSize = 10.sp,
                                color = HRPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        StatusBadge(status = record.status)
                    }
                }
            }
        }
    }

    if (showPunchModal) {
        PunchDialog(
            isCheckIn = isPunchInAction,
            onDismiss = { showPunchModal = false },
            onPunchConfirmed = { method, location ->
                viewModel.punchTodayAttendance(isPunchInAction, method, location)
                showPunchModal = false
            }
        )
    }
}

@Composable
private fun LegendItem(label: String, count: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$label ($count)",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
