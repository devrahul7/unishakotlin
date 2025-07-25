package com.example.plantcare.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plantcare.R
import com.example.plantcare.repository.ProductRepositoryImpl
import com.example.plantcare.viewmodel.ProductViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Add ActionId enum
enum class ActionId {
    WATER_PLANTS,
    ADD_NEW_PLANT,
    CARE_SCHEDULE,
    PLANT_HEALTH
}

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get data from intent
        val username = intent.getStringExtra("username") ?: "User"
        val password = intent.getStringExtra("password") ?: ""

        setContent {
            DashboardBody(username = username)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardBody(username: String = "User") {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    // Create a mutable state list for recent activities
    val recentActivities = remember {
        mutableStateListOf<RecentActivity>().apply {
            addAll(getRecentActivities())
        }
    }

    // Create repository and viewmodel for delete operations
    val repo = remember { ProductRepositoryImpl() }
    val viewModel = remember { ProductViewModel(repo) }

    // Register launcher for AddProductActivity
    val addPlantLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val plantName = data.getStringExtra("plant_name") ?: "New Plant"
                val plantType = data.getStringExtra("plant_type") ?: ""
                val plantDescription = data.getStringExtra("plant_description") ?: ""
                val plantPrice = data.getStringExtra("plant_price") ?: ""
                val plantImageUrl = data.getStringExtra("plant_image_url") ?: ""
                val plantId = data.getStringExtra("plant_id") ?: System.currentTimeMillis().toString()

                // Create current timestamp
                val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

                // Check if this is an edit operation
                val isEdit = data.getBooleanExtra("is_edit", false)

                if (isEdit) {
                    // Find and update existing plant in the list
                    val index = recentActivities.indexOfFirst {
                        it.plantId == plantId && it.isPlant
                    }
                    if (index != -1) {
                        recentActivities[index] = recentActivities[index].copy(
                            title = "Plant Updated",
                            description = "$plantName${if (plantType.isNotEmpty()) " • $plantType" else ""}",
                            time = "Just now"
                        )
                    }
                    Toast.makeText(context, "$plantName updated successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    // Add new plant to the beginning of the list
                    recentActivities.add(
                        0,
                        RecentActivity(
                            title = "New Plant Added",
                            description = "$plantName${if (plantType.isNotEmpty()) " • $plantType" else ""}",
                            time = "Just now",
                            icon = Icons.Default.Add,
                            iconColor = Color(0xFF8BC34A),
                            isPlant = true,
                            plantId = plantId,
                            plantName = plantName,
                            plantType = plantType,
                            plantDescription = plantDescription,
                            plantPrice = plantPrice,
                            plantImageUrl = plantImageUrl
                        )
                    )
                    Toast.makeText(context, "$plantName added successfully!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Register launcher for UpdateProductActivity
    val updatePlantLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val plantName = data.getStringExtra("plant_name") ?: "Updated Plant"
                val plantType = data.getStringExtra("plant_type") ?: ""
                val plantId = data.getStringExtra("plant_id") ?: ""
                val plantDescription = data.getStringExtra("plant_description") ?: ""
                val plantPrice = data.getStringExtra("plant_price") ?: ""
                val plantImageUrl = data.getStringExtra("plant_image_url") ?: ""

                // Find and update the plant in recent activities
                val index = recentActivities.indexOfFirst {
                    it.plantId == plantId && it.isPlant
                }
                if (index != -1) {
                    recentActivities[index] = recentActivities[index].copy(
                        title = "Plant Updated",
                        description = "$plantName${if (plantType.isNotEmpty()) " • $plantType" else ""}",
                        time = "Just now",
                        plantName = plantName,
                        plantType = plantType,
                        plantDescription = plantDescription,
                        plantPrice = plantPrice,
                        plantImageUrl = plantImageUrl
                    )
                }
                Toast.makeText(context, "$plantName updated successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Good morning, plant parent!",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = username,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "Plant care reminders", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Care Reminders",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = {
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50)
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Cards Section
            item {
                Text(
                    text = "Your Plant Journey",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(getStatsData()) { stat ->
                        StatsCard(stat = stat)
                    }
                }
            }

            // Weekly Progress Section
            item {
                WeeklyProgressCard()
            }

            // Quick Actions Section
            item {
                Text(
                    text = "Plant Care Actions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }

            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(320.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(getQuickActions()) { action ->
                        QuickActionCard(action = action) {
                            when (action.id) {
                                ActionId.ADD_NEW_PLANT -> {
                                    val intent = Intent(context, AddProductActivity::class.java)
                                    addPlantLauncher.launch(intent)
                                }
                                ActionId.WATER_PLANTS -> {
                                    // Add new watering activity
                                    recentActivities.add(
                                        0,
                                        RecentActivity(
                                            title = "Plants Watered",
                                            description = "Daily watering completed",
                                            time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                                            icon = Icons.Default.Refresh,
                                            iconColor = Color(0xFF2196F3),
                                            isPlant = false
                                        )
                                    )
                                    Toast.makeText(context, "Plants watered successfully!", Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    Toast.makeText(context, "${action.title} clicked", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }

            // Recent Activity Section
            item {
                Text(
                    text = "Recent Plant Care & Additions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }

            items(recentActivities) { activity ->
                RecentActivityCard(
                    activity = activity,
                    onEdit = { plantActivity ->
                        // Navigate to UpdateProductActivity
                        val intent = Intent(context, UpdateProductActivity::class.java).apply {
                            putExtra("plant_id", plantActivity.plantId)
                            putExtra("plant_name", plantActivity.plantName)
                            putExtra("plant_type", plantActivity.plantType)
                            putExtra("plant_description", plantActivity.plantDescription)
                            putExtra("plant_price", plantActivity.plantPrice)
                            putExtra("plant_image_url", plantActivity.plantImageUrl)
                        }
                        updatePlantLauncher.launch(intent)
                    },
                    onDelete = { plantActivity ->
                        // Delete from database and remove from list
                        if (plantActivity.plantId.isNotEmpty()) {
                            viewModel.deleteProduct(plantActivity.plantId) { success, message ->
                                if (success) {
                                    recentActivities.remove(plantActivity)
                                    Toast.makeText(context, "Plant deleted successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to delete plant: $message", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            recentActivities.remove(plantActivity)
                            Toast.makeText(context, "Activity removed", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun StatsCard(stat: StatData) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stat.value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = stat.color
            )
            Text(
                text = stat.label,
                fontSize = 10.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WeeklyProgressCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weekly Care Goal",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "92% Complete",
                    fontSize = 14.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = 0.92f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF4CAF50),
                trackColor = Color(0xFFE8F5E8)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "23 out of 25 care tasks completed this week",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun QuickActionCard(action: QuickAction, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = action.backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.title,
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = action.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RecentActivityCard(
    activity: RecentActivity,
    onEdit: (RecentActivity) -> Unit = {},
    onDelete: (RecentActivity) -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        activity.iconColor.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = activity.icon,
                    contentDescription = null,
                    tint = activity.iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = activity.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = activity.description,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Text(
                text = activity.time,
                fontSize = 12.sp,
                color = Color.Gray
            )

            // Show menu only for plant activities
            if (activity.isPlant) {
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Color.Gray
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        modifier = Modifier.size(18.dp),
                                        tint = Color(0xFF4CAF50)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Edit Plant")
                                }
                            },
                            onClick = {
                                expanded = false
                                onEdit(activity)
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        modifier = Modifier.size(18.dp),
                                        tint = Color(0xFFE53E3E)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Delete Plant")
                                }
                            },
                            onClick = {
                                expanded = false
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Plant") },
            text = { Text("Are you sure you want to delete this plant? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete(activity)
                    }
                ) {
                    Text("Delete", color = Color(0xFFE53E3E))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Updated Data Classes
data class StatData(
    val value: String,
    val label: String,
    val color: Color
)

data class QuickAction(
    val id: ActionId,
    val title: String,
    val icon: ImageVector,
    val backgroundColor: Color
)

data class RecentActivity(
    val title: String,
    val description: String,
    val time: String,
    val icon: ImageVector,
    val iconColor: Color,
    val isPlant: Boolean = false,
    val plantId: String = "",
    val plantName: String = "",
    val plantType: String = "",
    val plantDescription: String = "",
    val plantPrice: String = "",
    val plantImageUrl: String = ""
)

// Data Functions
fun getStatsData(): List<StatData> = listOf(
    StatData("12", "Plants Cared", Color(0xFF4CAF50)),
    StatData("28", "Days Streak", Color(0xFF2196F3)),
    StatData("10", "Healthy Plants", Color(0xFF8BC34A)),
    StatData("4.8", "Care Rating", Color(0xFFFF9800))
)

fun getQuickActions(): List<QuickAction> = listOf(
    QuickAction(ActionId.WATER_PLANTS, "Water Plants", Icons.Default.Refresh, Color(0xFF2196F3)),
    QuickAction(ActionId.ADD_NEW_PLANT, "Add New Plant", Icons.Default.Add, Color(0xFF4CAF50)),
    QuickAction(ActionId.CARE_SCHEDULE, "Care Schedule", Icons.Default.Settings, Color(0xFF9C27B0)),
    QuickAction(ActionId.PLANT_HEALTH, "Plant Health", Icons.Default.Star, Color(0xFFE91E63))
)

fun getRecentActivities(): List<RecentActivity> = listOf(
    RecentActivity(
        "Watered Monstera",
        "Morning watering • Next due in 3 days",
        "2 hours ago",
        Icons.Default.Refresh,
        Color(0xFF2196F3),
        isPlant = false
    ),
    RecentActivity(
        "Added fertilizer",
        "Snake Plant • Nutrient boost applied",
        "1 day ago",
        Icons.Default.Settings,
        Color(0xFF4CAF50),
        isPlant = false
    ),
    RecentActivity(
        "Plant health check",
        "All plants looking healthy!",
        "2 days ago",
        Icons.Default.Check,
        Color(0xFFFF9800),
        isPlant = false
    )
)

@Preview
@Composable
fun DashboardPreview() {
    DashboardBody(username = "Plant Lover")
}
