///No errors
package com.example.food_rescue

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.food_rescue.repository.FirestoreRepository
import com.example.food_rescue.ui.theme.FoodRescueTheme
import com.example.food_rescue.viewModel.FoodPost
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch


// In MainActivity.kt or a separate utility file
fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
class MainActivity : ComponentActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var firestore: FirebaseFirestore

    var isLoggedIn by mutableStateOf(false)
    var currentLocation: Location? = null
    var userRole by mutableStateOf<String?>(null)




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Lifecycle", "App Started")

        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fetchCurrentLocation()

        setContent {
            val firestoreRepository = remember { FirestoreRepository() }
            val currentUser = FirebaseAuth.getInstance().currentUser
            val isLoggedIn = remember { mutableStateOf(false) }

            FoodRescueTheme {
                val navController = rememberNavController()
                val startDestination = "auth_screen"
                Log.d("Navigation", "Start Destination: $startDestination")
                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    // **Authentication Screen**
                    composable("auth_screen") {
                        Log.d("Navigation", "Navigating to Auth Screen")
                        LoginAndRegisterScreen(
                            onLoginClicked = { email, password ->
                                login(email, password) {
                                    isLoggedIn.value = true
                                    navController.navigate("role_screen") // Correctly navigate using string
                                }
                            },
                            onRegisterClicked = { email, password, name, phone ->
                                register(email, password, name, phone) // Call register function correctly
                            }
                        )

                    }
                    composable("role_screen") {
                        navigateBasedOnRole(navController)
                    }

                    // **Food List Screen**
                    composable("food_list") {
                        val foodFeedViewModel: FoodFeedViewModel = viewModel()
                        FoodListScreen(
                            viewModel = foodFeedViewModel,
                            onFoodClick = { foodPost ->
                                navController.navigate("food_details_screen/${foodPost.donorId}")
                            }
                        )
                    }

                    // **Food Details Screen**
                    composable("food_details_screen/{foodId}") { backStackEntry ->
                        val foodId = backStackEntry.arguments?.getString("foodId").orEmpty()
                        Log.d("Navigation", "Navigating to Food Details Screen with foodId: $foodId")
                        if (foodId.isNotEmpty()) {
                            val foodFeedViewModel: FoodFeedViewModel = viewModel()
                            val foodPost = foodFeedViewModel.foodPosts.value?.find { it.donorId == foodId }

                            foodPost?.let {
                                FoodDetailsScreen(
                                    foodPost = it,
                                    onAcceptClick = {
                                        foodFeedViewModel.acceptFood(it)
                                        navController.popBackStack()
                                    }
                                )
                            } ?: run {
                                Log.e("NavigationError", "Food post not found for foodId: $foodId")
                            }
                        } else {
                            Log.e("NavigationError", "FoodId is null or empty")
                        }
                    }

                    // **Profile Screen**
                    composable("profile_screen") {
                        ProfileScreen(
                            userId = currentUser?.uid.orEmpty(),
                            navController = navController,
                            onLogout = {
                                isLoggedIn.value = false
                                userRole = null
                            }
                        )
                    }

                    // **Chat Screen**
                    composable("chat_screen/{donorId}") { backStackEntry ->
                        val donorId = backStackEntry.arguments?.getString("donorId")
                        val senderId = currentUser?.uid.orEmpty()
                        val receiverId = donorId.orEmpty()

                        ChatScreen(senderId = senderId, receiverId = receiverId)
                    }

                    // **Post Food Screen**
                    composable("post_food_screen") {
                        val context = LocalContext.current
                        PostFoodScreen(
                            navController = navController, // Pass the NavController here
                            onPostFood = { foodName, servings, freshness, location, duration ->
                                lifecycleScope.launch {
                                    firestoreRepository.addFoodPost(
                                        foodName = foodName,
                                        servings = servings,
                                        freshness = freshness,
                                        location = location,
                                        duration = duration,
                                        onSuccess = {
                                            Toast.makeText(context, "Food post added!", Toast.LENGTH_SHORT).show()
                                        },
                                        onFailure = { errorMessage ->
                                            Toast.makeText(context, "Failed: $errorMessage", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            },
                            onLogout = {
                                FirebaseAuth.getInstance().signOut()
                                Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                                navController.navigate("auth_screen") {
                                    popUpTo("donor_home_screen") { inclusive = true }
                                }
                            }
                        )
                    }


                    // **Donor Post Food Screen**
                    composable("donor_post_food_screen") {
                        val donorViewModel: DonorViewModel = viewModel(factory = DonorViewModelFactory())
                        DonorPostFoodScreen(donorViewModel = donorViewModel)
                    }

                    // **Recipient Profile Screen**
                    composable("recipient_profile_screen/{userId}") { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId").orEmpty()
                        val foodPosts = remember { mutableStateOf<List<FoodPost>>(emptyList()) }

                        val foodFeedViewModel: FoodFeedViewModel = viewModel()

                        LaunchedEffect(userId) {
                            firestoreRepository.getFoodPostsForUser(
                                onSuccess = { posts -> foodPosts.value = posts },
                                onFailure = { exception ->
                                    Log.e("FirestoreError", exception.message.orEmpty())
                                }
                            )
                        }

                        RecipientProfileScreen(
                            userId = userId,
                            foodPosts = foodPosts.value,
                            onChatClicked = { foodPost ->
                                navController.navigate("chat_screen/${foodPost.donorId}")
                            },
                            onProfileClicked = { navController.navigate("profile_screen") },
                            onAcceptFood = { foodPost ->
                                foodFeedViewModel.acceptFood(foodPost)
                            }
                        )
                    }
                }
            }
        }

        var locationFetched = false
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                if (!locationFetched) {
                    fetchCurrentLocation()
                    locationFetched = true
                    Toast.makeText(this@MainActivity, "Location fetched!", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    @Composable
    private fun navigateBasedOnRole(navController: NavHostController) {
        val foodFeedViewModel: FoodFeedViewModel = viewModel() // Obtain the ViewModel instance

        if (userRole == null) {
            RoleSelectionScreen(
                onDonorSelected = {
                    userRole = "donor"
                    navController.navigate("post_food_screen")
                },
                onRecipientSelected = {
                    userRole = "recipient"
                    navController.navigate("food_list")
                }
            )
        } else {
            if (userRole == "donor") {
                // Donor logic (PostFoodScreen)
                PostFoodScreen(navController = navController,
                    onPostFood = { foodName, servings, freshness, location, duration ->
                        addFoodPost(
                            foodName = foodName,
                            servings = servings,
                            freshness = freshness,
                            location = location,
                            duration = duration
                        )
                    },
                    onLogout = { logout() },
                )
            } else {
                // Recipient logic
                var foodPosts by remember { mutableStateOf(emptyList<FoodPost>()) }
                val context = LocalContext.current // Extract context inside the composable


                LaunchedEffect(Unit) {
                    fetchFoodPosts { fetchedPosts ->
                        foodPosts = fetchedPosts.map { foodPost ->
                            FoodPost(
                                donorId = foodPost.donorId,
                                foodName = foodPost.foodName,
                                freshness = foodPost.freshness,
                                location = foodPost.location,
                                servings = foodPost.servings.toString() // Convert to String if required
                            )
                        }
                    }
                }

                RecipientFoodFeedScreen(
                    foodFeedViewModel = foodFeedViewModel, // Pass the ViewModel
                    // Pass the food posts list
                    onProfileClicked = { navController.navigate("profile_screen") },
                    onChatClicked = { foodPost ->
                        navController.navigate("chat_screen/${foodPost.donorId}")
                    },
                    onFoodPostClicked = { foodPost ->
                        showToast("Food post clicked: ${foodPost.foodName}")
                        handleFoodAcceptance(foodPost, this)
                    },
                    onCopyLocation = { location ->
                        copyToClipboard(context, location)
                    } // Provide the onCopyLocation callback
                )

            }
        }
    }
    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Location", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Location copied to clipboard!", Toast.LENGTH_SHORT).show()
    }




    private fun fetchFoodPosts(onResult: (List<FoodPost>) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        firestore.collection("foodPosts")
            .whereEqualTo("status", "available") // Only fetch posts with "available" status
            .whereEqualTo("donorId", currentUser.uid) // Fetch posts created by the current donor
            .get()
            .addOnSuccessListener { querySnapshot ->
                val posts = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(FoodPost::class.java)
                }
                onResult(posts)
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error fetching food posts: ${exception.message}")
                onResult(emptyList()) // Return empty list on failure
            }
    }



    private fun fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = location
                    Log.d("Location", "Location fetched: ${location.latitude}, ${location.longitude}")

                } else {
                    Toast.makeText(this, "Unable to retrieve location", Toast.LENGTH_SHORT).show()
                    Log.e("Location", "Location not available")
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Location unavailable. Please enable GPS.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and Password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    // Or get from database if user was previously a donor/recipient
                    onSuccess()
                } else {
                    Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun register(email: String, password: String, name: String, phone: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and Password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Successfully registered the user
                    val userId = mAuth.currentUser?.uid
                    val userMap = hashMapOf(
                        "name" to name,
                        "phone" to phone,
                        "email" to email
                    )

                    // Save user data to Firestore
                    FirebaseFirestore.getInstance().collection("users")
                        .document(userId ?: "")
                        .set(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                            // You can navigate the user to the next screen or log them in
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error saving user data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // Registration failed
                    Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun logout() {
        mAuth.signOut()
        isLoggedIn = false
        userRole = null
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()

    }

    private fun addFoodPost(
        foodName: String,
        servings: String,
        freshness: String,
        location: GeoPoint?,
        duration: Int

    ) {
        if (location == null) {
            Toast.makeText(this, "Location unavailable. Please enable GPS.", Toast.LENGTH_LONG).show()
            return
        }

        val foodPost = FoodPost(
            foodName = foodName,
            servings = servings,
            freshness = freshness,
            location = location,
            duration = duration,
            donorId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
        )

        firestore.collection("foodPosts")
            .add(foodPost)
            .addOnSuccessListener {
                Toast.makeText(this, "Food post added successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add food post", Toast.LENGTH_SHORT).show()
            }
    }

    fun handleFoodAcceptance(foodPost: FoodPost, context: Context) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val foodPostRef = firestore.collection("foodPosts").document(foodPost.donorId)

        Log.d("FoodAcceptance", "Attempting to update food post with donorId: ${foodPost.donorId}, User ID: $userId")

        foodPostRef.update(
            mapOf(
                "status" to "accepted",
                "recipientId" to userId
            )
        )
            .addOnSuccessListener {
                Toast.makeText(context, "You have accepted ${foodPost.foodName}!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Log.e("FoodAcceptanceError", "Failed to accept food: ${exception.message}")
                Toast.makeText(context, "Failed to accept food: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


}
