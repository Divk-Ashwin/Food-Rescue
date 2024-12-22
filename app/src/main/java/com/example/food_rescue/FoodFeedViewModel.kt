package com.example.food_rescue

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.food_rescue.repository.FirestoreRepository
import com.example.food_rescue.viewModel.FoodPost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FoodFeedViewModel : ViewModel() {

    // LiveData for food posts
    private val _foodPosts = MutableLiveData<List<FoodPost>>()
    val foodPosts: LiveData<List<FoodPost>> get() = _foodPosts

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val firestoreRepository = FirestoreRepository()
    private val firestore = FirebaseFirestore.getInstance()

    init {
        // Fetch food posts from Firestore on initialization
        fetchFoodPosts()
    }

    fun acceptFood(foodPost: FoodPost) {
        val foodPostRef = firestore.collection("foodPosts").document(foodPost.donorId)
        foodPostRef.update(
            mapOf(
                "status" to "accepted",
                "recipientId" to FirebaseAuth.getInstance().currentUser?.uid
            )
        )
            .addOnSuccessListener {
                _errorMessage.value = "Successfully accepted ${foodPost.foodName}"
                // Refresh the food posts list
                fetchFoodPosts()
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Failed to accept food: ${e.message}"
            }
    }


    fun fetchFoodPosts() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            firestoreRepository.getFoodPostsForUser(
                onSuccess = { posts: List<FoodPost> ->
                    // Filter posts to only show available ones (not accepted)
                    val availablePosts = posts.filter { post -> post.status != "accepted" }
                    _foodPosts.value = availablePosts // Update LiveData with available posts
                },
                onFailure = { error: Exception ->
                    _errorMessage.value = error.message // Set error message if any
                }
            )
        } else {
            _errorMessage.value = "User not authenticated"
            Log.e("AuthError", "User ID is null")
        }
    }

}
