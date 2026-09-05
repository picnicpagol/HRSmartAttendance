package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.model.LeaveRequest
import com.example.data.model.LeaveStatus
import com.example.data.model.LeaveType
import com.example.ui.theme.*
import com.example.ui.viewmodel.HRViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LeaveScreen(
    viewModel: HRViewModel,
    modifier: Modifier = Modifier
) {
    val leaveRequests by viewModel.leaveRequests.collectAsStateWithLifecycle()
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Pending", "Approved", "Rejected", "All")

    var showApplyModal by remember { mutableStateOf(false) }

    val filteredLeaves = remember(leaveRequests, selectedTab) {
        when (selectedTab) {
            0 -> leaveRequests.filter { it.status == LeaveStatus.PENDING }
            1 -> leaveRequests.filter { it.status == LeaveStatus.APPROVED }
            2 -> leaveRequests.filter { it.status == LeaveStatus.REJECTED }
            else -> leaveRequests
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showApplyModal = true },
                containerColor = HRPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Apply Leave", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("apply_leave_button")
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Leave Quota Overview Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuotaCard("Casual", "10/12", HRPrimary, StatusLeaveBg, Modifier.weight(1f))
                QuotaCard("Sick", "12/14", StatusLateAmber, StatusLateBg, Modifier.weight(1f))
                QuotaCard("Annual", "15/15", StatusPresentGreen, StatusPresentBg, Modifier.weight(1f))
                QuotaCard("Emergency", "4/5", Color(0xFF9333EA), Color(0xFFFAF5FF), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab bar
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = HRPrimary,
                divider = {}
            ) {
                tabTitles.forEachIndexed { index, title ->
                    val count = when (index) {
                        0 -> leaveRequests.count { it.status == LeaveStatus.PENDING }
                        1 -> leaveRequests.count { it.status == LeaveStatus.APPROVED }
                        2 -> leaveRequests.count { it.status == LeaveStatus.REJECTED }
                        else -> leaveRequests.size
                    }
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = "$title ($count)",
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredLeaves.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No ${tabTitles[selectedTab].lowercase()} leave applications found.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 72.dp)
                ) {
                    items(filteredLeaves) { request ->
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
                                            text = request.employeeName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "${request.department} • ${request.employeeId}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Surface(
                                        color = when (request.status) {
                                            LeaveStatus.APPROVED -> StatusPresentBg
                                            LeaveStatus.REJECTED -> StatusAbsentBg
                                            LeaveStatus.PENDING -> StatusLateBg
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = request.status.name,
                                            color = when (request.status) {
                                                LeaveStatus.APPROVED -> StatusPresentGreen
                                                LeaveStatus.REJECTED -> StatusAbsentRed
                                                LeaveStatus.PENDING -> StatusLateAmber
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Type: ${request.leaveType.name.replace("_", " ")}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = HRPrimary
                                    )
                                    Text(
                                        text = "${request.daysCount} Day(s)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Period: ${request.startDate} to ${request.endDate}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Reason: ${request.reason}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (request.documentName.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(12.dp), tint = HRSecondary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = request.documentName,
                                            fontSize = 11.sp,
                                            color = HRSecondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                if (request.reviewedBy != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Reviewed by: ${request.reviewedBy} on ${request.reviewedOn}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Actions for HR / Admin when status is Pending
                                if (request.status == LeaveStatus.PENDING) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = { viewModel.reviewLeave(request, approved = false) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusAbsentRed),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.testTag("reject_leave_${request.id}")
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Reject", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Button(
                                            onClick = { viewModel.reviewLeave(request, approved = true) },
                                            colors = ButtonDefaults.buttonColors(containerColor = StatusPresentGreen),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.testTag("approve_leave_${request.id}")
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Approve", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Apply Leave Dialog
    if (showApplyModal) {
        var selectedType by remember { mutableStateOf(LeaveType.CASUAL) }
        var startDate by remember { mutableStateOf("2026-09-10") }
        var endDate by remember { mutableStateOf("2026-09-11") }
        var daysCountStr by remember { mutableStateOf("2") }
        var reason by remember { mutableStateOf("") }
        var docAttached by remember { mutableStateOf(false) }

        val activeEmp = employees.find { it.id == (currentUser?.employeeId ?: "") } ?: employees.firstOrNull()

        AlertDialog(
            onDismissRequest = { showApplyModal = false },
            title = { Text("Submit Leave Application") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Employee: ${activeEmp?.name ?: "Current User"} (${activeEmp?.id ?: "EMP-1004"})", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    Text("Leave Type:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        LeaveType.values().forEach { t ->
                            val isSel = selectedType == t
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) HRPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) {
                                TextButton(
                                    onClick = { selectedType = t },
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text(
                                        t.name.lowercase().replaceFirstChar { it.uppercase() },
                                        fontSize = 10.sp,
                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start Date (yyyy-MM-dd)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("End Date (yyyy-MM-dd)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = daysCountStr,
                        onValueChange = { daysCountStr = it },
                        label = { Text("Total Days") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason for Leave *") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = docAttached,
                            onCheckedChange = { docAttached = it }
                        )
                        Text("Attach Supporting Document (e.g. Medical prescription)", fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reason.isNotBlank() && activeEmp != null) {
                            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                            viewModel.applyLeave(
                                LeaveRequest(
                                    employeeId = activeEmp.id,
                                    employeeName = activeEmp.name,
                                    department = activeEmp.department,
                                    leaveType = selectedType,
                                    startDate = startDate,
                                    endDate = endDate,
                                    daysCount = daysCountStr.toIntOrNull() ?: 1,
                                    reason = reason.trim(),
                                    documentName = if (docAttached) "supporting_document.pdf" else "",
                                    appliedOn = today
                                )
                            )
                            showApplyModal = false
                        }
                    }
                ) {
                    Text("Submit Application")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showApplyModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun QuotaCard(title: String, balance: String, color: Color, bgColor: Color, modifier: Modifier) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontSize = 11.sp, color = color, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(balance, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
