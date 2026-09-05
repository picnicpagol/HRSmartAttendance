package com.example.data.repository

import com.example.data.db.AppDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class HRRepository(private val dao: AppDao) {

    // Authentication
    suspend fun getUserByEmail(email: String): User? = dao.getUserByEmail(email)
    suspend fun getUserByUsername(username: String): User? = dao.getUserByUsername(username)
    fun getAllUsers(): Flow<List<User>> = dao.getAllUsers()
    suspend fun insertUser(user: User) = dao.insertUser(user)

    // Employees
    fun getAllEmployees(): Flow<List<Employee>> = dao.getAllEmployees()
    suspend fun getEmployeeById(id: String): Employee? = dao.getEmployeeById(id)
    fun getEmployeeCount(): Flow<Int> = dao.getEmployeeCount()

    suspend fun addEmployee(employee: Employee, loggedInUser: String) {
        dao.insertEmployee(employee)
        logAudit(loggedInUser, "Employee Added", "Added employee ${employee.name} (${employee.id})")
    }

    suspend fun updateEmployee(employee: Employee, loggedInUser: String) {
        dao.updateEmployee(employee)
        logAudit(loggedInUser, "Employee Updated", "Updated profile of ${employee.name} (${employee.id})")
    }

    suspend fun deleteEmployee(employee: Employee, loggedInUser: String) {
        dao.deleteEmployee(employee)
        logAudit(loggedInUser, "Employee Deleted", "Deleted employee record ${employee.name} (${employee.id})")
    }

    // Departments
    fun getAllDepartments(): Flow<List<Department>> = dao.getAllDepartments()
    suspend fun addDepartment(department: Department, loggedInUser: String) {
        dao.insertDepartment(department)
        logAudit(loggedInUser, "Department Added", "Added department ${department.name}")
    }
    suspend fun updateDepartment(department: Department, loggedInUser: String) {
        dao.updateDepartment(department)
        logAudit(loggedInUser, "Department Updated", "Updated department ${department.name}")
    }
    suspend fun deleteDepartment(department: Department, loggedInUser: String) {
        dao.deleteDepartment(department)
        logAudit(loggedInUser, "Department Deleted", "Deleted department ${department.name}")
    }

    // Shifts
    fun getAllShifts(): Flow<List<Shift>> = dao.getAllShifts()
    suspend fun addShift(shift: Shift, loggedInUser: String) {
        dao.insertShift(shift)
        logAudit(loggedInUser, "Shift Added", "Added shift ${shift.name}")
    }
    suspend fun updateShift(shift: Shift, loggedInUser: String) {
        dao.updateShift(shift)
        logAudit(loggedInUser, "Shift Updated", "Updated shift ${shift.name}")
    }
    suspend fun deleteShift(shift: Shift, loggedInUser: String) {
        dao.deleteShift(shift)
        logAudit(loggedInUser, "Shift Deleted", "Deleted shift ${shift.name}")
    }

    // Attendance
    fun getAttendancesByDate(date: String): Flow<List<Attendance>> = dao.getAttendancesByDate(date)
    fun getAllAttendances(): Flow<List<Attendance>> = dao.getAllAttendances()
    fun getAttendancesByEmployee(employeeId: String): Flow<List<Attendance>> = dao.getAttendancesByEmployee(employeeId)

    suspend fun getTodayAttendance(employeeId: String, date: String): Attendance? {
        return dao.getAttendanceByEmployeeAndDate(employeeId, date)
    }

    suspend fun punchAttendance(
        employeeId: String,
        isCheckIn: Boolean,
        method: String,
        location: String,
        loggedInUser: String
    ): Result<Attendance> {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
        val currentTimeStr = timeFormat.format(Date())

        val emp = dao.getEmployeeById(employeeId) ?: return Result.failure(Exception("Employee not found"))
        val existing = dao.getAttendanceByEmployeeAndDate(employeeId, todayStr)

        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        if (isCheckIn) {
            val isLate = (hour > 9 || (hour == 9 && minute > 15))
            val status = if (isLate) AttendanceStatus.LATE else AttendanceStatus.PRESENT

            val record = existing?.copy(
                checkInTime = currentTimeStr,
                status = status,
                isLate = isLate,
                captureMethod = method,
                location = location
            ) ?: Attendance(
                employeeId = emp.id,
                employeeName = emp.name,
                department = emp.department,
                designation = emp.designation,
                date = todayStr,
                checkInTime = currentTimeStr,
                status = status,
                isLate = isLate,
                captureMethod = method,
                location = location
            )
            dao.insertAttendance(record)

            dao.insertNotification(
                NotificationItem(
                    title = if (isLate) "Late Check-in Recorded" else "Successful Check-in",
                    message = "${emp.name} checked in at $currentTimeStr via $method",
                    type = if (isLate) "LATE" else "CHECKIN",
                    timestamp = "Just now"
                )
            )

            logAudit(loggedInUser, "Attendance Check-in", "${emp.name} clocked in at $currentTimeStr via $method ($location)")
            return Result.success(record)
        } else {
            // Check out
            val checkInStr = existing?.checkInTime ?: "09:00 AM"
            val workingHours = calculateHoursDifference(checkInStr, currentTimeStr)
            val ot = if (workingHours > 9.0) (workingHours - 9.0) else 0.0
            val isEarly = (hour < 17)

            val record = existing?.copy(
                checkOutTime = currentTimeStr,
                workingHours = workingHours,
                status = AttendanceStatus.CHECKED_OUT,
                isEarlyExit = isEarly,
                overtimeHours = String.format(Locale.US, "%.1f", ot).toDouble()
            ) ?: Attendance(
                employeeId = emp.id,
                employeeName = emp.name,
                department = emp.department,
                designation = emp.designation,
                date = todayStr,
                checkOutTime = currentTimeStr,
                workingHours = workingHours,
                status = AttendanceStatus.CHECKED_OUT,
                isEarlyExit = isEarly,
                overtimeHours = String.format(Locale.US, "%.1f", ot).toDouble(),
                captureMethod = method,
                location = location
            )
            dao.insertAttendance(record)

            dao.insertNotification(
                NotificationItem(
                    title = "Successful Check-out",
                    message = "${emp.name} checked out at $currentTimeStr. Working hours: ${String.format(Locale.US, "%.1f", workingHours)}h",
                    type = "CHECKIN",
                    timestamp = "Just now"
                )
            )

            logAudit(loggedInUser, "Attendance Check-out", "${emp.name} clocked out at $currentTimeStr ($workingHours hrs)")
            return Result.success(record)
        }
    }

    suspend fun modifyAttendance(
        attendance: Attendance,
        reason: String,
        loggedInUser: String
    ) {
        val updated = attendance.copy(
            modifiedBy = loggedInUser,
            modificationReason = reason
        )
        dao.insertAttendance(updated)
        logAudit(loggedInUser, "Attendance Modified", "Modified attendance of ${attendance.employeeName} for ${attendance.date}. Reason: $reason")
    }

    // Leave Management
    fun getAllLeaveRequests(): Flow<List<LeaveRequest>> = dao.getAllLeaveRequests()
    fun getLeaveRequestsByEmployee(employeeId: String): Flow<List<LeaveRequest>> = dao.getLeaveRequestsByEmployee(employeeId)

    suspend fun applyLeave(request: LeaveRequest, loggedInUser: String) {
        dao.insertLeaveRequest(request)
        dao.insertNotification(
            NotificationItem(
                title = "New Leave Application",
                message = "${request.employeeName} applied for ${request.daysCount} days of ${request.leaveType.name} leave",
                type = "LEAVE",
                timestamp = "Just now"
            )
        )
        logAudit(loggedInUser, "Leave Applied", "Leave application submitted by ${request.employeeName} (${request.daysCount} days)")
    }

    suspend fun updateLeaveStatus(request: LeaveRequest, newStatus: LeaveStatus, reviewer: String) {
        val reviewedOn = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val updated = request.copy(
            status = newStatus,
            reviewedBy = reviewer,
            reviewedOn = reviewedOn
        )
        dao.updateLeaveRequest(updated)

        dao.insertNotification(
            NotificationItem(
                title = if (newStatus == LeaveStatus.APPROVED) "Leave Approved" else "Leave Rejected",
                message = "Leave application for ${request.employeeName} (${request.daysCount} days) was ${newStatus.name.lowercase()}",
                type = "LEAVE",
                timestamp = "Just now"
            )
        )
        logAudit(reviewer, "Leave Status Updated", "Marked leave request for ${request.employeeName} as ${newStatus.name}")
    }

    // Holidays
    fun getAllHolidays(): Flow<List<Holiday>> = dao.getAllHolidays()
    suspend fun addHoliday(holiday: Holiday, loggedInUser: String) {
        dao.insertHoliday(holiday)
        logAudit(loggedInUser, "Holiday Added", "Added holiday: ${holiday.name} on ${holiday.date}")
    }
    suspend fun deleteHoliday(holiday: Holiday, loggedInUser: String) {
        dao.deleteHoliday(holiday)
        logAudit(loggedInUser, "Holiday Deleted", "Deleted holiday: ${holiday.name}")
    }

    // Notifications
    fun getAllNotifications(): Flow<List<NotificationItem>> = dao.getAllNotifications()
    suspend fun markNotificationRead(id: Long) = dao.markNotificationRead(id)
    suspend fun markAllNotificationsRead() = dao.markAllNotificationsRead()

    // Audit Logs
    fun getAllAuditLogs(): Flow<List<AuditLog>> = dao.getAllAuditLogs()

    private suspend fun logAudit(user: String, action: String, details: String) {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val timeStr = SimpleDateFormat("hh:mm a", Locale.US).format(Date())
        dao.insertAuditLog(
            AuditLog(
                user = user,
                action = action,
                date = dateStr,
                time = timeStr,
                details = details
            )
        )
    }

    // Settings
    fun getSettings(): Flow<CompanySettings?> = dao.getSettings()
    suspend fun saveSettings(settings: CompanySettings, loggedInUser: String) {
        dao.insertOrUpdateSettings(settings)
        logAudit(loggedInUser, "Settings Updated", "Updated company configuration and attendance policies")
    }

    private fun calculateHoursDifference(inTime: String, outTime: String): Double {
        return try {
            val format = SimpleDateFormat("hh:mm a", Locale.US)
            val dIn = format.parse(inTime)
            val dOut = format.parse(outTime)
            if (dIn != null && dOut != null) {
                var diffMs = dOut.time - dIn.time
                if (diffMs < 0) diffMs += 24 * 60 * 60 * 1000 // Cross midnight
                val hours = diffMs / (1000.0 * 60.0 * 60.0)
                String.format(Locale.US, "%.2f", hours).toDouble()
            } else 8.5
        } catch (e: Exception) {
            8.0
        }
    }
}
