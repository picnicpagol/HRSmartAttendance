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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AttendanceScreen(
    viewModel: HRViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val todayAttendances by viewModel.todayAttendances.collectAsStateWithLifecycle()
    val departments by viewModel.departments.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedDeptFilter by remember { mutableStateOf<String?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<AttendanceStatus?>(null) }

    var editingRecord by remember { mutableStateOf<Attendance?>(null) }

    val filteredList = remember(todayAttendances, searchQuery, selectedDeptFilter, selectedStatusFilter) {
        todayAttendances.filter { record ->
            val matchesQuery = searchQuery.isBlank() ||
                    record.employeeName.contains(searchQuery, ignoreCase = true) ||
                    record.employeeId.contains(searchQuery, ignoreCase = true)
            val matchesDept = selectedDeptFilter == null || record.department == selectedDeptFilter
            val matchesStatus = selectedStatusFilter == null || record.status == selectedStatusFilter
            matchesQuery && matchesDept && matchesStatus
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Date switcher bar
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
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
                IconButton(
                    onClick = {
                        val cal = Calendar.getInstance()
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        cal.time = sdf.parse(selectedDate) ?: Date()
                        cal.add(Calendar.DAY_OF_YEAR, -1)
                        viewModel.selectedDate.value = sdf.format(cal.time)
                    }
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Attendance Date: $selectedDate",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Total Records: ${todayAttendances.size}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = {
                        val cal = Calendar.getInstance()
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        cal.time = sdf.parse(selectedDate) ?: Date()
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                        viewModel.selectedDate.value = sdf.format(cal.time)
                    }
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search and filters
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter attendance records...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("attendance_search_bar"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Status filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedStatusFilter == null,
                    onClick = { selectedStatusFilter = null },
                    label = { Text("All (${todayAttendances.size})") },
                    shape = RoundedCornerShape(8.dp)
                )
            }
            items(AttendanceStatus.values()) { st ->
                val c = todayAttendances.count { it.status == st }
                FilterChip(
                    selected = selectedStatusFilter == st,
                    onClick = { selectedStatusFilter = if (selectedStatusFilter == st) null else st },
                    label = { Text("${st.name.replace("_", " ")} ($c)") },
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Attendance records list
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No attendance records found for this date and filter.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredList) { record ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = record.employeeName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${record.department} • ID: ${record.employeeId}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                StatusBadge(status = record.status)
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "In: ${record.checkInTime ?: "--"}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (record.isLate) StatusLateAmber else StatusPresentGreen
                                )
                                Text(
                                    text = "Out: ${record.checkOutTime ?: "--"}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (record.isEarlyExit) Color(0xFFD97706) else HRPrimary
                                )
                                Text(
                                    text = "Total: ${record.workingHours}h",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "OT: ${record.overtimeHours}h",
                                    fontSize = 12.sp,
                                    color = if (record.overtimeHours > 0) StatusPresentGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            if (record.modifiedBy != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Modified by ${record.modifiedBy}: ${record.modificationReason ?: "Manual correction"}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Method: ${record.captureMethod} • ${record.location}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                TextButton(
                                    onClick = { editingRecord = record },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                ) {
                                    Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Manual Edit", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingRecord != null) {
        val record = editingRecord!!
        var inTime by remember { mutableStateOf(record.checkInTime ?: "09:00 AM") }
        var outTime by remember { mutableStateOf(record.checkOutTime ?: "06:00 PM") }
        var selectedStatus by remember { mutableStateOf(record.status) }
        var reason by remember { mutableStateOf("") }
        var reasonError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { editingRecord = null },
            title = { Text("Manual Attendance Override") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${record.employeeName} (${record.employeeId}) • ${record.date}", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    OutlinedTextField(
                        value = inTime,
                        onValueChange = { inTime = it },
                        label = { Text("Clock In Time") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = outTime,
                        onValueChange = { outTime = it },
                        label = { Text("Clock Out Time") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Status:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(AttendanceStatus.PRESENT, AttendanceStatus.LATE, AttendanceStatus.HALF_DAY, AttendanceStatus.ABSENT).forEach { st ->
                            val isSel = selectedStatus == st
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f).clickable { selectedStatus = st }
                            ) {
                                Box(modifier = Modifier.padding(6.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = st.name.replace("_", " "),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
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
                        supportingText = { if (reasonError) Text("Reason is required for audit logs") },
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
                            editingRecord = null
                        }
                    }
                ) {
                    Text("Apply Override")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { editingRecord = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
