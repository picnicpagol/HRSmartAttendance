package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Employee
import com.example.ui.theme.*
import com.example.ui.viewmodel.HRViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeScreen(
    viewModel: HRViewModel,
    modifier: Modifier = Modifier
) {
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val departments by viewModel.departments.collectAsStateWithLifecycle()
    val shifts by viewModel.shifts.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedDeptFilter by remember { mutableStateOf<String?>(null) }
    var sortByNameAsc by remember { mutableStateOf(true) }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingEmployee by remember { mutableStateOf<Employee?>(null) }
    var viewingEmployee by remember { mutableStateOf<Employee?>(null) }
    var deletingEmployee by remember { mutableStateOf<Employee?>(null) }

    val filteredEmployees = remember(employees, searchQuery, selectedDeptFilter, sortByNameAsc) {
        employees.filter { emp ->
            val matchesQuery = searchQuery.isBlank() ||
                    emp.name.contains(searchQuery, ignoreCase = true) ||
                    emp.id.contains(searchQuery, ignoreCase = true) ||
                    emp.designation.contains(searchQuery, ignoreCase = true) ||
                    emp.email.contains(searchQuery, ignoreCase = true)
            val matchesDept = selectedDeptFilter == null || emp.department == selectedDeptFilter
            matchesQuery && matchesDept
        }.sortedWith(
            if (sortByNameAsc) compareBy { it.name } else compareByDescending { it.name }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = HRPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_employee_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Employee")
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
            // Search and sort row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search employees...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("employee_search_input"),
                    singleLine = true
                )

                IconButton(
                    onClick = { sortByNameAsc = !sortByNameAsc },
                    modifier = Modifier.testTag("sort_employee_button")
                ) {
                    Icon(
                        imageVector = if (sortByNameAsc) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = "Sort"
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Department filter chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedDeptFilter == null,
                        onClick = { selectedDeptFilter = null },
                        label = { Text("All (${employees.size})") },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                items(departments) { dept ->
                    val count = employees.count { it.department == dept.name }
                    FilterChip(
                        selected = selectedDeptFilter == dept.name,
                        onClick = { selectedDeptFilter = if (selectedDeptFilter == dept.name) null else dept.name },
                        label = { Text("${dept.name} ($count)") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("filter_dept_${dept.code.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Total Employees: ${filteredEmployees.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Employee list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 72.dp)
            ) {
                items(filteredEmployees) { emp ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewingEmployee = emp }
                            .testTag("employee_card_${emp.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                UserAvatarBadge(
                                    name = emp.name,
                                    colorHex = emp.avatarColor,
                                    size = 46
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
                                        text = "ID: ${emp.id} • ${emp.email}",
                                        fontSize = 11.sp,
                                        color = HRPrimary
                                    )
                                }

                                Surface(
                                    color = if (emp.status == "Active") StatusPresentBg else StatusLateBg,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = emp.status,
                                        color = if (emp.status == "Active") StatusPresentGreen else StatusLateAmber,
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
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Phone: ${emp.phone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Shift: ${emp.shiftName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Row {
                                    IconButton(
                                        onClick = { viewingEmployee = emp },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = "View Profile", modifier = Modifier.size(18.dp), tint = HRPrimary)
                                    }
                                    IconButton(
                                        onClick = { editingEmployee = emp },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp), tint = HRSecondary)
                                    }
                                    IconButton(
                                        onClick = { deletingEmployee = emp },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp), tint = StatusAbsentRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add or Edit Employee Dialog
    if (showAddDialog || editingEmployee != null) {
        val isEdit = editingEmployee != null
        val emp = editingEmployee

        var name by remember { mutableStateOf(emp?.name ?: "") }
        var email by remember { mutableStateOf(emp?.email ?: "") }
        var phone by remember { mutableStateOf(emp?.phone ?: "+880 1700000000") }
        var department by remember { mutableStateOf(emp?.department ?: (departments.firstOrNull()?.name ?: "Engineering")) }
        var designation by remember { mutableStateOf(emp?.designation ?: "Software Engineer") }
        var joiningDate by remember { mutableStateOf(emp?.joiningDate ?: "2024-01-15") }
        var salaryStr by remember { mutableStateOf(emp?.salary?.toInt()?.toString() ?: "55000") }
        var emergencyContact by remember { mutableStateOf(emp?.emergencyContact ?: "+880 1800000000") }
        var shiftName by remember { mutableStateOf(emp?.shiftName ?: "General Shift") }
        var status by remember { mutableStateOf(emp?.status ?: "Active") }

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                editingEmployee = null
            },
            title = {
                Text(if (isEdit) "Edit Employee Profile" else "Add New Employee")
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("employee_form_name")
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("employee_form_email")
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = designation,
                        onValueChange = { designation = it },
                        label = { Text("Designation *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Department") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = salaryStr,
                        onValueChange = { salaryStr = it },
                        label = { Text("Base Salary (BDT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = joiningDate,
                        onValueChange = { joiningDate = it },
                        label = { Text("Joining Date (yyyy-MM-dd)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = emergencyContact,
                        onValueChange = { emergencyContact = it },
                        label = { Text("Emergency Contact") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && email.isNotBlank()) {
                            val sal = salaryStr.toDoubleOrNull() ?: 50000.0
                            val newId = emp?.id ?: "EMP-${1000 + employees.size + 1}"
                            val newEmp = Employee(
                                id = newId,
                                name = name.trim(),
                                email = email.trim(),
                                phone = phone.trim(),
                                department = department,
                                designation = designation.trim(),
                                joiningDate = joiningDate,
                                shiftId = 1,
                                shiftName = shiftName,
                                salary = sal,
                                emergencyContact = emergencyContact,
                                status = status,
                                avatarColor = emp?.avatarColor ?: 0xFF2563EB
                            )
                            if (isEdit) {
                                viewModel.updateEmployee(newEmp)
                            } else {
                                viewModel.addEmployee(newEmp)
                            }
                            showAddDialog = false
                            editingEmployee = null
                        }
                    },
                    modifier = Modifier.testTag("employee_form_submit")
                ) {
                    Text(if (isEdit) "Save Changes" else "Add Employee")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showAddDialog = false
                        editingEmployee = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // View Profile Modal
    if (viewingEmployee != null) {
        val emp = viewingEmployee!!
        AlertDialog(
            onDismissRequest = { viewingEmployee = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UserAvatarBadge(name = emp.name, colorHex = emp.avatarColor, size = 44)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(emp.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${emp.designation} • ${emp.id}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileItem("Department", emp.department)
                    ProfileItem("Email Address", emp.email)
                    ProfileItem("Phone", emp.phone)
                    ProfileItem("Joining Date", emp.joiningDate)
                    ProfileItem("Assigned Shift", emp.shiftName)
                    ProfileItem("Monthly Salary", "৳ ${String.format("%,.0f", emp.salary)}")
                    ProfileItem("Emergency Contact", emp.emergencyContact)
                    ProfileItem("Current Status", emp.status)
                }
            },
            confirmButton = {
                Button(onClick = { viewingEmployee = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Delete confirmation dialog
    if (deletingEmployee != null) {
        val emp = deletingEmployee!!
        AlertDialog(
            onDismissRequest = { deletingEmployee = null },
            title = { Text("Confirm Deletion") },
            text = {
                Text("Are you sure you want to delete employee record for ${emp.name} (${emp.id})? This action will be recorded in audit logs.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteEmployee(emp)
                        deletingEmployee = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusAbsentRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deletingEmployee = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ProfileItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}
