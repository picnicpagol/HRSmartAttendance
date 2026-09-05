package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

enum class PunchMode(val title: String, val icon: ImageVector) {
    QUICK("Quick Tap", Icons.Default.TouchApp),
    QR("QR Code", Icons.Default.QrCodeScanner),
    GPS("GPS Location", Icons.Default.MyLocation),
    BIOMETRIC("Biometric", Icons.Default.Fingerprint)
}

@Composable
fun PunchDialog(
    isCheckIn: Boolean,
    onDismiss: () -> Unit,
    onPunchConfirmed: (method: String, location: String) -> Unit
) {
    var selectedMode by remember { mutableStateOf(PunchMode.QUICK) }
    var isVerifying by remember { mutableStateOf(false) }
    var verificationSuccess by remember { mutableStateOf(false) }

    val currentTime = remember {
        SimpleDateFormat("hh:mm:ss a", Locale.US).format(Date())
    }
    val currentDate = remember {
        SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US).format(Date())
    }

    AlertDialog(
        onDismissRequest = { if (!isVerifying) onDismiss() },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isCheckIn) StatusPresentBg else StatusCheckedOutBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCheckIn) Icons.Default.Login else Icons.Default.Logout,
                        contentDescription = null,
                        tint = if (isCheckIn) StatusPresentGreen else StatusCheckedOutPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = if (isCheckIn) "Employee Check-In" else "Employee Check-Out",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Live clock badge
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentTime,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = currentDate,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Select Attendance Capture Method:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Method selector row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PunchMode.values().forEach { mode ->
                        val isSelected = selectedMode == mode
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedMode = mode }
                                .testTag("punch_mode_${mode.name.lowercase()}"),
                            border = if (isSelected) null else CardDefaults.outlinedCardBorder()
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = mode.icon,
                                    contentDescription = mode.title,
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = mode.title,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Verification details area
                when (selectedMode) {
                    PunchMode.QUICK -> {
                        Surface(
                            color = StatusPresentBg,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = StatusPresentGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Instant Web/Mobile Punch", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = StatusPresentGreen)
                                    Text("Verified device session and active network timestamp", fontSize = 11.sp, color = HRTextSecondary)
                                }
                            }
                        }
                    }
                    PunchMode.QR -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .size(140.dp)
                                .border(2.dp, HRSecondary, RoundedCornerShape(12.dp))
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode2,
                                    contentDescription = "QR Code",
                                    tint = Color.White,
                                    modifier = Modifier.size(90.dp)
                                )
                                Text(
                                    text = "Ready to Scan",
                                    color = HRSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 6.dp)
                                )
                            }
                        }
                    }
                    PunchMode.GPS -> {
                        Surface(
                            color = StatusLeaveBg,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = StatusLeaveBlue)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("HQ Geofence Verified", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = StatusLeaveBlue)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("• Coordinates: 23.7925° N, 90.4078° E", fontSize = 11.sp, color = HRTextSecondary)
                                Text("• Geofence Radius: 50m (Status: Inside 18m)", fontSize = 11.sp, color = StatusPresentGreen, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    PunchMode.BIOMETRIC -> {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(HRPrimaryLight.copy(alpha = 0.3f), Color.Transparent))),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(HRPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Biometric",
                                    tint = Color.White,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Biometric Sensor Ready",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (isVerifying) {
                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Authenticating sensor stream...", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isVerifying = true
                },
                enabled = !isVerifying,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCheckIn) StatusPresentGreen else StatusCheckedOutPurple
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_punch_button")
            ) {
                Text(
                    text = if (isCheckIn) "Confirm Check-In" else "Confirm Check-Out",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isVerifying,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel")
            }
        }
    )

    LaunchedEffect(isVerifying) {
        if (isVerifying) {
            delay(900)
            isVerifying = false
            verificationSuccess = true
            val loc = when (selectedMode) {
                PunchMode.GPS -> "Corporate HQ (GPS - 23.79°N, 90.40°E)"
                PunchMode.QR -> "Main Gate QR Terminal #02"
                PunchMode.BIOMETRIC -> "Biometric Device #B1 - 2nd Floor"
                PunchMode.QUICK -> "Headquarters (Web Portal)"
            }
            onPunchConfirmed(selectedMode.name, loc)
        }
    }
}
