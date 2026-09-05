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
import com.example.data.model.Department
import com.example.ui.theme.HRPrimary
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.viewmodel.HRViewModel

@Composable
fun DepartmentScreen(
    viewModel: HRViewModel,
    modifier: Modifier = Modifier
) {
    val departments by viewModel.departments.collectAsStateWithLifecycle()
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val todayAttendances by viewModel.todayAttendances.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingDept by remember { mutableStateOf<Department?>(null) }
    var deletingDept by remember { mutableStateOf<Department?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = HRPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_department_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Department")
            }
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
            SectionHeader(
                title = "Departments & Units",
                subtitle = "Manage organizational hierarchy and department attendance"
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 72.dp)
            ) {
                items(departments) { dept ->
                    val deptEmployees = employees.filter { it.department == dept.name }
                    val deptAttendances = todayAttendances.filter { it.department == dept.name }
                    val presentCount = deptAttendances.count { it.status == AttendanceStatus.PRESENT || it.status == AttendanceStatus.LATE || it.status == AttendanceStatus.CHECKED_OUT }
                    val total = deptEmployees.size.coerceAtLeast(dept.employeeCount).coerceAtLeast(1)
                    val attendancePercent = (presentCount.toDouble() / total * 100).toInt()

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = dept.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = dept.code,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Head: ${dept.headName}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row {
                                    IconButton(onClick = { editingDept = dept }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(onClick = { deletingDept = dept }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = StatusAbsentRed)
                                    }
                                }
                            }

                            if (dept.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dept.description,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Active Staff: ${deptEmployees.size} employees",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Today's Attendance: $presentCount / $total ($attendancePercent%)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (attendancePercent >= 80) StatusPresentGreen else HRPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog || editingDept != null) {
        val isEdit = editingDept != null
        val target = editingDept

        var name by remember { mutableStateOf(target?.name ?: "") }
        var code by remember { mutableStateOf(target?.code ?: "") }
        var headName by remember { mutableStateOf(target?.headName ?: "") }
        var description by remember { mutableStateOf(target?.description ?: "") }

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                editingDept = null
            },
            title = { Text(if (isEdit) "Edit Department" else "Add New Department") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Department Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Code (e.g. ENG, FIN, HR)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = headName,
                        onValueChange = { headName = it },
                        label = { Text("Department Head *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val d = Department(
                                id = target?.id ?: 0,
                                name = name.trim(),
                                code = if (code.isNotBlank()) code.trim().uppercase() else name.take(3).uppercase(),
                                headName = headName.trim(),
                                description = description.trim(),
                                employeeCount = target?.employeeCount ?: 0
                            )
                            if (isEdit) {
                                viewModel.addDepartment(d)
                            } else {
                                viewModel.addDepartment(d)
                            }
                            showAddDialog = false
                            editingDept = null
                        }
                    }
                ) {
                    Text(if (isEdit) "Save Department" else "Create Department")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showAddDialog = false
                    editingDept = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (deletingDept != null) {
        val d = deletingDept!!
        AlertDialog(
            onDismissRequest = { deletingDept = null },
            title = { Text("Delete Department") },
            text = { Text("Are you sure you want to delete '${d.name}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDepartment(d)
                        deletingDept = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusAbsentRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deletingDept = null }) { Text("Cancel") }
            }
        )
    }
}
