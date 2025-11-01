package com.example.trainingtracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.trainingtracker.databinding.FragmentTrainingDetailBinding
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Fragment for displaying training details
 */
class TrainingDetailFragment : Fragment() {

    private var _binding: FragmentTrainingDetailBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var firestore: FirebaseFirestore
    private lateinit var exerciseAdapter: ExerciseDetailAdapter
    private var trainingId: String? = null
    private var currentTraining: Training? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrainingDetailBinding.inflate(inflater, container, false)
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
        
        // Setup buttons
        setupButtons()
        
        // Load training
        loadTraining()
    }

    private fun setupRecyclerView() {
        exerciseAdapter = ExerciseDetailAdapter()
        binding.recyclerviewExercises.adapter = exerciseAdapter
    }

    private fun setupButtons() {
        // Toggle complete button
        binding.buttonToggleComplete.setOnClickListener {
            toggleTrainingComplete()
        }
        
        // Update training button
        binding.buttonUpdateTraining.setOnClickListener {
            navigateToUpdateTraining()
        }
    }

    private fun navigateToUpdateTraining() {
        val bundle = Bundle().apply {
            putString("trainingId", trainingId)
        }
        findNavController().navigate(R.id.UpdateTrainingFragment, bundle)
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
                    displayTraining(training)
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

    private fun displayTraining(training: Training) {
        with(binding) {
            // Display training name
            textviewTrainingName.text = training.name
            
            // Display date
            textviewDate.text = training.getFormattedDate()
            
            // Display status
            textviewStatus.text = training.getStatusText()
            
            // Display duration
            textviewDuration.text = "Total: ${training.getFormattedDuration()}"
            
            // Display exercise count
            textviewExerciseCount.text = "(${training.getExerciseCount()})"
            
            // Display exercises
            exerciseAdapter.submitList(training.exercises)
            
            // Update button text
            buttonToggleComplete.text = if (training.complete) {
                "Mark as Incomplete"
            } else {
                "Mark as Complete"
            }
        }
    }

    private fun toggleTrainingComplete() {
        val training = currentTraining ?: return
        val newStatus = !training.complete
        
        if (trainingId == null) return
        
        // Update in Firestore
        firestore.collection("trainings")
            .document(trainingId!!)
            .update("complete", newStatus)
            .addOnSuccessListener {
                val message = if (newStatus) "Training marked as complete!" else "Training marked as incomplete!"
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                
                // Reload training from Firestore to get updated data
                loadTraining()
            }
            .addOnFailureListener { e ->
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
