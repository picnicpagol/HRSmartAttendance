package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.model.Attendance
import com.example.data.model.AttendanceStatus
import com.example.ui.theme.*
import com.example.ui.viewmodel.HRViewModel

@Composable
fun LiveStatusScreen(
    viewModel: HRViewModel,
    modifier: Modifier = Modifier
) {
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val todayAttendances by viewModel.todayAttendances.collectAsStateWithLifecycle()
    val departments by viewModel.departments.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<AttendanceStatus?>(null) }
    var selectedDeptFilter by remember { mutableStateOf<String?>(null) }

    var editingAttendance by remember { mutableStateOf<Attendance?>(null) }

    // Combine employees with today's attendance record
    val attendanceMap = remember(todayAttendances) {
        todayAttendances.associateBy { it.employeeId }
    }

    val liveRecords = remember(employees, attendanceMap, searchQuery, selectedStatusFilter, selectedDeptFilter) {
        employees.map { emp ->
            val att = attendanceMap[emp.id]
            val effectiveStatus = att?.status ?: AttendanceStatus.NOT_CHECKED_IN
            Triple(emp, att, effectiveStatus)
        }.filter { (emp, _, status) ->
            val matchesSearch = searchQuery.isBlank() ||
                    emp.name.contains(searchQuery, ignoreCase = true) ||
                    emp.id.contains(searchQuery, ignoreCase = true) ||
                    emp.designation.contains(searchQuery, ignoreCase = true)

            val matchesStatus = selectedStatusFilter == null || status == selectedStatusFilter
            val matchesDept = selectedDeptFilter == null || emp.department == selectedDeptFilter

            matchesSearch && matchesStatus && matchesDept
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name, ID or designation...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("live_status_search_bar"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Status filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedStatusFilter == null,
                    onClick = { selectedStatusFilter = null },
                    label = { Text("All (${employees.size})") },
                    shape = RoundedCornerShape(8.dp)
                )
            }
            items(AttendanceStatus.values()) { status ->
                val count = todayAttendances.count { it.status == status }
                FilterChip(
                    selected = selectedStatusFilter == status,
                    onClick = { selectedStatusFilter = if (selectedStatusFilter == status) null else status },
                    label = { Text("${status.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }} ($count)") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("filter_chip_${status.name.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Result count header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Showing ${liveRecords.size} employees",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Live Sync: Active",
                style = MaterialTheme.typography.bodySmall,
                color = StatusPresentGreen,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Live employee list
        if (liveRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.PersonSearch,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No employees match current filters",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(liveRecords) { (emp, att, status) ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                UserAvatarBadge(
                                    name = emp.name,
                                    colorHex = emp.avatarColor,
                                    size = 44
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = emp.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${emp.designation} • ${emp.department}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "ID: ${emp.id} • Shift: ${emp.shiftName}",
                                        fontSize = 11.sp,
                                        color = HRPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                StatusBadge(status = status)
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Grid of attendance metrics
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MetricColumn("Check-In", att?.checkInTime ?: "--:--", Icons.Default.Login)
                                MetricColumn("Check-Out", att?.checkOutTime ?: "--:--", Icons.Default.Logout)
                                MetricColumn("Hours", if (att != null && att.workingHours > 0) "${att.workingHours}h" else "--", Icons.Default.Timer)
                                MetricColumn(
                                    "Late Status",
                                    if (att?.isLate == true) "Late" else if (att?.checkInTime != null) "On Time" else "--",
                                    Icons.Default.AccessTime,
                                    color = if (att?.isLate == true) StatusLateAmber else StatusPresentGreen
                                )
                            }

                            // Capture details & Manual Edit button for HR/Admin
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Capture: ${att?.captureMethod ?: "None"} • ${att?.location ?: "HQ"}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                TextButton(
                                    onClick = {
                                        editingAttendance = att ?: Attendance(
                                            employeeId = emp.id,
                                            employeeName = emp.name,
                                            department = emp.department,
                                            designation = emp.designation,
                                            date = viewModel.selectedDate.value,
                                            status = AttendanceStatus.PRESENT
                                        )
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.testTag("edit_attendance_${emp.id}")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Modify", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingAttendance != null) {
        val record = editingAttendance!!
        var inTime by remember { mutableStateOf(record.checkInTime ?: "09:00 AM") }
        var outTime by remember { mutableStateOf(record.checkOutTime ?: "06:00 PM") }
        var selectedStatus by remember { mutableStateOf(record.status) }
        var reason by remember { mutableStateOf("") }
        var reasonError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { editingAttendance = null },
            title = {
                Text("Modify Attendance - ${record.employeeName}")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Employee ID: ${record.employeeId} • Date: ${record.date}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = inTime,
                        onValueChange = { inTime = it },
                        label = { Text("Check-In Time") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = outTime,
                        onValueChange = { outTime = it },
                        label = { Text("Check-Out Time") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Attendance Status:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(AttendanceStatus.PRESENT, AttendanceStatus.LATE, AttendanceStatus.ABSENT, AttendanceStatus.ON_LEAVE).forEach { st ->
                            val isSel = selectedStatus == st
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedStatus = st }
                            ) {
                                Box(modifier = Modifier.padding(6.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = st.name.replace("_", " "),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it; reasonError = false },
                        label = { Text("Reason for Modification *") },
                        isError = reasonError,
                        supportingText = { if (reasonError) Text("Audit reason is required") },
                        placeholder = { Text("e.g. Device malfunction, gate pass verified") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reason.isBlank()) {
                            reasonError = true
                        } else {
                            val updated = record.copy(
                                checkInTime = inTime,
                                checkOutTime = outTime,
                                status = selectedStatus,
                                isLate = (selectedStatus == AttendanceStatus.LATE)
                            )
                            viewModel.modifyAttendanceRecord(updated, reason)
                            editingAttendance = null
                        }
                    }
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { editingAttendance = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MetricColumn(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color? = null
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color ?: MaterialTheme.colorScheme.onSurface
        )
    }
}
