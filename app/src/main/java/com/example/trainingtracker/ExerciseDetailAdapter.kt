package com.example.trainingtracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.trainingtracker.databinding.ItemExerciseDetailBinding

/**
 * Adapter for displaying exercises in training detail screen (read-only)
 */
class ExerciseDetailAdapter : ListAdapter<Exercise, ExerciseDetailAdapter.ExerciseViewHolder>(ExerciseDetailDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val binding = ItemExerciseDetailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExerciseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    class ExerciseViewHolder(
        private val binding: ItemExerciseDetailBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(exercise: Exercise, position: Int) {
            with(binding) {
                // Set exercise name with number
                textviewExerciseName.text = "$position. ${exercise.name}"
                
                // Set exercise type and details
                val typeAndDetails = if (exercise.type.isNotEmpty()) {
                    "${exercise.type} | ${exercise.getFormattedDetails()}"
                } else {
                    exercise.getFormattedDetails()
                }
                textviewExerciseDetails.text = typeAndDetails
            }
        }
    }

    private class ExerciseDetailDiffCallback : DiffUtil.ItemCallback<Exercise>() {
        override fun areItemsTheSame(oldItem: Exercise, newItem: Exercise): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Exercise, newItem: Exercise): Boolean {
            return oldItem == newItem
        }
    }
}
