package com.example.trainingtracker

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.trainingtracker.databinding.FragmentUpdateTrainingBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Fragment for updating an existing training
 */
class UpdateTrainingFragment : Fragment() {

    private var _binding: FragmentUpdateTrainingBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var firestore: FirebaseFirestore
    private lateinit var exerciseAdapter: ExerciseAdapter
    private val exercises = mutableListOf<ExerciseWithCategory>()
    private var selectedDate: Date? = null
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    private var trainingId: String? = null
    private var currentTraining: Training? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateTrainingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()
        
        // Get training ID from arguments
        trainingId = arguments?.getString("trainingId")
        
        // Setup RecyclerView
        setupRecyclerView()
        
        // Setup date picker
        setupDatePicker()
        
        // Setup buttons
        setupButtons()
        
        // Load training data
        loadTraining()
    }

    private fun loadTraining() {
        if (trainingId == null) {
            Toast.makeText(requireContext(), "Training not found", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }
        
        // Show loading
        binding.progressbar.visibility = View.VISIBLE
        
        // Load training from Firestore
        firestore.collection("trainings")
            .document(trainingId!!)
            .get()
            .addOnSuccessListener { document ->
                binding.progressbar.visibility = View.GONE
                
                val training = document.toObject(Training::class.java)
                if (training != null) {
                    currentTraining = training
                    populateTrainingData(training)
                } else {
                    Toast.makeText(requireContext(), "Training not found", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
            .addOnFailureListener { e ->
                binding.progressbar.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    "Error loading training: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().navigateUp()
            }
    }

    private fun populateTrainingData(training: Training) {
        // Set training name
        binding.edittextTrainingName.setText(training.name)
        
        // Set training date
        selectedDate = training.trainingDate.toDate()
        binding.edittextTrainingDate.setText(dateFormat.format(selectedDate!!))
        
        // Populate exercises
        exercises.clear()
        // Map exercises with their types from the Exercise.type field
        training.exercises.forEach { exercise ->
            val exerciseType = ExerciseType.fromDisplayName(exercise.type) ?: ExerciseType.OTHER
            exercises.add(ExerciseWithCategory(exercise, exerciseType))
        }
        updateUI()
    }

    private fun setupRecyclerView() {
        exerciseAdapter = ExerciseAdapter { exerciseWithCategory ->
            removeExercise(exerciseWithCategory)
        }
        binding.recyclerviewExercises.adapter = exerciseAdapter
    }

    private fun setupDatePicker() {
        // Open date picker on click
        binding.edittextTrainingDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        if (selectedDate != null) {
            calendar.time = selectedDate!!
        }
        
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = calendar.time
                binding.edittextTrainingDate.setText(dateFormat.format(selectedDate!!))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        // Allow past dates for updates
        // No minDate restriction
        
        datePickerDialog.show()
    }

    private fun setupButtons() {
        // Add Exercise button
        binding.buttonAddExercise.setOnClickListener {
            showMuscleCategoryDialog()
        }
        
        // Save Training button
        binding.buttonSaveTraining.setOnClickListener {
            updateTraining()
        }
    }

    private fun showMuscleCategoryDialog() {
        val categories = ExerciseType.values().map { it.displayName }.toTypedArray()
        
        AlertDialog.Builder(requireContext())
            .setTitle("Select Exercise Type")
            .setItems(categories) { _, which ->
                val selectedCategory = ExerciseType.values()[which]
                showAddExerciseDialog(selectedCategory)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddExerciseDialog(exerciseType: ExerciseType) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_exercise, null)
        val editTextName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edittext_exercise_name)
        val textInputSets = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.textinput_sets)
        val editTextSets = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edittext_sets)
        val textInputReps = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.textinput_reps)
        val editTextReps = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edittext_reps)
        val textInputDuration = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.textinput_duration)
        val editTextDuration = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edittext_duration)
        
        // Show/hide fields based on exercise type
        if (exerciseType.isCardio) {
            // For cardio: show only Name and Duration
            textInputSets.visibility = android.view.View.GONE
            textInputReps.visibility = android.view.View.GONE
            textInputDuration.visibility = android.view.View.VISIBLE
        } else {
            // For strength: show only Name, Sets, and Reps
            textInputSets.visibility = android.view.View.VISIBLE
            textInputReps.visibility = android.view.View.VISIBLE
            textInputDuration.visibility = android.view.View.GONE
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Add ${exerciseType.displayName} Exercise")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = editTextName.text.toString().trim()
                
                // Validate name
                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter exercise name", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                if (exerciseType.isCardio) {
                    // Cardio exercise - require duration
                    val durationText = editTextDuration.text.toString().trim()
                    
                    if (durationText.isEmpty()) {
                        Toast.makeText(requireContext(), "Please enter duration for cardio exercise", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    
                    val duration = durationText.toIntOrNull()
                    if (duration == null || duration <= 0) {
                        Toast.makeText(requireContext(), "Please enter valid duration (in seconds)", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    
                    val exercise = Exercise(
                        name = name,
                        type = exerciseType.displayName,
                        duration = duration,
                        sets = null,
                        repetitions = null
                    )
                    
                    addExercise(ExerciseWithCategory(exercise, exerciseType))
                } else {
                    // Strength exercise - require sets and reps
                    val setsText = editTextSets.text.toString().trim()
                    val repsText = editTextReps.text.toString().trim()
                    
                    if (setsText.isEmpty() || repsText.isEmpty()) {
                        Toast.makeText(requireContext(), "Please enter sets and repetitions", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    
                    val sets = setsText.toIntOrNull()
                    if (sets == null || sets <= 0) {
                        Toast.makeText(requireContext(), "Please enter valid number of sets", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    
                    val repetitions = repsText.split(",").mapNotNull { it.trim().toIntOrNull() }
                    if (repetitions.isEmpty()) {
                        Toast.makeText(requireContext(), "Please enter valid repetitions (comma-separated)", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    
                    // Validate sets and reps match
                    if (sets != repetitions.size) {
                        Toast.makeText(
                            requireContext(),
                            "Number of repetitions must match number of sets",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setPositiveButton
                    }
                    
                    val exercise = Exercise(
                        name = name,
                        type = exerciseType.displayName,
                        duration = null,
                        sets = sets,
                        repetitions = repetitions
                    )
                    
                    addExercise(ExerciseWithCategory(exercise, exerciseType))
                }
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

    private fun updateTraining() {
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
        
        if (trainingId == null) {
            Toast.makeText(requireContext(), "Training ID not found", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Use selected date
        val trainingTimestamp = if (selectedDate != null) {
            Timestamp(selectedDate!!)
        } else {
            Timestamp.now()
        }
        
        // Create updated training data
        val updates = hashMapOf<String, Any>(
            "name" to trainingName,
            "trainingDate" to trainingTimestamp,
            "exercises" to exercises.map { it.exercise }
        )
        
        // Show loading
        binding.progressbar.visibility = View.VISIBLE
        
        // Update in Firestore
        firestore.collection("trainings")
            .document(trainingId!!)
            .update(updates)
            .addOnSuccessListener {
                binding.progressbar.visibility = View.GONE
                Toast.makeText(requireContext(), "Training updated successfully!", Toast.LENGTH_SHORT).show()
                // Navigate back
                findNavController().navigateUp()
            }
            .addOnFailureListener { e ->
                binding.progressbar.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    "Error updating training: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
