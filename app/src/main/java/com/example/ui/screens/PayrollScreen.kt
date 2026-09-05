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

@Composable
fun PayrollScreen(
    viewModel: HRViewModel,
    modifier: Modifier = Modifier
) {
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val allAttendances by viewModel.allAttendances.collectAsStateWithLifecycle()
    val leaveRequests by viewModel.leaveRequests.collectAsStateWithLifecycle()

    var selectedCycle by remember { mutableStateOf("September 2026") }
    var searchQuery by remember { mutableStateOf("") }

    val standardWorkingDays = 26.0

    // Compute payroll item per employee
    val payrollItems = remember(employees, allAttendances, searchQuery) {
        employees.filter {
            searchQuery.isBlank() ||
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    it.id.contains(searchQuery, ignoreCase = true) ||
                    it.department.contains(searchQuery, ignoreCase = true)
        }.map { emp ->
            val empRecords = allAttendances.filter { it.employeeId == emp.id }
            val presentDays = empRecords.count { it.status == AttendanceStatus.PRESENT || it.status == AttendanceStatus.CHECKED_OUT }.toDouble()
            val lateDays = empRecords.count { it.isLate || it.status == AttendanceStatus.LATE }.toDouble()
            val absentDays = empRecords.count { it.status == AttendanceStatus.ABSENT }.toDouble()
            val totalLeaveDays = empRecords.count { it.status == AttendanceStatus.ON_LEAVE }.toDouble()
            val overtimeHours = empRecords.sumOf { it.overtimeHours }

            // 3 Late days = 1 Day salary deduction rule
            val lateDeductionDays = (lateDays / 3.0).toInt().toDouble()
            val payableDays = (presentDays + lateDays + totalLeaveDays - lateDeductionDays).coerceIn(0.0, standardWorkingDays)

            val perDaySalary = emp.salary / standardWorkingDays
            val hourlySalary = perDaySalary / 8.0
            val overtimePay = overtimeHours * hourlySalary * 1.5
            val lateDeductionAmount = lateDeductionDays * perDaySalary
            val absentDeductionAmount = absentDays * perDaySalary

            val netPayableSalary = (payableDays * perDaySalary + overtimePay).coerceAtLeast(0.0)

            EmployeePayrollData(
                employee = emp,
                presentDays = presentDays,
                lateDays = lateDays,
                absentDays = absentDays,
                leaveDays = totalLeaveDays,
                overtimeHours = overtimeHours,
                lateDeductionDays = lateDeductionDays,
                payableDays = payableDays,
                overtimePay = overtimePay,
                netSalary = netPayableSalary
            )
        }
    }

    val totalPayout = payrollItems.sumOf { it.netSalary }
    val totalOT = payrollItems.sumOf { it.overtimeHours }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Payroll cycle banner
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
                    Column {
                        Text(
                            text = "Payroll Attendance Sheet",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Cycle: $selectedCycle (26 Working Days)",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "AUTO CALCULATED",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color.White.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Est. Gross Payout", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                        Text("৳ ${String.format("%,.0f", totalPayout)}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Total Overtime", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                        Text("${String.format("%.1f", totalOT)} Hours", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Late Policy", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                        Text("3 Lates = 1 Day", color = StatusLateAmber, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter employee payroll...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("payroll_search_input"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Employees for Payout: ${payrollItems.size}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Employee payroll cards list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(payrollItems) { item ->
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
                            Column {
                                Text(item.employee.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${item.employee.designation} • ${item.employee.department} • ${item.employee.id}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("৳ ${String.format("%,.0f", item.netSalary)}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = StatusPresentGreen)
                                Text("Payable: ${item.payableDays.toInt()} / 26 Days", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Detailed row of attendance calculations
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            PayrollMini("Present", "${item.presentDays.toInt()}d", StatusPresentGreen)
                            PayrollMini("Late", "${item.lateDays.toInt()}d", StatusLateAmber)
                            PayrollMini("Absent", "${item.absentDays.toInt()}d", StatusAbsentRed)
                            PayrollMini("Leave", "${item.leaveDays.toInt()}d", StatusLeaveBlue)
                            PayrollMini("OT Hours", "${item.overtimeHours.toInt()}h", HRSecondary)
                            PayrollMini("Late Deduct", "-${item.lateDeductionDays.toInt()}d", StatusAbsentRed)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Base: ৳ ${String.format("%,.0f", item.employee.salary)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (item.overtimePay > 0) {
                                Text("+ OT Pay: ৳ ${String.format("%,.0f", item.overtimePay)}", fontSize = 11.sp, color = StatusPresentGreen, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PayrollMini(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

private data class EmployeePayrollData(
    val employee: com.example.data.model.Employee,
    val presentDays: Double,
    val lateDays: Double,
    val absentDays: Double,
    val leaveDays: Double,
    val overtimeHours: Double,
    val lateDeductionDays: Double,
    val payableDays: Double,
    val overtimePay: Double,
    val netSalary: Double
)
