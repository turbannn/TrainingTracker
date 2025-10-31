package com.example.trainingtracker

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * User data class
 * Represents a user in the system
 */
data class User(
    @DocumentId
    val id: String = "",
    val email: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val password: String = "" // Note: In production, never store passwords in Firestore
)
