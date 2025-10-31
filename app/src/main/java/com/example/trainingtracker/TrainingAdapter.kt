package com.example.trainingtracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.trainingtracker.databinding.ItemTrainingBinding

/**
 * Adapter for displaying trainings in RecyclerView
 */
class TrainingAdapter(
    private val onTrainingClick: (Training) -> Unit
) : ListAdapter<Training, TrainingAdapter.TrainingViewHolder>(TrainingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrainingViewHolder {
        val binding = ItemTrainingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TrainingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrainingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TrainingViewHolder(
        private val binding: ItemTrainingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(training: Training) {
            with(binding) {
                // Set date
                textviewDate.text = training.getFormattedDate()
                
                // Set status
                textviewStatus.text = training.getStatusText()
                
                // Set exercise count
                val exerciseCount = training.getExerciseCount()
                textviewExercises.text = if (exerciseCount == 1) {
                    "$exerciseCount exercise"
                } else {
                    "$exerciseCount exercises"
                }
                
                // Set duration
                textviewDuration.text = training.getFormattedDuration()
                
                // Set status icon color based on completion
                val iconTint = if (training.isComplete) {
                    android.R.color.holo_green_dark
                } else {
                    android.R.color.holo_orange_dark
                }
                imageviewStatus.setColorFilter(
                    root.context.getColor(iconTint)
                )
                
                // Set click listener
                root.setOnClickListener {
                    onTrainingClick(training)
                }
            }
        }
    }

    private class TrainingDiffCallback : DiffUtil.ItemCallback<Training>() {
        override fun areItemsTheSame(oldItem: Training, newItem: Training): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Training, newItem: Training): Boolean {
            return oldItem == newItem
        }
    }
}
