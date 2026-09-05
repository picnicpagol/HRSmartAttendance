package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.DemoDataSeeder
import com.example.data.model.*
import com.example.data.repository.HRRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class NavScreen(val title: String) {
    DASHBOARD("Main Dashboard"),
    LIVE_STATUS("Live Status"),
    EMPLOYEES("Employees"),
    ATTENDANCE("Attendance"),
    LEAVES("Leave Management"),
    SHIFTS("Shift Management"),
    DEPARTMENTS("Departments"),
    CALENDAR("Attendance Calendar"),
    REPORTS("Reports & Export"),
    ANALYTICS("Visual Analytics"),
    PAYROLL("Payroll-Ready Summary"),
    HOLIDAYS("Holidays"),
    AUDIT_LOGS("Audit Logs"),
    NOTIFICATIONS("Notifications"),
    SETTINGS("System Settings"),
    EMPLOYEE_SELF_PORTAL("My Self-Service Portal")
}

class HRViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HRRepository

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _currentScreen = MutableStateFlow(NavScreen.DASHBOARD)
    val currentScreen: StateFlow<NavScreen> = _currentScreen.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    // Filters
    val employeeSearchQuery = MutableStateFlow("")
    val departmentFilter = MutableStateFlow<String?>(null)
    val statusFilter = MutableStateFlow<AttendanceStatus?>(null)
    val selectedDate = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))

    val employees: StateFlow<List<Employee>>
    val todayAttendances: StateFlow<List<Attendance>>
    val allAttendances: StateFlow<List<Attendance>>
    val departments: StateFlow<List<Department>>
    val shifts: StateFlow<List<Shift>>
    val leaveRequests: StateFlow<List<LeaveRequest>>
    val holidays: StateFlow<List<Holiday>>
    val notifications: StateFlow<List<NotificationItem>>
    val auditLogs: StateFlow<List<AuditLog>>
    val companySettings: StateFlow<CompanySettings>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = HRRepository(db.appDao())

        viewModelScope.launch {
            DemoDataSeeder.seedIfNeeded(db.appDao())
            // Default logged in as Admin for full immediate functionality
            val adminUser = repository.getUserByEmail("admin@company.com")
            _currentUser.value = adminUser
        }

        employees = repository.getAllEmployees().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        todayAttendances = selectedDate.flatMapLatest { date ->
            repository.getAttendancesByDate(date)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allAttendances = repository.getAllAttendances().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        departments = repository.getAllDepartments().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        shifts = repository.getAllShifts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        leaveRequests = repository.getAllLeaveRequests().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        holidays = repository.getAllHolidays().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        notifications = repository.getAllNotifications().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        auditLogs = repository.getAllAuditLogs().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        companySettings = repository.getSettings().map { it ?: CompanySettings() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CompanySettings())
    }

    fun setScreen(screen: NavScreen) {
        _currentScreen.value = screen
    }

    fun login(emailOrUser: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(emailOrUser) ?: repository.getUserByUsername(emailOrUser)
            if (user != null && user.passwordHash == pass) {
                _currentUser.value = user
                onResult(true, "Welcome, ${user.fullName} (${user.role.name})")
            } else {
                onResult(false, "Invalid credentials. Use demo accounts or verify password.")
            }
        }
    }

    fun quickSwitchRole(role: UserRole) {
        viewModelScope.launch {
            val targetEmail = when (role) {
                UserRole.ADMIN -> "admin@company.com"
                UserRole.HR -> "hr@company.com"
                UserRole.MANAGER -> "manager@company.com"
                UserRole.EMPLOYEE -> "employee@company.com"
            }
            val user = repository.getUserByEmail(targetEmail)
            if (user != null) {
                _currentUser.value = user
                _toastMessage.emit("Switched to ${user.fullName} (${role.name})")
                if (role == UserRole.EMPLOYEE) {
                    _currentScreen.value = NavScreen.EMPLOYEE_SELF_PORTAL
                } else {
                    _currentScreen.value = NavScreen.DASHBOARD
                }
            }
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    // Attendance Punch
    fun punchTodayAttendance(isCheckIn: Boolean, method: String, location: String = "Corporate HQ (Geofence Verified)") {
        val user = _currentUser.value ?: return
        val empId = if (user.employeeId.isNotBlank()) user.employeeId else "EMP-1001"
        viewModelScope.launch {
            val res = repository.punchAttendance(empId, isCheckIn, method, location, user.fullName)
            res.onSuccess {
                val action = if (isCheckIn) "Checked In" else "Checked Out"
                _toastMessage.emit("✓ Successfully $action via $method ($location)")
            }.onFailure {
                _toastMessage.emit("Error: ${it.message}")
            }
        }
    }

    fun modifyAttendanceRecord(attendance: Attendance, reason: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.modifyAttendance(attendance, reason, user.fullName)
            _toastMessage.emit("✓ Attendance for ${attendance.employeeName} updated")
        }
    }

    // Employee CRUD
    fun addEmployee(employee: Employee) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.addEmployee(employee, user.fullName)
            _toastMessage.emit("✓ Employee ${employee.name} added successfully")
        }
    }

    fun updateEmployee(employee: Employee) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateEmployee(employee, user.fullName)
            _toastMessage.emit("✓ Employee profile updated")
        }
    }

    fun deleteEmployee(employee: Employee) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteEmployee(employee, user.fullName)
            _toastMessage.emit("✓ Employee record deleted")
        }
    }

    // Leaves
    fun applyLeave(request: LeaveRequest) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.applyLeave(request, user.fullName)
            _toastMessage.emit("✓ Leave application submitted successfully")
        }
    }

    fun reviewLeave(request: LeaveRequest, approved: Boolean) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val status = if (approved) LeaveStatus.APPROVED else LeaveStatus.REJECTED
            repository.updateLeaveStatus(request, status, user.fullName)
            _toastMessage.emit("✓ Leave request ${status.name.lowercase()} for ${request.employeeName}")
        }
    }

    // Departments
    fun addDepartment(dept: Department) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.addDepartment(dept, user.fullName)
            _toastMessage.emit("✓ Department ${dept.name} added")
        }
    }

    fun deleteDepartment(dept: Department) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteDepartment(dept, user.fullName)
            _toastMessage.emit("✓ Department deleted")
        }
    }

    // Shifts
    fun addShift(shift: Shift) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.addShift(shift, user.fullName)
            _toastMessage.emit("✓ Shift ${shift.name} added")
        }
    }

    fun deleteShift(shift: Shift) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteShift(shift, user.fullName)
            _toastMessage.emit("✓ Shift deleted")
        }
    }

    // Holidays
    fun addHoliday(holiday: Holiday) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.addHoliday(holiday, user.fullName)
            _toastMessage.emit("✓ Holiday ${holiday.name} added")
        }
    }

    fun deleteHoliday(holiday: Holiday) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteHoliday(holiday, user.fullName)
            _toastMessage.emit("✓ Holiday deleted")
        }
    }

    // Settings
    fun updateSettings(settings: CompanySettings) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.saveSettings(settings, user.fullName)
            _toastMessage.emit("✓ System settings saved successfully")
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
            _toastMessage.emit("All notifications marked as read")
        }
    }
}
