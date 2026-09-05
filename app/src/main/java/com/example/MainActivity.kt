package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserRole
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.HRViewModel
import com.example.ui.viewmodel.NavScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val hrViewModel: HRViewModel = viewModel()
                HRAppContent(viewModel = hrViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HRAppContent(viewModel: HRViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadNotifications = notifications.count { !it.isRead }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showUserMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    if (currentUser == null) {
        LoginScreen(
            viewModel = viewModel,
            onLoginSuccess = {
                // currentUser will update reactively
            }
        )
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(300.dp),
                    drawerContainerColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Brand Drawer Header
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(HRPrimary)
                                .padding(20.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Badge,
                                            contentDescription = null,
                                            tint = HRPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "HR Smart Attendance",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = "Enterprise Management",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Logged user info
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    UserAvatarBadge(
                                        name = currentUser?.fullName ?: "User",
                                        colorHex = 0xFF2563EB,
                                        size = 32
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = currentUser?.fullName ?: "Admin User",
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "Role: ${currentUser?.role?.name ?: "ADMIN"}",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "CORE MODULES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )

                        // Nav items
                        DrawerNavItem(NavScreen.DASHBOARD, Icons.Default.Dashboard, currentScreen == NavScreen.DASHBOARD) {
                            viewModel.setScreen(NavScreen.DASHBOARD)
                            coroutineScope.launch { drawerState.close() }
                        }

                        DrawerNavItem(NavScreen.LIVE_STATUS, Icons.Default.Sensors, currentScreen == NavScreen.LIVE_STATUS) {
                            viewModel.setScreen(NavScreen.LIVE_STATUS)
                            coroutineScope.launch { drawerState.close() }
                        }

                        DrawerNavItem(NavScreen.EMPLOYEES, Icons.Default.Groups, currentScreen == NavScreen.EMPLOYEES) {
                            viewModel.setScreen(NavScreen.EMPLOYEES)
                            coroutineScope.launch { drawerState.close() }
                        }

                        DrawerNavItem(NavScreen.ATTENDANCE, Icons.Default.EventAvailable, currentScreen == NavScreen.ATTENDANCE) {
                            viewModel.setScreen(NavScreen.ATTENDANCE)
                            coroutineScope.launch { drawerState.close() }
                        }

                        DrawerNavItem(NavScreen.LEAVES, Icons.Default.FlightTakeoff, currentScreen == NavScreen.LEAVES) {
                            viewModel.setScreen(NavScreen.LEAVES)
                            coroutineScope.launch { drawerState.close() }
                        }

                        DrawerNavItem(NavScreen.CALENDAR, Icons.Default.CalendarMonth, currentScreen == NavScreen.CALENDAR) {
                            viewModel.setScreen(NavScreen.CALENDAR)
                            coroutineScope.launch { drawerState.close() }
                        }

                        Divider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                        Text(
                            text = "ORGANIZATION & HR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )

                        DrawerNavItem(NavScreen.SHIFTS, Icons.Default.Schedule, currentScreen == NavScreen.SHIFTS) {
                            viewModel.setScreen(NavScreen.SHIFTS)
                            coroutineScope.launch { drawerState.close() }
                        }

                        DrawerNavItem(NavScreen.DEPARTMENTS, Icons.Default.CorporateFare, currentScreen == NavScreen.DEPARTMENTS) {
                            viewModel.setScreen(NavScreen.DEPARTMENTS)
                            coroutineScope.launch { drawerState.close() }
                        }

                        DrawerNavItem(NavScreen.HOLIDAYS, Icons.Default.BeachAccess, currentScreen == NavScreen.HOLIDAYS) {
                            viewModel.setScreen(NavScreen.HOLIDAYS)
                            coroutineScope.launch { drawerState.close() }
                        }

                        DrawerNavItem(NavScreen.PAYROLL, Icons.Default.Payments, currentScreen == NavScreen.PAYROLL) {
                            viewModel.setScreen(NavScreen.PAYROLL)
                            coroutineScope.launch { drawerState.close() }
                        }

                        Divider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                        Text(
                            text = "REPORTS & SECURITY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )

                        DrawerNavItem(NavScreen.REPORTS, Icons.Default.Assessment, currentScreen == NavScreen.REPORTS) {
                            viewModel.setScreen(NavScreen.REPORTS)
                            coroutineScope.launch { drawerState.close() }
                        }

                        DrawerNavItem(NavScreen.ANALYTICS, Icons.Default.BarChart, currentScreen == NavScreen.ANALYTICS) {
                            viewModel.setScreen(NavScreen.ANALYTICS)
                            coroutineScope.launch { drawerState.close() }
                        }

                        DrawerNavItem(NavScreen.AUDIT_LOGS, Icons.Default.Security, currentScreen == NavScreen.AUDIT_LOGS) {
                            viewModel.setScreen(NavScreen.AUDIT_LOGS)
                            coroutineScope.launch { drawerState.close() }
                        }

                        DrawerNavItem(NavScreen.NOTIFICATIONS, Icons.Default.Notifications, currentScreen == NavScreen.NOTIFICATIONS) {
                            viewModel.setScreen(NavScreen.NOTIFICATIONS)
                            coroutineScope.launch { drawerState.close() }
                        }

                        DrawerNavItem(NavScreen.SETTINGS, Icons.Default.Settings, currentScreen == NavScreen.SETTINGS) {
                            viewModel.setScreen(NavScreen.SETTINGS)
                            coroutineScope.launch { drawerState.close() }
                        }

                        DrawerNavItem(NavScreen.EMPLOYEE_SELF_PORTAL, Icons.Default.PersonPin, currentScreen == NavScreen.EMPLOYEE_SELF_PORTAL) {
                            viewModel.setScreen(NavScreen.EMPLOYEE_SELF_PORTAL)
                            coroutineScope.launch { drawerState.close() }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = currentScreen.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = "HR Smart Attendance",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { coroutineScope.launch { drawerState.open() } },
                                modifier = Modifier.testTag("open_navigation_drawer")
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = "Open Navigation Menu")
                            }
                        },
                        actions = {
                            // Notifications bell with badge
                            IconButton(
                                onClick = { viewModel.setScreen(NavScreen.NOTIFICATIONS) },
                                modifier = Modifier.testTag("top_bar_notifications_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (unreadNotifications > 0) {
                                            Badge(
                                                containerColor = StatusAbsentRed,
                                                contentColor = Color.White
                                            ) {
                                                Text("$unreadNotifications")
                                            }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                                }
                            }

                            // Role switcher & User menu
                            Box {
                                IconButton(
                                    onClick = { showUserMenu = true },
                                    modifier = Modifier.testTag("top_bar_user_profile")
                                ) {
                                    UserAvatarBadge(
                                        name = currentUser?.fullName ?: "User",
                                        colorHex = 0xFF1E3A8A,
                                        size = 32
                                    )
                                }

                                DropdownMenu(
                                    expanded = showUserMenu,
                                    onDismissRequest = { showUserMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(currentUser?.fullName ?: "User", fontWeight = FontWeight.Bold)
                                                Text("Role: ${currentUser?.role?.name}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        },
                                        onClick = { showUserMenu = false },
                                        leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) }
                                    )
                                    Divider()
                                    DropdownMenuItem(
                                        text = { Text("Switch to Admin") },
                                        onClick = {
                                            showUserMenu = false
                                            viewModel.quickSwitchRole(UserRole.ADMIN)
                                        },
                                        leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Switch to HR") },
                                        onClick = {
                                            showUserMenu = false
                                            viewModel.quickSwitchRole(UserRole.HR)
                                        },
                                        leadingIcon = { Icon(Icons.Default.People, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Switch to Manager") },
                                        onClick = {
                                            showUserMenu = false
                                            viewModel.quickSwitchRole(UserRole.MANAGER)
                                        },
                                        leadingIcon = { Icon(Icons.Default.SupervisorAccount, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Switch to Employee Portal") },
                                        onClick = {
                                            showUserMenu = false
                                            viewModel.quickSwitchRole(UserRole.EMPLOYEE)
                                        },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                                    )
                                    Divider()
                                    DropdownMenuItem(
                                        text = { Text("Sign Out", color = StatusAbsentRed, fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            showUserMenu = false
                                            viewModel.logout()
                                        },
                                        leadingIcon = { Icon(Icons.Default.Logout, contentDescription = null, tint = StatusAbsentRed) }
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentScreen) {
                        NavScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                        NavScreen.LIVE_STATUS -> LiveStatusScreen(viewModel = viewModel)
                        NavScreen.EMPLOYEES -> EmployeeScreen(viewModel = viewModel)
                        NavScreen.ATTENDANCE -> AttendanceScreen(viewModel = viewModel)
                        NavScreen.LEAVES -> LeaveScreen(viewModel = viewModel)
                        NavScreen.SHIFTS -> ShiftScreen(viewModel = viewModel)
                        NavScreen.DEPARTMENTS -> DepartmentScreen(viewModel = viewModel)
                        NavScreen.CALENDAR -> CalendarScreen(viewModel = viewModel)
                        NavScreen.REPORTS -> ReportsScreen(viewModel = viewModel)
                        NavScreen.ANALYTICS -> AnalyticsScreen(viewModel = viewModel)
                        NavScreen.PAYROLL -> PayrollScreen(viewModel = viewModel)
                        NavScreen.HOLIDAYS -> HolidayScreen(viewModel = viewModel)
                        NavScreen.AUDIT_LOGS -> AuditLogScreen(viewModel = viewModel)
                        NavScreen.NOTIFICATIONS -> NotificationScreen(viewModel = viewModel)
                        NavScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                        NavScreen.EMPLOYEE_SELF_PORTAL -> EmployeePortalScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerNavItem(
    screen: NavScreen,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(screen.title, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium) },
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = screen.title, modifier = Modifier.size(20.dp)) },
        shape = RoundedCornerShape(10.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            selectedTextColor = HRPrimary,
            selectedIconColor = HRPrimary
        ),
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .testTag("nav_item_${screen.name.lowercase()}")
    )
}
