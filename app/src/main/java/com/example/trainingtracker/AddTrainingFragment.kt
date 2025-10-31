package com.example.trainingtracker

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.trainingtracker.databinding.FragmentAddTrainingBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Fragment for adding a new training
 */
class AddTrainingFragment : Fragment() {

    private var _binding: FragmentAddTrainingBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var firestore: FirebaseFirestore
    private lateinit var exerciseAdapter: ExerciseAdapter
    private val exercises = mutableListOf<ExerciseWithCategory>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTrainingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()
        
        // Setup RecyclerView
        setupRecyclerView()
        
        // Setup buttons
        setupButtons()
        
        // Update UI
        updateUI()
    }

    private fun setupRecyclerView() {
        exerciseAdapter = ExerciseAdapter { exerciseWithCategory ->
            removeExercise(exerciseWithCategory)
        }
        binding.recyclerviewExercises.adapter = exerciseAdapter
    }

    private fun setupButtons() {
        // Add Exercise button
        binding.buttonAddExercise.setOnClickListener {
            showMuscleCategoryDialog()
        }
        
        // Save Training button
        binding.buttonSaveTraining.setOnClickListener {
            saveTraining()
        }
    }

    private fun showMuscleCategoryDialog() {
        val categories = MuscleCategory.values().map { it.displayName }.toTypedArray()
        
        AlertDialog.Builder(requireContext())
            .setTitle("Select Muscle Category")
            .setItems(categories) { _, which ->
                val selectedCategory = MuscleCategory.values()[which]
                showAddExerciseDialog(selectedCategory)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddExerciseDialog(category: MuscleCategory) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_exercise, null)
        val editTextName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edittext_exercise_name)
        val editTextSets = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edittext_sets)
        val editTextReps = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edittext_reps)
        val editTextDuration = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edittext_duration)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Add ${category.displayName} Exercise")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = editTextName.text.toString().trim()
                val setsText = editTextSets.text.toString().trim()
                val repsText = editTextReps.text.toString().trim()
                val durationText = editTextDuration.text.toString().trim()
                
                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter exercise name", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                val sets = setsText.toIntOrNull()
                val repetitions = if (repsText.isNotEmpty()) {
                    repsText.split(",").mapNotNull { it.trim().toIntOrNull() }
                } else null
                val duration = durationText.toIntOrNull()
                
                // Validate sets and reps match
                if (sets != null && repetitions != null && sets != repetitions.size) {
                    Toast.makeText(
                        requireContext(),
                        "Number of reps must match number of sets",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }
                
                val exercise = Exercise(
                    name = name,
                    sets = sets,
                    repetitions = repetitions,
                    duration = duration
                )
                
                addExercise(ExerciseWithCategory(exercise, category))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addExercise(exerciseWithCategory: ExerciseWithCategory) {
        exercises.add(exerciseWithCategory)
        updateUI()
    }

    private fun removeExercise(exerciseWithCategory: ExerciseWithCategory) {
        exercises.remove(exerciseWithCategory)
        updateUI()
    }

    private fun updateUI() {
        exerciseAdapter.submitList(exercises.toList())
        
        // Show/hide empty state
        if (exercises.isEmpty()) {
            binding.textviewEmptyExercises.visibility = View.VISIBLE
            binding.recyclerviewExercises.visibility = View.GONE
        } else {
            binding.textviewEmptyExercises.visibility = View.GONE
            binding.recyclerviewExercises.visibility = View.VISIBLE
        }
    }

    private fun saveTraining() {
        val trainingName = binding.edittextTrainingName.text.toString().trim()
        
        // Validate training name
        if (trainingName.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter training name", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Validate exercises
        if (exercises.isEmpty()) {
            Toast.makeText(requireContext(), "Please add at least one exercise", Toast.LENGTH_SHORT).show()
            return
        }
        
        val userId = UserSession.getUserId()
        if (userId == null) {
            Toast.makeText(requireContext(), "Please sign in to save training", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Create training object
        val training = Training(
            isComplete = false,
            trainingDate = Timestamp.now(),
            exercises = exercises.map { it.exercise },
            userId = userId
        )
        
        // Save to Firestore
        firestore.collection("trainings")
            .add(training)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Training saved successfully!", Toast.LENGTH_SHORT).show()
                // Navigate back
                findNavController().navigateUp()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Error saving training: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
