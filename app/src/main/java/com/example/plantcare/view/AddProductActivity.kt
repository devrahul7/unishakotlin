package com.example.plantcare.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.plantcare.model.ProductModel
import com.example.plantcare.repository.ProductRepositoryImpl
import com.example.plantcare.viewmodel.ProductViewModel

class AddProductActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AddProductScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen() {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var plantName by remember { mutableStateOf("") }
    var plantType by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val context = LocalContext.current
    val activity = context as? Activity

    val repo = remember { ProductRepositoryImpl() }
    val viewModel = remember { ProductViewModel(repo) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Plant") },
                navigationIcon = {
                    IconButton(onClick = {
                        activity?.finish()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Image Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clickable {
                            imagePickerLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    selectedImageUri?.let { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "Plant Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } ?: run {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Photo",
                                modifier = Modifier.size(48.dp),
                                tint = Color(0xFF4CAF50)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap to add plant photo",
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Plant Name Field
            OutlinedTextField(
                value = plantName,
                onValueChange = { plantName = it },
                label = { Text("Plant Name") },
                placeholder = { Text("e.g., Monstera Deliciosa") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    focusedLabelColor = Color(0xFF4CAF50)
                )
            )

            // Plant Type Field
            OutlinedTextField(
                value = plantType,
                onValueChange = { plantType = it },
                label = { Text("Plant Type/Category") },
                placeholder = { Text("e.g., Indoor Plant, Succulent, Flowering") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    focusedLabelColor = Color(0xFF4CAF50)
                )
            )

            // Price Field
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price") },
                placeholder = { Text("e.g., 25.99") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    focusedLabelColor = Color(0xFF4CAF50)
                )
            )

            // Description Field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Care Instructions & Description") },
                placeholder = { Text("Describe care requirements, lighting needs, watering schedule...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    focusedLabelColor = Color(0xFF4CAF50)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Add Button
            Button(
                onClick = {
                    val imageUri = selectedImageUri

                    when {
                        imageUri == null -> {
                            Toast.makeText(context, "Please select a plant image", Toast.LENGTH_SHORT).show()
                        }
                        plantName.isBlank() -> {
                            Toast.makeText(context, "Please enter plant name", Toast.LENGTH_SHORT).show()
                        }
                        plantType.isBlank() -> {
                            Toast.makeText(context, "Please enter plant type", Toast.LENGTH_SHORT).show()
                        }
                        price.isBlank() -> {
                            Toast.makeText(context, "Please enter price", Toast.LENGTH_SHORT).show()
                        }
                        description.isBlank() -> {
                            Toast.makeText(context, "Please enter care instructions", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            val priceValue = price.toDoubleOrNull()
                            if (priceValue == null) {
                                Toast.makeText(context, "Please enter valid price", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // Show loading state
                            Toast.makeText(context, "Adding plant...", Toast.LENGTH_SHORT).show()

                            viewModel.uploadImage(context, imageUri) { imageUrl ->
                                if (imageUrl != null) {
                                    // Create ProductModel
                                    val model = ProductModel(
                                        "",
                                        plantName,
                                        priceValue,
                                        description,
                                        imageUrl
                                    )

                                    viewModel.addProduct(model) { success, message ->
                                        if (success) {
                                            // Prepare result data for dashboard
                                            val resultIntent = Intent().apply {
                                                putExtra("plant_name", plantName)
                                                putExtra("plant_type", plantType)
                                                putExtra("plant_description", description)
                                                putExtra("plant_price", price)
                                                putExtra("plant_image_url", imageUrl)
                                            }

                                            // Set result and finish
                                            activity?.setResult(Activity.RESULT_OK, resultIntent)
                                            Toast.makeText(context, "Plant added successfully!", Toast.LENGTH_SHORT).show()
                                            activity?.finish()
                                        } else {
                                            Toast.makeText(context, "Failed to add plant: $message", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Failed to upload image. Please try again.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text(
                    text = "Add Plant to Garden",
                    color = Color.White
                )
            }

            // Cancel Button
            OutlinedButton(
                onClick = {
                    activity?.finish()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF4CAF50)
                )
            ) {
                Text("Cancel")
            }
        }
    }
}
