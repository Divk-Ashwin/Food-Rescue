package com.example.food_rescue

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.food_rescue.viewModel.FoodPost
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.util.Locale



val customTextStyle = TextStyle(
    fontWeight = FontWeight.Bold,
    fontSize = 20.sp,
    color = Color.Black
)

data class ChatMessage(
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

@Composable
fun LoginAndRegisterScreen(
    onLoginClicked: (String, String) -> Unit,
    onRegisterClicked: (String, String, String, String) -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (isRegistering) "Register for Food Rescue" else "Welcome to Food Rescue",
            style = MaterialTheme.typography.headlineLarge
        )

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation =
            if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = null
                    )
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (isRegistering) {
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                if (isRegistering) {
                    onRegisterClicked(email, password, name, phone)
                } else {
                    onLoginClicked(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isRegistering) "Register" else "Login")
        }

        TextButton(
            onClick = { isRegistering = !isRegistering },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (isRegistering) "Already have an account? Login"
                else "New here? Register"
            )
        }
    }
}

@Composable
fun FoodPostItem(
    foodPost: FoodPost,
    onFoodDetailsClicked: () -> Unit,
    onCopyLocation: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onFoodDetailsClicked)  // Clicks will trigger the details action
    ) {
        // Food name
        Text(
            text = "Food: ${foodPost.foodName}",
            style = MaterialTheme.typography.headlineMedium
        )

        // Servings
        Text(
            text = "Servings: ${foodPost.servings}",
            style = MaterialTheme.typography.bodyLarge
        )

        // Freshness
        Text(
            text = "Freshness: ${foodPost.freshness}",
            style = MaterialTheme.typography.bodyLarge
        )

        // Optional: Display donor name if available
        Text(
            text = "Donor: ${foodPost.donorName}",
            style = MaterialTheme.typography.bodyLarge
        )

        // Optional: Display donor phone if available
        Text(
            text = "Donor Phone: ${foodPost.donorPhone}",
            style = MaterialTheme.typography.bodyLarge
        )

        // Optional: Display donor address if available
        Text(
            text = "Donor Address: ${foodPost.location}",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        foodPost.location?.let {
            // Display latitude and longitude
            Text("Location: ${it.latitude}, ${it.longitude}", style = MaterialTheme.typography.bodyLarge)

            // Button to copy location
            Button(
                modifier = Modifier.padding(top = 8.dp),
                onClick = { onCopyLocation("${it.latitude}, ${it.longitude}") }
            ) {
                Text("Copy Location")
            }
        } ?: Text("Location not available", style = MaterialTheme.typography.bodyLarge)
    }
}





@Composable
fun RoleSelectionScreen(
    onDonorSelected: () -> Unit,
    onRecipientSelected: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Select Your Role", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // Donor image as a clickable button
        Image(
            painter = painterResource(id = R.drawable.donor), // Use the correct image resource
            contentDescription = "Donor",
            modifier = Modifier
                .clickable(onClick = onDonorSelected)
                .size(200.dp) // Adjust size as necessary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Recipient image as a clickable button
        Image(
            painter = painterResource(id = R.drawable.receiver), // Use the correct image resource
            contentDescription = "Recipient",
            modifier = Modifier
                .clickable(onClick = onRecipientSelected)
                .size(200.dp) // Adjust size as necessary
        )
    }
}

@Composable
fun PostFoodScreen(
    navController: NavController,
    onPostFood: (String, String, String, GeoPoint?, Int) -> Unit,
    onLogout: () -> Unit,
    locationViewModel: LocationViewModel = viewModel(),

) {
    var foodName by remember { mutableStateOf("") }
    var servings by remember { mutableStateOf("") }
    var freshness by remember { mutableStateOf("") }
    val context = LocalContext.current

    val location = locationViewModel.location.observeAsState()

    // Use mutableIntStateOf for primitive Int values
    var durationHours by remember { mutableIntStateOf(0) }
    var durationMinutes by remember { mutableIntStateOf(0) }
    BackHandler {
        navController.popBackStack() // Navigate to the previous screen
    }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = foodName,
            onValueChange = { foodName = it },
            label = { Text("Food Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = servings,
            onValueChange = { servings = it },
            label = { Text("Servings") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = freshness,
            onValueChange = { freshness = it },
            label = { Text("Freshness") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))


        // Duration Input Section with buttons for adjusting hours and minutes
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            // Increment by 30 minutes
            TextButton(onClick = {
                if (durationMinutes < 30) {
                    durationMinutes += 30
                } else {
                    durationMinutes = 0
                    if (durationHours < 8) { // Limit hours to 8
                        durationHours++
                    }
                }
            }) {
                Text("▲") // Increment button
            }

            // Display time in HH:mm format
            Text(text = String.format(Locale.US, "%02d:%02d", durationHours, durationMinutes), style = customTextStyle)

            // Decrement by 30 minutes
            TextButton(onClick = {
                // Only decrement if time is not at 00:00
                if (!(durationHours == 0 && durationMinutes == 0)) {
                    if (durationMinutes > 0) {
                        durationMinutes -= 30
                    } else {
                        // Reset minutes to 30 and decrement hours if minutes are 0
                        durationMinutes = 30
                        if (durationHours > 0) {
                            durationHours--
                        }
                    }
                }
            }) {
                Text("▼") // Decrement button
            }
        }







        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (foodName.isNotBlank() && servings.isNotBlank() && freshness.isNotBlank()) {
                    onPostFood(foodName, servings, freshness, location.value, durationHours * 60 + durationMinutes)
                } else {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Post Food")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            onLogout() // Call logout when button is clicked
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Logout")
        }

    }

    // Request location permissions on screen load
    LaunchedEffect(Unit) {
        ActivityCompat.requestPermissions(
            (context as Activity),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1001
        )
    }
}





class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val _location = MutableLiveData<GeoPoint?>()
    val location: LiveData<GeoPoint?> = _location

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    init {
        fetchLocation()
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                _location.value = GeoPoint(it.latitude, it.longitude)
            }
        }.addOnFailureListener {
            _location.value = null
        }
    }
}


    @Composable
    fun RecipientFoodFeedScreen(
        foodFeedViewModel: FoodFeedViewModel,
        onProfileClicked: () -> Unit,
        onChatClicked: (FoodPost) -> Unit,
        onFoodPostClicked: (FoodPost) -> Unit,
        onCopyLocation: (String) -> Unit

    ) {
        // Collect food posts from the ViewModel
        val foodPosts by foodFeedViewModel.foodPosts.observeAsState(emptyList())

        // State for dialog handling
        var showDialog by remember { mutableStateOf(false) }
        var selectedFood by remember { mutableStateOf<FoodPost?>(null) }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Header: Title
            Text(
                text = "Available Food Posts",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Display list of food posts
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(foodPosts) { foodPost ->
                    FoodPostItem(
                        foodPost = foodPost,
                        onFoodDetailsClicked = { onFoodPostClicked(foodPost) },
                        onCopyLocation = { onCopyLocation(it) }
                    )
                }
            }

            // Show dialog with post details
            if (showDialog && selectedFood != null) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = {
                        Text(
                            text = selectedFood!!.foodName,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Servings: ${selectedFood!!.servings}")
                            Text("Freshness: ${selectedFood!!.freshness}")
                            Text("Location: ${selectedFood!!.location}")
                            Text("Donor Name: ${selectedFood!!.donorName}")
                            Text("Donor Phone: ${selectedFood!!.donorPhone}")
                            Text("Donor Address: ${selectedFood!!.donorAddress}")

                            Button(
                                onClick = { onChatClicked(selectedFood!!) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Chat with Donor")
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                onFoodPostClicked(selectedFood!!)
                                showDialog = false
                            }
                        ) {
                            Text("Accept Food")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDialog = false }) {
                            Text("Close")
                        }
                    }
                )
            }

            // Buttons for navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Button(onClick = onProfileClicked) {
                    Text("Profile")
                }
            }
        }
    }


@Composable
fun ChatScreen(senderId: String, receiverId: String) {
    val firestore = FirebaseFirestore.getInstance()
    val messages = remember { mutableStateListOf<ChatMessage>() } // Explicit type for messages
    val messageInput = remember { mutableStateOf("") }

    LaunchedEffect(senderId, receiverId) {
        val query = firestore.collection("chatMessages")
            .whereIn("senderId", listOf(senderId, receiverId)) // Ensure both sender and receiver messages
            .orderBy("timestamp")

        query.addSnapshotListener { snapshot, _ ->
            snapshot?.documents
                ?.mapNotNull { documentSnapshot -> documentSnapshot.toObject(ChatMessage::class.java) }
                ?.let { newMessages ->
                    messages.clear()
                    messages.addAll(newMessages)
                }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) { // Weight to allow the text field to stay below
            items(messages) { message ->
                Text(text = "${message.senderId}: ${message.text}", modifier = Modifier.padding(8.dp))
            }
        }
        Row(modifier = Modifier.padding(8.dp)) {
            TextField(
                value = messageInput.value,
                onValueChange = { messageInput.value = it },
                label = { Text("Type a message") },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    val text = messageInput.value.trim()
                    if (text.isNotEmpty()) {
                        val newMessage = ChatMessage(
                            senderId = senderId,
                            receiverId = receiverId,
                            text = text,
                            timestamp = Timestamp.now()
                        )
                        firestore.collection("chatMessages").add(newMessage)
                        messageInput.value = "" // Clear input field
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Send")
            }
        }
    }
}

@Composable
fun RecipientProfileScreen(
    userId: String,
    foodPosts: List<FoodPost>,
    onChatClicked: (FoodPost) -> Unit,
    onProfileClicked: () -> Unit,
    onAcceptFood: (FoodPost) -> Unit  // Add this parameter
) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    // State variables
    var userDetails by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch user details from Firestore
    LaunchedEffect(userId) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    userDetails = document.getString("name") ?: "Unknown User"
                }
                isLoading = false
            }
            .addOnFailureListener { exception ->
                errorMessage = "Failed to load user details: ${exception.message}"
                isLoading = false
            }
    }

    // UI implementation
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (isLoading) {
            CircularProgressIndicator() // Show loading indicator
        } else {
            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error) // Display error message
            }

            Text(
                text = "User Profile for $userId: $userDetails",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                onProfileClicked() // Handle profile click
                Toast.makeText(context, "Profile Clicked", Toast.LENGTH_SHORT).show()
            }) {
                Text("View Full Profile")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display food posts
            foodPosts.forEach { foodPost ->
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(text = "Food Name: ${foodPost.foodName}")
                    Text(text = "Servings: ${foodPost.servings}")
                    Text(text = "Freshness: ${foodPost.freshness}")

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(onClick = {
                        onChatClicked(foodPost) // Handle chat click
                    }) {
                        Text("Chat with Donor")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(onClick = {
                        onAcceptFood(foodPost) // Handle food acceptance
                    }) {
                        Text("Accept Food")
                    }
                }
            }
        }
    }
}




@Composable
fun ProfileScreen(userId: String, navController: NavController, onLogout: () -> Unit) {

    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var donorProfile by remember { mutableStateOf<DonorProfile?>(null) } // New state for donor info
    var isLoading by remember { mutableStateOf(true) }

    // Fetch user profile data from Firestore
    LaunchedEffect(userId) {
        // Fetch user profile
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                userProfile = document.toObject(UserProfile::class.java)
            }
            .addOnFailureListener { exception ->
                context.showToast("Failed to load profile: ${exception.message}")
            }

        // Fetch donor profile (assuming donor information is in a separate collection)
        firestore.collection("donors").document(userId).get()
            .addOnSuccessListener { document ->
                donorProfile = document.toObject(DonorProfile::class.java)
                isLoading = false
            }
            .addOnFailureListener { exception ->
                context.showToast("Failed to load donor info: ${exception.message}")
                isLoading = false
            }
    }

    BackHandler {
        // Define custom back navigation behavior
        navController.popBackStack() // Navigate back to the previous screen
    }

    // UI for the profile screen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            // Show loading indicator while fetching profile
            CircularProgressIndicator()
        } else {
            userProfile?.let { profile ->
                Text("Welcome, ${profile.name}", style = MaterialTheme.typography.headlineLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Email: ${profile.email}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Role: ${profile.role}", style = MaterialTheme.typography.bodyLarge)
            }

            donorProfile?.let { donor ->
                Spacer(modifier = Modifier.height(24.dp))
                Text("Donor Name: ${donor.donorName}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Phone Number: ${donor.donorPhone}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Address: ${donor.donorAddress}", style = MaterialTheme.typography.bodyLarge)
            }

            // Logout button
            Button(onClick = {
                // Logout logic
                FirebaseAuth.getInstance().signOut()
                onLogout()  // Call the logout function
                navController.navigate("auth_screen") {
                    popUpTo("auth_screen") { inclusive = true } // Clear back stack
                }
            }) {
                Text("Logout")
            }
        }
    }
}

data class DonorProfile(
    val donorName: String = "",
    val donorPhone: String = "",
    val donorAddress: String = ""
)



@Composable
fun DonorPostFoodScreen(
    donorViewModel: DonorViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel(),
    foodFeedViewModel: FoodFeedViewModel = viewModel()
) {
    var foodName by remember { mutableStateOf("") }
    var servings by remember { mutableStateOf("") }
    var freshness by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Observe LiveData from FoodFeedViewModel
    val errorMessage by foodFeedViewModel.errorMessage.observeAsState(null)
    val foodPosts by foodFeedViewModel.foodPosts.observeAsState(emptyList())
    val currentLocation by locationViewModel.location.observeAsState()

    // Fetch food posts when screen loads
    LaunchedEffect(Unit) {
        foodFeedViewModel.fetchFoodPosts()
    }

    // Show errors if any
    errorMessage?.let { error ->
        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        // Food Name input
        TextField(
            value = foodName,
            onValueChange = { foodName = it },
            label = { Text("Food Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Servings input
        TextField(
            value = servings,
            onValueChange = { servings = it },
            label = { Text("Servings") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Freshness input
        TextField(
            value = freshness,
            onValueChange = { freshness = it },
            label = { Text("Freshness") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Duration input
        TextField(
            value = duration,
            onValueChange = { duration = it },
            label = { Text("Duration (hours)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        currentLocation?.let {
            Text(text = "Current location: ${it.latitude}, ${it.longitude}")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (foodName.isNotBlank() && servings.isNotBlank() && freshness.isNotBlank()) {
                    donorViewModel.addFoodPost(
                        foodName = foodName,
                        servings = servings,
                        freshness = freshness,
                        location = currentLocation,
                        duration = duration.toIntOrNull() ?: 4,
                        onSuccess = { postId ->
                            Toast.makeText(
                                context,
                                "Food post added! ID: $postId",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onFailure = { error ->
                            Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Post Food")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display food posts
        if (foodPosts.isNotEmpty()) {
            LazyColumn {
                items(foodPosts) { foodPost ->
                    Text(text = foodPost.foodName)
                }
            }
        } else {
            CircularProgressIndicator()
        }

    }
}


@Composable
fun FoodListScreen(viewModel: FoodFeedViewModel, onFoodClick: (FoodPost) -> Unit) {
    val foodPosts by viewModel.foodPosts.observeAsState(emptyList())

    // Trigger data fetching when this screen is launched
    LaunchedEffect(Unit) {
        viewModel.fetchFoodPosts()  // Make sure this function exists in FoodFeedViewModel
    }

    LazyColumn {
        items(foodPosts) { food ->
            Button(
                onClick = { onFoodClick(food) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(text = food.foodName)
            }
        }
    }
}

@Composable
fun FoodDetailsScreen(foodPost: FoodPost, onAcceptClick: () -> Unit) {
    val context = LocalContext.current

    // Trigger side effect to show a toast when food is accepted
    LaunchedEffect(Unit) {
        Toast.makeText(context, "Food Accepted!", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Food Name: ${foodPost.foodName}")
        Text("Servings: ${foodPost.servings}")
        Text("Freshness: ${foodPost.freshness}")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            // Trigger action when food is accepted
            onAcceptClick()
            // Show the toast after the action in the composable context
            Toast.makeText(context, "Food Accepted!", Toast.LENGTH_SHORT).show()
        }) {
            Text("Accept Food")
        }
    }
}
// Sample UserProfile data class
data class UserProfile(
    val name: String = "",
    val email: String = "",
    val role: String = "",
    val phone: String = "" // Add this field
)


