package com.example.trainingtracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.trainingtracker.databinding.FragmentTrainingsListBinding
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Trainings List Fragment - displays list of user's trainings
 */
class TrainingsListFragment : Fragment() {

    private var _binding: FragmentTrainingsListBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var firestore: FirebaseFirestore
    private lateinit var trainingAdapter: TrainingAdapter
    private var trainingsListener: com.google.firebase.firestore.ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrainingsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()
        
        // Setup RecyclerView
        setupRecyclerView()
        
        // Setup FAB
        setupFab()
        
        // Load trainings
        loadTrainings()
    }

    private fun setupRecyclerView() {
        trainingAdapter = TrainingAdapter { training ->
            onTrainingClicked(training)
        }
        binding.recyclerviewTrainings.adapter = trainingAdapter
    }

    private fun setupFab() {
        binding.fabAddTraining.setOnClickListener {
            // Navigate to add training screen
            findNavController().navigate(R.id.AddTrainingFragment)
        }
    }

    private fun loadTrainings() {
        val userId = UserSession.getUserId()
        
        if (userId == null) {
            Toast.makeText(
                requireContext(),
                "Please sign in to view trainings",
                Toast.LENGTH_SHORT
            ).show()
            showEmptyState()
            return
        }
        
        // Show loading
        showLoading()
        
        // Load trainings from Firestore
        trainingsListener = firestore.collection("trainings")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                // Check if view is still alive
                if (_binding == null) return@addSnapshotListener
                
                // Hide loading
                hideLoading()
                
                if (error != null) {
                    Toast.makeText(
                        requireContext(),
                        "Error loading trainings: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    showEmptyState()
                    return@addSnapshotListener
                }
                
                val trainings = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Training::class.java)
                }?.sortedByDescending { it.trainingDate } ?: emptyList()
                
                if (trainings.isEmpty()) {
                    showEmptyState()
                } else {
                    showTrainings(trainings)
                }
            }
    }

    private fun onTrainingClicked(training: Training) {
        // Navigate to training details
        val bundle = Bundle().apply {
            putString("trainingId", training.id)
        }
        findNavController().navigate(R.id.TrainingDetailFragment, bundle)
    }

    private fun showLoading() {
        _binding?.let {
            it.progressbar.visibility = View.VISIBLE
            it.recyclerviewTrainings.visibility = View.GONE
            it.layoutEmptyState.visibility = View.GONE
        }
    }

    private fun hideLoading() {
        _binding?.progressbar?.visibility = View.GONE
    }

    private fun showEmptyState() {
        _binding?.let {
            it.layoutEmptyState.visibility = View.VISIBLE
            it.recyclerviewTrainings.visibility = View.GONE
        }
    }

    private fun showTrainings(trainings: List<Training>) {
        _binding?.let {
            it.layoutEmptyState.visibility = View.GONE
            it.recyclerviewTrainings.visibility = View.VISIBLE
            trainingAdapter.submitList(trainings)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove Firestore listener
        trainingsListener?.remove()
        trainingsListener = null
        _binding = null
    }
}
