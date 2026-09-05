package com.example.data.db

import com.example.data.model.*
import java.text.SimpleDateFormat
import java.util.*

object DemoDataSeeder {

    suspend fun seedIfNeeded(dao: AppDao) {
        val existingSettings = dao.getSettingsSync()
        if (existingSettings != null) {
            return // Already seeded
        }

        // 1. Company Settings
        val settings = CompanySettings()
        dao.insertOrUpdateSettings(settings)

        // 2. Users (Admin, HR, Manager, Employee)
        val users = listOf(
            User(
                username = "admin",
                email = "admin@company.com",
                passwordHash = "admin123",
                fullName = "Arif Rahman",
                role = UserRole.ADMIN,
                employeeId = "EMP-1001",
                department = "Administration",
                designation = "Chief Executive Officer",
                avatarColor = 0xFF1E3A8A
            ),
            User(
                username = "hr_sarah",
                email = "hr@company.com",
                passwordHash = "hr123",
                fullName = "Sarah Chowdhury",
                role = UserRole.HR,
                employeeId = "EMP-1002",
                department = "Human Resources",
                designation = "Lead HR Manager",
                avatarColor = 0xFF7C3AED
            ),
            User(
                username = "manager_rafiq",
                email = "manager@company.com",
                passwordHash = "manager123",
                fullName = "Rafiqul Islam",
                role = UserRole.MANAGER,
                employeeId = "EMP-1003",
                department = "Engineering",
                designation = "VP of Engineering",
                avatarColor = 0xFF0D9488
            ),
            User(
                username = "tanvir_dev",
                email = "employee@company.com",
                passwordHash = "emp123",
                fullName = "Tanvir Ahmed",
                role = UserRole.EMPLOYEE,
                employeeId = "EMP-1004",
                department = "Engineering",
                designation = "Senior Android Engineer",
                avatarColor = 0xFF2563EB
            )
        )
        for (u in users) {
            dao.insertUser(u)
        }

        // 3. Departments (5 Departments)
        val departments = listOf(
            Department(name = "Engineering", code = "ENG", headName = "Rafiqul Islam", employeeCount = 10, description = "Software architecture & mobile development"),
            Department(name = "Human Resources", code = "HR", headName = "Sarah Chowdhury", employeeCount = 4, description = "Talent acquisition & people operations"),
            Department(name = "Marketing & Growth", code = "MKT", headName = "Farzana Yasmin", employeeCount = 6, description = "Brand marketing & business acquisition"),
            Department(name = "Finance & Accounts", code = "FIN", headName = "Kamrul Hassan", employeeCount = 5, description = "Payroll, billing & financial audits"),
            Department(name = "Customer Operations", code = "OPS", headName = "Mahmudur Rahman", employeeCount = 5, description = "Client support & quality assurance")
        )
        dao.insertDepartments(departments)

        // 4. Shifts (3 Shifts)
        val shifts = listOf(
            Shift(id = 1, name = "General Shift", startTime = "09:00 AM", endTime = "06:00 PM", gracePeriodMinutes = 15, breakMinutes = 60, overtimeRule = "1.5x after 9 hrs"),
            Shift(id = 2, name = "Morning Shift", startTime = "08:00 AM", endTime = "05:00 PM", gracePeriodMinutes = 15, breakMinutes = 60, overtimeRule = "1.5x after 9 hrs"),
            Shift(id = 3, name = "Night Shift", startTime = "10:00 PM", endTime = "06:00 AM", gracePeriodMinutes = 10, breakMinutes = 45, overtimeRule = "2.0x after 8 hrs")
        )
        dao.insertShifts(shifts)

        // 5. Employees (30+ Employees)
        val employeeData = listOf(
            Triple("Arif Rahman", "Administration", "Chief Executive Officer"),
            Triple("Sarah Chowdhury", "Human Resources", "Lead HR Manager"),
            Triple("Rafiqul Islam", "Engineering", "VP of Engineering"),
            Triple("Tanvir Ahmed", "Engineering", "Senior Android Engineer"),
            Triple("Nusrat Jahan", "Human Resources", "Talent Acquisition Specialist"),
            Triple("Zubair Hossain", "Engineering", "Principal Backend Architect"),
            Triple("Sadia Afrin", "Engineering", "Senior Frontend Developer"),
            Triple("Mehedi Hasan", "Marketing & Growth", "Digital Marketing Lead"),
            Triple("Farzana Yasmin", "Marketing & Growth", "VP of Brand Strategy"),
            Triple("Kamrul Hassan", "Finance & Accounts", "Senior Finance Controller"),
            Triple("Amina Begum", "Finance & Accounts", "Payroll Specialist"),
            Triple("Mahmudur Rahman", "Customer Operations", "Operations Director"),
            Triple("Fahim Montasir", "Engineering", "DevOps & Cloud Engineer"),
            Triple("Tasnim Tabassum", "Engineering", "UI/UX Product Designer"),
            Triple("Shakil Anwar", "Customer Operations", "Customer Support Lead"),
            Triple("Sabrina Sultana", "Human Resources", "HR Generalist"),
            Triple("Imtiaz Shovon", "Engineering", "Full Stack Developer"),
            Triple("Rezaul Karim", "Finance & Accounts", "Accounts Executive"),
            Triple("Nabila Anjum", "Marketing & Growth", "Content Strategist"),
            Triple("Asif Iqbal", "Customer Operations", "Support Specialist"),
            Triple("Shahriar Kabir", "Engineering", "QA Automation Engineer"),
            Triple("Mithila Roy", "Marketing & Growth", "SEO & Performance Specialist"),
            Triple("Nasir Uddin", "Finance & Accounts", "Tax & Audit Officer"),
            Triple("Shamima Akter", "Human Resources", "Employee Relations Officer"),
            Triple("Faisal Mahmud", "Engineering", "Security & Compliance Engineer"),
            Triple("Laila Arjumand", "Marketing & Growth", "Growth Marketer"),
            Triple("Tariqul Islam", "Customer Operations", "Customer Success Manager"),
            Triple("Anika Bushra", "Engineering", "Junior Android Developer"),
            Triple("Mustafizur Rahman", "Engineering", "Database Administrator"),
            Triple("Sharmin Sultana", "Customer Operations", "Operations Coordinator"),
            Triple("Jubair Al Mahmud", "Finance & Accounts", "Financial Analyst")
        )

        val avatarColors = listOf(
            0xFF2563EB, 0xFF7C3AED, 0xFF0D9488, 0xFFDC2626, 0xFFEA580C,
            0xFFD97706, 0xFF059669, 0xFF0891B2, 0xFF4F46E5, 0xFF9333EA
        )

        val employees = employeeData.mapIndexed { idx, (name, dept, desig) ->
            val empId = "EMP-${1001 + idx}"
            val emailName = name.lowercase().replace(" ", ".")
            val shift = if (idx % 7 == 0) shifts[1] else if (idx % 11 == 0) shifts[2] else shifts[0]
            val salary = 45000.0 + (idx * 3200.0)
            Employee(
                id = empId,
                name = name,
                email = "$emailName@company.com",
                phone = "+880 17${10000000 + (idx * 273182) % 90000000}",
                department = dept,
                designation = desig,
                joiningDate = "2023-${(idx % 12 + 1).toString().padStart(2, '0')}-15",
                shiftId = shift.id,
                shiftName = shift.name,
                salary = salary,
                emergencyContact = "+880 18${20000000 + (idx * 314159) % 80000000}",
                status = if (idx == 24) "On Leave" else "Active",
                avatarColor = avatarColors[idx % avatarColors.size]
            )
        }
        dao.insertEmployees(employees)

        // 6. 30 Days Attendance Data
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        val todayStr = dateFormat.format(cal.time)

        val attendancesToInsert = mutableListOf<Attendance>()

        // Generate past 29 days
        for (dayOffset in 29 downTo 1) {
            val pastCal = Calendar.getInstance()
            pastCal.add(Calendar.DAY_OF_YEAR, -dayOffset)
            val dayOfWeek = pastCal.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek == Calendar.FRIDAY || dayOfWeek == Calendar.SATURDAY) {
                continue // Weekend
            }
            val pastDateStr = dateFormat.format(pastCal.time)

            employees.forEachIndexed { empIdx, emp ->
                val randSeed = (pastDateStr.hashCode() + empIdx * 31).let { if (it < 0) -it else it }
                val roll = randSeed % 100

                val (status, inTime, outTime, hrs, late, early, ot) = when {
                    roll < 72 -> { // Present on time
                        val minuteIn = (randSeed % 14).let { if (it < 10) "0$it" else "$it" }
                        val minuteOut = (randSeed % 25 + 5).let { if (it < 10) "0$it" else "$it" }
                        AttendanceData(AttendanceStatus.PRESENT, "08:$minuteIn AM", "06:$minuteOut PM", 9.2, false, false, 0.5)
                    }
                    roll < 86 -> { // Late
                        val minLate = (randSeed % 35 + 16).let { if (it < 10) "0$it" else "$it" }
                        AttendanceData(AttendanceStatus.LATE, "09:$minLate AM", "06:15 PM", 8.6, true, false, 0.0)
                    }
                    roll < 93 -> { // On Leave
                        AttendanceData(AttendanceStatus.ON_LEAVE, null, null, 0.0, false, false, 0.0)
                    }
                    else -> { // Absent
                        AttendanceData(AttendanceStatus.ABSENT, null, null, 0.0, false, false, 0.0)
                    }
                }

                attendancesToInsert.add(
                    Attendance(
                        employeeId = emp.id,
                        employeeName = emp.name,
                        department = emp.department,
                        designation = emp.designation,
                        date = pastDateStr,
                        checkInTime = inTime,
                        checkOutTime = outTime,
                        workingHours = hrs,
                        status = status,
                        isLate = late,
                        isEarlyExit = early,
                        overtimeHours = ot,
                        captureMethod = if (roll % 3 == 0) "QR" else if (roll % 4 == 0) "GPS" else "BIOMETRIC"
                    )
                )
            }
        }

        // Today's live attendance
        employees.forEachIndexed { idx, emp ->
            val roll = (idx * 17) % 100
            val (status, inTime, outTime, hrs, late, early, ot, method) = when {
                idx == 3 -> { // Tanvir Ahmed (Employee Demo Account) - Present & Working
                    AttendanceData(AttendanceStatus.PRESENT, "08:52 AM", null, 4.5, false, false, 0.0, "BIOMETRIC")
                }
                idx == 0 -> { // Arif Rahman (Admin) - Present
                    AttendanceData(AttendanceStatus.PRESENT, "08:45 AM", null, 4.6, false, false, 0.0, "QR")
                }
                idx == 1 -> { // Sarah (HR) - Present
                    AttendanceData(AttendanceStatus.PRESENT, "08:58 AM", null, 4.4, false, false, 0.0, "GPS")
                }
                idx == 2 -> { // Rafiqul (Manager) - Late
                    AttendanceData(AttendanceStatus.LATE, "09:28 AM", null, 3.9, true, false, 0.0, "BIOMETRIC")
                }
                roll < 50 -> { // Present
                    val min = (idx % 14).let { if (it < 10) "0$it" else "$it" }
                    AttendanceData(AttendanceStatus.PRESENT, "08:$min AM", null, 4.2, false, false, 0.0, "BIOMETRIC")
                }
                roll < 70 -> { // Late
                    val min = (16 + (idx % 30)).let { if (it < 10) "0$it" else "$it" }
                    AttendanceData(AttendanceStatus.LATE, "09:$min AM", null, 3.6, true, false, 0.0, "GPS")
                }
                roll < 80 -> { // Checked Out Early
                    AttendanceData(AttendanceStatus.CHECKED_OUT, "08:30 AM", "01:15 PM", 4.75, false, true, 0.0, "QR")
                }
                roll < 88 -> { // On Leave
                    AttendanceData(AttendanceStatus.ON_LEAVE, null, null, 0.0, false, false, 0.0, "MANUAL")
                }
                roll < 94 -> { // Absent
                    AttendanceData(AttendanceStatus.ABSENT, null, null, 0.0, false, false, 0.0, "MANUAL")
                }
                else -> { // Not Checked In Yet
                    AttendanceData(AttendanceStatus.NOT_CHECKED_IN, null, null, 0.0, false, false, 0.0, "MANUAL")
                }
            }

            attendancesToInsert.add(
                Attendance(
                    employeeId = emp.id,
                    employeeName = emp.name,
                    department = emp.department,
                    designation = emp.designation,
                    date = todayStr,
                    checkInTime = inTime,
                    checkOutTime = outTime,
                    workingHours = hrs,
                    status = status,
                    isLate = late,
                    isEarlyExit = early,
                    overtimeHours = ot,
                    captureMethod = method
                )
            )
        }

        dao.insertAttendances(attendancesToInsert)

        // 7. Leave Requests
        val leaveRequests = listOf(
            LeaveRequest(
                employeeId = "EMP-1007",
                employeeName = "Sadia Afrin",
                department = "Engineering",
                leaveType = LeaveType.CASUAL,
                startDate = "2026-09-08",
                endDate = "2026-09-09",
                daysCount = 2,
                reason = "Family vacation and personal errands",
                status = LeaveStatus.PENDING,
                appliedOn = "2026-09-03"
            ),
            LeaveRequest(
                employeeId = "EMP-1011",
                employeeName = "Amina Begum",
                department = "Finance & Accounts",
                leaveType = LeaveType.SICK,
                startDate = "2026-09-05",
                endDate = "2026-09-06",
                daysCount = 2,
                reason = "Seasonal viral fever and physician consultation",
                documentName = "medical_certificate.pdf",
                status = LeaveStatus.PENDING,
                appliedOn = "2026-09-04"
            ),
            LeaveRequest(
                employeeId = "EMP-1004",
                employeeName = "Tanvir Ahmed",
                department = "Engineering",
                leaveType = LeaveType.ANNUAL,
                startDate = "2026-09-20",
                endDate = "2026-09-25",
                daysCount = 5,
                reason = "Annual family tour to Sylhet",
                status = LeaveStatus.PENDING,
                appliedOn = "2026-09-02"
            ),
            LeaveRequest(
                employeeId = "EMP-1014",
                employeeName = "Tasnim Tabassum",
                department = "Engineering",
                leaveType = LeaveType.CASUAL,
                startDate = "2026-08-25",
                endDate = "2026-08-26",
                daysCount = 2,
                reason = "Attending sibling wedding ceremony",
                status = LeaveStatus.APPROVED,
                appliedOn = "2026-08-20",
                reviewedBy = "Sarah Chowdhury",
                reviewedOn = "2026-08-21"
            ),
            LeaveRequest(
                employeeId = "EMP-1025",
                employeeName = "Faisal Mahmud",
                department = "Engineering",
                leaveType = LeaveType.EMERGENCY,
                startDate = "2026-08-28",
                endDate = "2026-08-29",
                daysCount = 2,
                reason = "Urgent residential electrical repair",
                status = LeaveStatus.REJECTED,
                appliedOn = "2026-08-27",
                reviewedBy = "Sarah Chowdhury",
                reviewedOn = "2026-08-27"
            )
        )
        dao.insertLeaveRequests(leaveRequests)

        // 8. Holidays
        val holidays = listOf(
            Holiday(name = "Shaheed Day & Int'l Mother Language Day", date = "2026-02-21", type = "National", description = "Tribute to language martyrs"),
            Holiday(name = "Independence Day", date = "2026-03-26", type = "National", description = "National Independence Celebration"),
            Holiday(name = "Bangla New Year (Pohela Boishakh)", date = "2026-04-14", type = "Cultural", description = "Celebration of Bengali Year"),
            Holiday(name = "May Day (Labor Day)", date = "2026-05-01", type = "International", description = "International Workers' Day"),
            Holiday(name = "Eid-ul-Fitr", date = "2026-03-20", type = "Religious", description = "Islamic Festival celebration"),
            Holiday(name = "Eid-ul-Adha", date = "2026-05-27", type = "Religious", description = "Feast of Sacrifice"),
            Holiday(name = "National Mourning Day", date = "2026-08-15", type = "National", description = "National Day of Remembrance"),
            Holiday(name = "Victory Day", date = "2026-12-16", type = "National", description = "Commemorating victory in 1971"),
            Holiday(name = "Christmas Day", date = "2026-12-25", type = "Religious", description = "Christian Holiday")
        )
        dao.insertHolidays(holidays)

        // 9. Notifications
        val notifications = listOf(
            NotificationItem(
                title = "New Leave Application",
                message = "Sadia Afrin applied for 2 days of Casual Leave (Sep 8 - Sep 9)",
                type = "LEAVE",
                timestamp = "10 mins ago"
            ),
            NotificationItem(
                title = "Late Entry Alert",
                message = "Rafiqul Islam checked in at 09:28 AM (28 minutes after shift start)",
                type = "LATE",
                timestamp = "1 hour ago"
            ),
            NotificationItem(
                title = "HR Announcement",
                message = "Quarterly all-hands meeting scheduled for coming Thursday at 03:00 PM.",
                type = "ANNOUNCEMENT",
                timestamp = "3 hours ago"
            ),
            NotificationItem(
                title = "Successful Check-in",
                message = "Tanvir Ahmed verified via Biometric punch at 08:52 AM",
                type = "CHECKIN",
                timestamp = "4 hours ago"
            ),
            NotificationItem(
                title = "Attendance Reminder",
                message = "3 employees have not checked in yet for today's General Shift.",
                type = "SYSTEM",
                timestamp = "5 hours ago"
            )
        )
        dao.insertNotifications(notifications)

        // 10. Audit Logs
        val auditLogs = listOf(
            AuditLog(
                user = "Arif Rahman (Admin)",
                action = "System Login",
                date = todayStr,
                time = "08:46 AM",
                details = "Admin logged in via Enterprise Mobile App"
            ),
            AuditLog(
                user = "Sarah Chowdhury (HR)",
                action = "Attendance Modified",
                date = todayStr,
                time = "09:15 AM",
                details = "Manual time correction for Mehedi Hasan (08:55 AM) - verified gate record"
            ),
            AuditLog(
                user = "Sarah Chowdhury (HR)",
                action = "Leave Approved",
                date = "2026-08-21",
                time = "11:30 AM",
                details = "Approved Casual Leave for Tasnim Tabassum"
            ),
            AuditLog(
                user = "Arif Rahman (Admin)",
                action = "Shift Settings Updated",
                date = "2026-08-15",
                time = "04:00 PM",
                details = "Updated grace period for Morning Shift to 15 minutes"
            )
        )
        dao.insertAuditLogs(auditLogs)
    }

    private data class AttendanceData(
        val status: AttendanceStatus,
        val inTime: String?,
        val outTime: String?,
        val hrs: Double,
        val late: Boolean,
        val early: Boolean,
        val ot: Double,
        val method: String = "BIOMETRIC"
    )
}
