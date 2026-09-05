package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AttendanceStatus
import com.example.ui.theme.*
import com.example.ui.viewmodel.HRViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EmployeePortalScreen(
    viewModel: HRViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val allAttendances by viewModel.allAttendances.collectAsStateWithLifecycle()
    val leaveRequests by viewModel.leaveRequests.collectAsStateWithLifecycle()

    var showPunchModal by remember { mutableStateOf(false) }
    var isCheckInAction by remember { mutableStateOf(true) }

    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val activeEmp = remember(employees, currentUser) {
        employees.find { it.id == (currentUser?.employeeId ?: "") } ?: employees.firstOrNull()
    }

    val empId = activeEmp?.id ?: "EMP-1004"
    val myRecords = remember(allAttendances, empId) {
        allAttendances.filter { it.employeeId == empId }
    }
    val todayRecord = remember(myRecords, todayDate) {
        myRecords.find { it.date == todayDate }
    }
    val myLeaves = remember(leaveRequests, empId) {
        leaveRequests.filter { it.employeeId == empId }
    }

    val hasCheckedIn = todayRecord?.checkInTime != null
    val hasCheckedOut = todayRecord?.checkOutTime != null

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Employee Hero Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = HRPrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            UserAvatarBadge(name = activeEmp?.name ?: "User", colorHex = activeEmp?.avatarColor ?: 0xFF3B82F6, size = 48)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(activeEmp?.name ?: "Employee Portal", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                                Text("${activeEmp?.designation} • ${activeEmp?.department}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                                Text("ID: $empId • Shift: ${activeEmp?.shiftName}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Today's Status", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                            Text(
                                text = if (hasCheckedOut) "Checked Out" else if (hasCheckedIn) "Working Now" else "Not Checked In",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (hasCheckedIn && !hasCheckedOut) StatusPresentGreen else Color.White
                            )
                        }
                        Column {
                            Text("Clock In", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                            Text(todayRecord?.checkInTime ?: "--:--", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text("Clock Out", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                            Text(todayRecord?.checkOutTime ?: "--:--", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Punch Actions Row
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Attendance Terminal Actions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Capture attendance via GPS Office Geofence, Dynamic QR, or Biometric",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                isCheckInAction = true
                                showPunchModal = true
                            },
                            enabled = !hasCheckedIn || hasCheckedOut,
                            colors = ButtonDefaults.buttonColors(containerColor = StatusPresentGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(48.dp).testTag("portal_check_in_btn")
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Punch In", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                isCheckInAction = false
                                showPunchModal = true
                            },
                            enabled = hasCheckedIn && !hasCheckedOut,
                            colors = ButtonDefaults.buttonColors(containerColor = StatusCheckedOutPurple),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(48.dp).testTag("portal_check_out_btn")
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Punch Out", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // My Leave Quota Summary
        item {
            SectionHeader(
                title = "My Leave Balances",
                subtitle = "Available paid quotas for current year",
                actionText = "Apply Leave",
                onAction = { viewModel.setScreen(com.example.ui.viewmodel.NavScreen.LEAVES) }
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(color = StatusLeaveBg, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Casual", fontSize = 11.sp, color = HRPrimary, fontWeight = FontWeight.SemiBold)
                        Text("10/12", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HRPrimary)
                    }
                }
                Surface(color = StatusLateBg, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Medical", fontSize = 11.sp, color = StatusLateAmber, fontWeight = FontWeight.SemiBold)
                        Text("12/14", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StatusLateAmber)
                    }
                }
                Surface(color = StatusPresentBg, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Annual", fontSize = 11.sp, color = StatusPresentGreen, fontWeight = FontWeight.SemiBold)
                        Text("15/15", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StatusPresentGreen)
                    }
                }
            }
        }

        // My Recent Attendance Logs
        item {
            SectionHeader(
                title = "My Recent Attendance History",
                subtitle = "Last logs and recorded work hours"
            )
        }

        if (myRecords.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("No logs found for your employee ID.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(myRecords.take(8)) { record ->
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(record.date, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("In: ${record.checkInTime ?: "--"} | Out: ${record.checkOutTime ?: "--"} (${record.workingHours}h)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Method: ${record.captureMethod} • ${record.location}", fontSize = 10.sp, color = HRPrimary)
                        }
                        StatusBadge(status = record.status)
                    }
                }
            }
        }
    }

    if (showPunchModal) {
        PunchDialog(
            isCheckIn = isCheckInAction,
            onDismiss = { showPunchModal = false },
            onPunchConfirmed = { method, location ->
                viewModel.punchTodayAttendance(isCheckInAction, method, location)
                showPunchModal = false
            }
        )
    }
}
