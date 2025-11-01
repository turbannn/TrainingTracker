package com.example.trainingtracker

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Training data class
 * Represents a complete training session
 */
data class Training(
    @DocumentId
    val id: String = "",
    val name: String = "",                      // Name of the training
    val complete: Boolean = false,              // Completion status
    val trainingDate: Timestamp = Timestamp.now(),
    val exercises: List<Exercise> = emptyList(),
    val userId: String = ""  // To associate training with a user
) {
    /**
     * Returns formatted date string
     */
    fun getFormattedDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(trainingDate.toDate())
    }
    
    /**
     * Returns formatted date only (without time)
     */
    fun getFormattedDateOnly(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(trainingDate.toDate())
    }
    
    /**
     * Returns total number of exercises
     */
    fun getExerciseCount(): Int = exercises.size
    
    /**
     * Returns completion status as string
     */
    fun getStatusText(): String = if (complete) "Completed" else "In Progress"
    
    /**
     * Calculates total duration of all exercises (if available)
     */
    fun getTotalDuration(): Int? {
        val durations = exercises.mapNotNull { it.duration }
        return if (durations.isNotEmpty()) durations.sum() else null
    }
    
    /**
     * Returns formatted total duration string
     */
    fun getFormattedDuration(): String {
        val totalSeconds = getTotalDuration()
        return if (totalSeconds != null) {
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            if (minutes > 0) {
                "${minutes}m ${seconds}s"
            } else {
                "${seconds}s"
            }
        } else {
            "N/A"
        }
    }
    
    /**
     * Creates a copy with updated completion status
     */
    fun markAsComplete(): Training {
        return this.copy(complete = true)
    }
    
    /**
     * Creates a copy with updated completion status
     */
    fun markAsIncomplete(): Training {
        return this.copy(complete = false)
    }
}
