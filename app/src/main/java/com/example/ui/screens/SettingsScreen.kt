package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CompanySettings
import com.example.ui.theme.HRPrimary
import com.example.ui.viewmodel.HRViewModel

@Composable
fun SettingsScreen(
    viewModel: HRViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.companySettings.collectAsStateWithLifecycle()

    var companyName by remember(settings) { mutableStateOf(settings.companyName) }
    var tagline by remember(settings) { mutableStateOf(settings.tagline) }
    var address by remember(settings) { mutableStateOf(settings.address) }
    var workingHours by remember(settings) { mutableStateOf(settings.workingHours) }
    var gracePeriod by remember(settings) { mutableStateOf(settings.gracePeriodMinutes.toString()) }
    var otMultiplier by remember(settings) { mutableStateOf(settings.overtimeMultiplier.toString()) }
    var timezone by remember(settings) { mutableStateOf(settings.timezone) }

    var allowGps by remember(settings) { mutableStateOf(settings.allowGpsPunch) }
    var allowQr by remember(settings) { mutableStateOf(settings.allowQrPunch) }
    var allowBio by remember(settings) { mutableStateOf(settings.allowBiometricPunch) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        SectionHeader(
            title = "System Settings & Policies",
            subtitle = "Configure organization profiles, attendance rules, and terminal methods"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Company Information Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Enterprise Company Profile", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = HRPrimary)

                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Company Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("settings_company_name")
                )

                OutlinedTextField(
                    value = tagline,
                    onValueChange = { tagline = it },
                    label = { Text("Motto / Bengali Tagline") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Headquarters Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = timezone,
                    onValueChange = { timezone = it },
                    label = { Text("Timezone & Date Standard") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Attendance Rules Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Attendance & Payroll Calculation Rules", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = HRPrimary)

                OutlinedTextField(
                    value = workingHours,
                    onValueChange = { workingHours = it },
                    label = { Text("Standard Working Hours (e.g. 09:00 AM - 06:00 PM)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = gracePeriod,
                        onValueChange = { gracePeriod = it },
                        label = { Text("Grace Period (Mins)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = otMultiplier,
                        onValueChange = { otMultiplier = it },
                        label = { Text("Overtime Multiplier") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Capture Methods & Terminal Toggles
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Attendance Capture Methods", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = HRPrimary)

                Spacer(modifier = Modifier.height(8.dp))

                SwitchRow("GPS Geofence Office Radius Verification", allowGps) { allowGps = it }
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 6.dp))
                SwitchRow("Terminal Dynamic QR Code Verification", allowQr) { allowQr = it }
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 6.dp))
                SwitchRow("Hardware Biometric Sensor Integration", allowBio) { allowBio = it }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val updated = settings.copy(
                    companyName = companyName.trim(),
                    tagline = tagline.trim(),
                    address = address.trim(),
                    workingHours = workingHours.trim(),
                    gracePeriodMinutes = gracePeriod.toIntOrNull() ?: 15,
                    overtimeMultiplier = otMultiplier.toDoubleOrNull() ?: 1.5,
                    timezone = timezone.trim(),
                    allowGpsPunch = allowGps,
                    allowQrPunch = allowQr,
                    allowBiometricPunch = allowBio
                )
                viewModel.updateSettings(updated)
            },
            colors = ButtonDefaults.buttonColors(containerColor = HRPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("save_settings_button")
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save System Settings", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
