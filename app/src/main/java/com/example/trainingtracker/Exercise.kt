package com.example.trainingtracker

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Exercise data class
 * Represents a single exercise within a training session
 */
data class Exercise(
    val name: String = "",
    val duration: Int? = null,           // Duration in seconds (nullable)
    val sets: Int? = null,               // Number of sets (nullable)
    val repetitions: List<Int>? = null   // Array of repetitions per set (nullable)
) {
    /**
     * Validates that repetitions array size matches sets count
     */
    fun isValid(): Boolean {
        if (sets != null && repetitions != null) {
            return repetitions.size == sets
        }
        return true
    }
    
    /**
     * Returns a formatted string representation of the exercise
     */
    fun getFormattedDetails(): String {
        val details = mutableListOf<String>()
        
        duration?.let { details.add("${it}s") }
        sets?.let { details.add("$it sets") }
        repetitions?.let { 
            if (it.isNotEmpty()) {
                details.add("reps: ${it.joinToString(", ")}")
            }
        }
        
        return if (details.isNotEmpty()) {
            details.joinToString(" | ")
        } else {
            "No details"
        }
    }
}
