package com.example.food_rescue.repository

import android.util.Log
import com.example.food_rescue.viewModel.FoodPost
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class FirestoreRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val foodPostsCollection = firestore.collection("foodPosts")
    private val usersCollection = firestore.collection("users")
    private val currentUser = FirebaseAuth.getInstance().currentUser

    // Function to add a food post to Firestore
    fun addFoodPost(
        foodName: String,
        servings: String,
        freshness: String,
        location: GeoPoint?,
        duration: Int,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (currentUser == null) {
            onFailure("User not logged in")
            return
        }

        val userId = currentUser.uid
        Log.d("FirestoreDebug", "Fetching donor details for userId: $userId")

        // Fetch donor details from the users collection
        usersCollection.document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Extract donor details
                    val donorName = documentSnapshot.getString("name") ?: "Unknown"
                    val donorPhone = documentSnapshot.getString("phone") ?: "Unknown"
                    val donorAddress = documentSnapshot.getString("address") ?: "Unknown"
                    val currentTime = com.google.firebase.Timestamp.now()
                    val expirationTime = com.google.firebase.Timestamp(currentTime.seconds + duration * 60 * 60, 0)

                    // Create FoodPost object with donor details
                    val donorId = documentSnapshot.getString("donorId") ?: "Unknown"
                    val foodPost = FoodPost(
                        foodName = foodName,
                        servings = servings,
                        freshness = freshness,
                        location = location,
                        donorId = donorId,
                        donorName = donorName,
                        donorPhone = donorPhone,
                        donorAddress = donorAddress,
                        duration = duration, // Ensure your FoodPost class supports this field
                        expirationTime = expirationTime
                    )

                    // Add food post to Firestore
                    foodPostsCollection.add(foodPost)
                        .addOnSuccessListener { documentReference ->
                            onSuccess() // Call success callback
                        }
                        .addOnFailureListener { exception ->
                            Log.e("FirestoreError", "Failed to add food post: ${exception.message}")
                            onFailure("Failed to add food post: ${exception.message}") // Call failure callback
                        }
                } else {
                    Log.e("FirestoreError", "Donor details not found for userId: $userId")
                    onFailure("Donor details not found")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error fetching donor details: ${exception.message}")
                onFailure("Error fetching donor details: ${exception.message}")
            }
    }

    fun getFoodPostsForUser(
        onSuccess: (List<FoodPost>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        foodPostsCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val foodPosts = querySnapshot.documents.mapNotNull { it.toObject(FoodPost::class.java) }

                // Fetch donor details for each food post
                val updatedFoodPosts = mutableListOf<FoodPost>()
                val tasks = foodPosts.map { foodPost ->
                    usersCollection.document(foodPost.donorId).get()
                        .addOnSuccessListener { donorSnapshot ->
                            if (donorSnapshot.exists()) {
                                val donorName = donorSnapshot.getString("name") ?: "Unknown"
                                val donorPhone = donorSnapshot.getString("phone") ?: "Unknown"
                                val donorAddress = donorSnapshot.getString("address") ?: "Unknown"

                                // Update food post with donor details
                                updatedFoodPosts.add(
                                    foodPost.copy(
                                        donorName = donorName,
                                        donorPhone = donorPhone,
                                        donorAddress = donorAddress
                                    )
                                )
                            }
                        }
                }

                // Wait for all tasks to complete
                Tasks.whenAllComplete(tasks).addOnSuccessListener {
                    Log.d("FirestoreDebug", "Retrieved ${updatedFoodPosts.size} updated food posts")
                    onSuccess(updatedFoodPosts)
                }.addOnFailureListener { exception ->
                    Log.e("FirestoreError", "Error fetching donor details: ${exception.message}")
                    onFailure(exception)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error fetching food posts: ${exception.message}")
                onFailure(exception)
            }
    }
}
