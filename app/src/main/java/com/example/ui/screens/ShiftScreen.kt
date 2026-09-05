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
import com.example.data.model.Shift
import com.example.ui.theme.HRPrimary
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.viewmodel.HRViewModel

@Composable
fun ShiftScreen(
    viewModel: HRViewModel,
    modifier: Modifier = Modifier
) {
    val shifts by viewModel.shifts.collectAsStateWithLifecycle()
    val employees by viewModel.employees.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingShift by remember { mutableStateOf<Shift?>(null) }
    var deletingShift by remember { mutableStateOf<Shift?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = HRPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_shift_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Shift")
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
                title = "Organization Work Shifts",
                subtitle = "Automatic late calculation, grace period, and overtime rules"
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 72.dp)
            ) {
                items(shifts) { shift ->
                    val assignedCount = employees.count { it.shiftName == shift.name }
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = HRPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = shift.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "$assignedCount Employees",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Working Hours", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${shift.startTime} - ${shift.endTime}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Column {
                                    Text("Grace Period", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${shift.gracePeriodMinutes} mins", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Column {
                                    Text("Break Duration", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${shift.breakMinutes} mins", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
                                Text(
                                    text = "Overtime: ${shift.overtimeRule}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )

                                Row {
                                    IconButton(onClick = { editingShift = shift }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(onClick = { deletingShift = shift }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = StatusAbsentRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog || editingShift != null) {
        val isEdit = editingShift != null
        val target = editingShift

        var name by remember { mutableStateOf(target?.name ?: "") }
        var startTime by remember { mutableStateOf(target?.startTime ?: "09:00 AM") }
        var endTime by remember { mutableStateOf(target?.endTime ?: "06:00 PM") }
        var graceStr by remember { mutableStateOf(target?.gracePeriodMinutes?.toString() ?: "15") }
        var breakStr by remember { mutableStateOf(target?.breakMinutes?.toString() ?: "60") }
        var overtimeRule by remember { mutableStateOf(target?.overtimeRule ?: "1.5x after 8 hrs") }

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                editingShift = null
            },
            title = { Text(if (isEdit) "Edit Shift" else "Add New Shift") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Shift Name (e.g. Evening Shift)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time (e.g. 09:00 AM)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time (e.g. 06:00 PM)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = graceStr,
                        onValueChange = { graceStr = it },
                        label = { Text("Late Grace Period (Minutes)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = breakStr,
                        onValueChange = { breakStr = it },
                        label = { Text("Break Duration (Minutes)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = overtimeRule,
                        onValueChange = { overtimeRule = it },
                        label = { Text("Overtime Policy") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val s = Shift(
                                id = target?.id ?: 0,
                                name = name.trim(),
                                startTime = startTime.trim(),
                                endTime = endTime.trim(),
                                gracePeriodMinutes = graceStr.toIntOrNull() ?: 15,
                                breakMinutes = breakStr.toIntOrNull() ?: 60,
                                overtimeRule = overtimeRule
                            )
                            if (isEdit) {
                                viewModel.addShift(s)
                            } else {
                                viewModel.addShift(s)
                            }
                            showAddDialog = false
                            editingShift = null
                        }
                    }
                ) {
                    Text(if (isEdit) "Save Shift" else "Create Shift")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showAddDialog = false
                    editingShift = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (deletingShift != null) {
        val s = deletingShift!!
        AlertDialog(
            onDismissRequest = { deletingShift = null },
            title = { Text("Delete Shift") },
            text = { Text("Are you sure you want to delete '${s.name}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteShift(s)
                        deletingShift = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusAbsentRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deletingShift = null }) { Text("Cancel") }
            }
        )
    }
}
