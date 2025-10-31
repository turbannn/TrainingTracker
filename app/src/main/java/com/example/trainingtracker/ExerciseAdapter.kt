package com.example.trainingtracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.trainingtracker.databinding.ItemExerciseBinding

/**
 * Data class to hold exercise with muscle category
 */
data class ExerciseWithCategory(
    val exercise: Exercise,
    val category: MuscleCategory
)

/**
 * Adapter for displaying exercises in add training screen
 */
class ExerciseAdapter(
    private val onDeleteClick: (ExerciseWithCategory) -> Unit
) : ListAdapter<ExerciseWithCategory, ExerciseAdapter.ExerciseViewHolder>(ExerciseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val binding = ItemExerciseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExerciseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ExerciseViewHolder(
        private val binding: ItemExerciseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(exerciseWithCategory: ExerciseWithCategory) {
            with(binding) {
                val exercise = exerciseWithCategory.exercise
                
                // Set exercise name
                textviewExerciseName.text = exercise.name
                
                // Set muscle category
                textviewMuscleCategory.text = exerciseWithCategory.category.displayName
                
                // Set exercise details
                textviewExerciseDetails.text = exercise.getFormattedDetails()
                
                // Set delete button click listener
                buttonDelete.setOnClickListener {
                    onDeleteClick(exerciseWithCategory)
                }
            }
        }
    }

    private class ExerciseDiffCallback : DiffUtil.ItemCallback<ExerciseWithCategory>() {
        override fun areItemsTheSame(oldItem: ExerciseWithCategory, newItem: ExerciseWithCategory): Boolean {
            return oldItem.exercise.name == newItem.exercise.name
        }

        override fun areContentsTheSame(oldItem: ExerciseWithCategory, newItem: ExerciseWithCategory): Boolean {
            return oldItem == newItem
        }
    }
}
