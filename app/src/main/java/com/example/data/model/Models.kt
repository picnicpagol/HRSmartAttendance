package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    ADMIN, HR, MANAGER, EMPLOYEE
}

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val email: String,
    val passwordHash: String,
    val fullName: String,
    val role: UserRole,
    val employeeId: String = "",
    val department: String = "",
    val designation: String = "",
    val avatarColor: Long = 0xFF2563EB
)

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey val id: String, // e.g. EMP-1001
    val name: String,
    val email: String,
    val phone: String,
    val department: String,
    val designation: String,
    val joiningDate: String,
    val shiftId: Long,
    val shiftName: String,
    val salary: Double,
    val emergencyContact: String,
    val status: String = "Active", // Active, On Leave, Inactive
    val avatarColor: Long = 0xFF3B82F6
)

@Entity(tableName = "departments")
data class Department(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val code: String,
    val headName: String,
    val employeeCount: Int = 0,
    val description: String = ""
)

@Entity(tableName = "shifts")
data class Shift(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // Morning, General, Night
    val startTime: String, // e.g. "09:00"
    val endTime: String, // e.g. "18:00"
    val gracePeriodMinutes: Int = 15,
    val breakMinutes: Int = 60,
    val overtimeRule: String = "1.5x after 8 hours"
)

enum class AttendanceStatus {
    PRESENT, LATE, ABSENT, ON_LEAVE, NOT_CHECKED_IN, CHECKED_OUT, HALF_DAY
}

@Entity(tableName = "attendances")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: String,
    val employeeName: String,
    val department: String,
    val designation: String = "",
    val date: String, // yyyy-MM-dd
    val checkInTime: String? = null, // e.g. "08:55 AM"
    val checkOutTime: String? = null, // e.g. "06:10 PM"
    val workingHours: Double = 0.0,
    val status: AttendanceStatus = AttendanceStatus.NOT_CHECKED_IN,
    val isLate: Boolean = false,
    val isEarlyExit: Boolean = false,
    val overtimeHours: Double = 0.0,
    val captureMethod: String = "MANUAL", // MANUAL, QR, GPS, BIOMETRIC, WEB
    val location: String = "Headquarters",
    val modifiedBy: String? = null,
    val modificationReason: String? = null
)

enum class LeaveType {
    CASUAL, SICK, ANNUAL, EMERGENCY, UNPAID
}

enum class LeaveStatus {
    PENDING, APPROVED, REJECTED
}

@Entity(tableName = "leave_requests")
data class LeaveRequest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: String,
    val employeeName: String,
    val department: String,
    val leaveType: LeaveType,
    val startDate: String,
    val endDate: String,
    val daysCount: Int = 1,
    val reason: String,
    val documentName: String = "",
    val status: LeaveStatus = LeaveStatus.PENDING,
    val appliedOn: String,
    val reviewedBy: String? = null,
    val reviewedOn: String? = null
)

@Entity(tableName = "holidays")
data class Holiday(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val date: String, // yyyy-MM-dd
    val type: String = "National",
    val description: String = ""
)

@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // CHECKIN, LATE, LEAVE, ANNOUNCEMENT, SYSTEM
    val timestamp: String,
    val isRead: Boolean = false
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val user: String,
    val action: String,
    val date: String,
    val time: String,
    val ipDevice: String = "Mobile Android App",
    val details: String
)

@Entity(tableName = "company_settings")
data class CompanySettings(
    @PrimaryKey val id: Long = 1,
    val companyName: String = "HR Smart Attendance Ltd.",
    val tagline: String = "এক ক্লিকেই আপনার প্রতিষ্ঠানের উপস্থিতি, অনুপস্থিতি, লেট এন্ট্রি ও ছুটির সম্পূর্ণ আপডেট।",
    val address: String = "Gulshan-2, Dhaka 1212",
    val workingHours: String = "09:00 AM - 06:00 PM",
    val gracePeriodMinutes: Int = 15,
    val overtimeMultiplier: Double = 1.5,
    val timezone: String = "Asia/Dhaka (GMT+6)",
    val dateFormat: String = "yyyy-MM-dd",
    val allowGpsPunch: Boolean = true,
    val allowQrPunch: Boolean = true,
    val allowBiometricPunch: Boolean = true
)
