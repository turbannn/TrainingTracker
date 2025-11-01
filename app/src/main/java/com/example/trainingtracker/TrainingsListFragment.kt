package com.example.trainingtracker

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.trainingtracker.databinding.FragmentTrainingsListBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Trainings List Fragment - displays list of user's trainings
 */
class TrainingsListFragment : Fragment() {

    companion object {
        private const val TAG = "TrainingsListFragment"
    }

    private var _binding: FragmentTrainingsListBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var firestore: FirebaseFirestore
    private lateinit var trainingAdapter: TrainingAdapter
    private var trainingsListener: com.google.firebase.firestore.ListenerRegistration? = null
    
    private val calendar = Calendar.getInstance()
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

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
        
        // Setup month navigation
        setupMonthNavigation()
        
        // Setup FAB
        setupFab()
        
        // Load trainings for current month
        updateMonthDisplay()
        loadTrainings()
    }

    private fun setupMonthNavigation() {
        Log.d(TAG, "Setting up month navigation")
        
        // Previous month button
        binding.buttonPrevMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            Log.d(TAG, "Previous month clicked, now showing: ${monthYearFormat.format(calendar.time)}")
            updateMonthDisplay()
            loadTrainings()
        }
        
        // Next month button
        binding.buttonNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            Log.d(TAG, "Next month clicked, now showing: ${monthYearFormat.format(calendar.time)}")
            updateMonthDisplay()
            loadTrainings()
        }
    }

    private fun updateMonthDisplay() {
        val displayText = monthYearFormat.format(calendar.time)
        binding.textviewCurrentMonth.text = displayText
        Log.d(TAG, "Month display updated to: $displayText")
    }

    private fun setupRecyclerView() {
        trainingAdapter = TrainingAdapter(
            onTrainingClick = { training ->
                onTrainingClicked(training)
            },
            onDeleteClick = { training ->
                onDeleteTraining(training)
            }
        )
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
            Log.e(TAG, "User ID is null, cannot load trainings")
            Toast.makeText(
                requireContext(),
                "Please sign in to view trainings",
                Toast.LENGTH_SHORT
            ).show()
            showEmptyState()
            return
        }
        
        Log.d(TAG, "Loading trainings for user: $userId")
        
        // Show loading
        showLoading()
        
        // Remove previous listener if exists
        trainingsListener?.remove()
        
        // Load ALL trainings and filter in code
        trainingsListener = firestore.collection("trainings")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                // Check if view is still alive
                if (_binding == null) {
                    Log.w(TAG, "Binding is null, skipping update")
                    return@addSnapshotListener
                }
                
                // Hide loading
                hideLoading()
                
                if (error != null) {
                    Log.e(TAG, "Error loading trainings", error)
                    Toast.makeText(
                        requireContext(),
                        "Error loading trainings: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    showEmptyState()
                    return@addSnapshotListener
                }
                
                Log.d(TAG, "Received ${snapshot?.documents?.size ?: 0} trainings from Firestore")
                
                // Get selected month and year
                val selectedYear = calendar.get(Calendar.YEAR)
                val selectedMonth = calendar.get(Calendar.MONTH)
                
                Log.d(TAG, "Filtering for year: $selectedYear, month: $selectedMonth")
                
                // Filter trainings by selected month
                val trainings = snapshot?.documents?.mapNotNull { document ->
                    try {
                        document.toObject(Training::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing training document: ${document.id}", e)
                        null
                    }
                }?.filter { training ->
                    training.trainingDate?.let { timestamp ->
                        val trainingCal = Calendar.getInstance().apply {
                            time = timestamp.toDate()
                        }
                        val trainingYear = trainingCal.get(Calendar.YEAR)
                        val trainingMonth = trainingCal.get(Calendar.MONTH)
                        
                        val matches = trainingYear == selectedYear && trainingMonth == selectedMonth
                        
                        Log.d(TAG, "Training '${training.name}' date: ${timestamp.toDate()}, " +
                                "year: $trainingYear, month: $trainingMonth, matches: $matches")
                        
                        matches
                    } ?: false
                }?.sortedByDescending { it.trainingDate } ?: emptyList()
                
                Log.d(TAG, "Filtered to ${trainings.size} trainings for selected month")
                
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

    private fun onDeleteTraining(training: Training) {
        // Show confirmation dialog
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Training")
            .setMessage("Are you sure to delete?")
            .setPositiveButton("Delete") { _, _ ->
                deleteTrainingFromFirestore(training)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteTrainingFromFirestore(training: Training) {
        training.id?.let { trainingId ->
            firestore.collection("trainings")
                .document(trainingId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        "Training deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { error ->
                    Toast.makeText(
                        requireContext(),
                        "Error deleting training: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
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
