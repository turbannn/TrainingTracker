package com.example.trainingtracker

/**
 * Enum for muscle categories
 */
enum class MuscleCategory(val displayName: String) {
    CHEST("Chest"),
    SHOULDERS("Shoulders"),
    BICEPS("Biceps"),
    TRICEPS("Triceps"),
    UPPER_BACK("Upper Back"),
    LATS("Lats"),
    CALVES("Calves"),
    HAMSTRINGS("Hamstrings"),
    QUADS("Quads");

    companion object {
        fun fromDisplayName(name: String): MuscleCategory? {
            return values().find { it.displayName == name }
        }
    }
}
