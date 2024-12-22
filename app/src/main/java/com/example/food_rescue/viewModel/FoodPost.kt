package com.example.food_rescue.viewModel


import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class FoodPost(
    val foodName: String = "",
    val servings: String = "",
    val freshness: String = "",
    val location: GeoPoint? = null,
    val donorName: String = "",
    val donorPhone: String = "",
    val donorAddress: String = "",
    val recipientId: String? = null,
    val donorId: String = "",
    val expirationTime: Timestamp? = null,
    val duration: Int = 90,
    val status: String = "available",
    var id: String = "" // New field for Firestore document ID
)


