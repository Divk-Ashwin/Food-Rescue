package com.example.food_rescue

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.food_rescue.viewModel.FoodPost
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import android.util.Log

class DonorViewModel() : ViewModel() {
    // Mutable LiveData for tracking errors
    private val _errorMessage = MutableLiveData<String?>()


    fun addFoodPost(
        foodName: String,
        servings: String,
        freshness: String,
        location: GeoPoint?,
        duration: Int, // Pass duration here
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentTime = Timestamp.now()
        val expirationTime = Timestamp(currentTime.seconds + 3 * 60 * 60, 0) // Adding 3 hours to the current time
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // Handle case where the user is not logged in
            Log.e("AuthError", "User not logged in")
            return
        }
        val donorId = user.uid
        val foodPost = FoodPost(
            foodName = foodName,
            servings = servings,
            freshness = freshness,
            expirationTime = expirationTime,
            location = location,
            donorId = donorId,
            duration = duration // Use the new duration field
        )

        FirebaseFirestore.getInstance().collection("foodPosts")
            .add(foodPost)
            .addOnSuccessListener { documentReference ->
                val generatedPostId = documentReference.id
                onSuccess(generatedPostId)
            }
            .addOnFailureListener { exception ->
                _errorMessage.value = exception.message ?: "Unknown error"
                onFailure(exception.message ?: "Unknown error")
            }
    }



}

class DonorViewModelFactory(
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DonorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DonorViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


