package com.example.trainingtracker

/**
 * Enum for exercise types
 */
enum class ExerciseType(val displayName: String, val isCardio: Boolean) {
    CHEST("Chest", false),
    SHOULDERS("Shoulders", false),
    BICEPS("Biceps", false),
    TRICEPS("Triceps", false),
    UPPER_BACK("Upper Back", false),
    LATS("Lats", false), //lower back
    CALVES("Calves", false), // ikra
    HAMSTRINGS("Hamstrings", false), //not quads
    QUADS("Quads", false), //quads
    ABS("Abs", false),  // stomach
    TREADMILL("Treadmill", true),  // run forest run
    STAIR_CLIMBER("Stair Climber", true),  // climb forest climb
    OTHER_CARDIO("Other Cardio", true),
    OTHER("Other", false);  // Default category for existing exercises

    companion object {
        fun fromDisplayName(name: String): ExerciseType? {
            return values().find { it.displayName == name }
        }
    }
}
