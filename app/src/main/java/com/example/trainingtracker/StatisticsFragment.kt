package com.example.trainingtracker

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.trainingtracker.databinding.FragmentStatisticsBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Statistics Fragment - displays exercise progress charts
 */
class StatisticsFragment : Fragment() {

    companion object {
        private const val TAG = "StatisticsFragment"
    }

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var firestore: FirebaseFirestore
    private val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
    
    // Map to store unique exercise types
    private val exerciseTypes = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        firestore = FirebaseFirestore.getInstance()
        
        // Load all exercise types to populate the dropdown
        loadAllExerciseTypes()
    }

    private fun loadAllExerciseTypes() {
        val userId = UserSession.getUserId()
        
        if (userId == null) {
            Log.e(TAG, "User ID is null")
            Toast.makeText(requireContext(), "Please sign in", Toast.LENGTH_SHORT).show()
            return
        }
        
        Log.d(TAG, "Loading exercise types for user: $userId")
        
        // Use all available ExerciseType enum values
        exerciseTypes.clear()
        ExerciseType.values().forEach { exerciseType ->
            exerciseTypes.add(exerciseType.name)
        }
        
        Log.d(TAG, "Loaded ${exerciseTypes.size} exercise types from enum: $exerciseTypes")
        
        if (exerciseTypes.isEmpty()) {
            showEmptyState()
        } else {
            setupExerciseDropdown()
            hideEmptyState()
        }
    }

    private fun setupExerciseDropdown() {
        // Convert types to ExerciseType enum and get display names, then sort
        val exerciseList = exerciseTypes.mapNotNull { typeStr ->
            try {
                val exerciseType = ExerciseType.valueOf(typeStr)
                exerciseType.displayName to typeStr // Pair of display name and enum string
            } catch (e: Exception) {
                Log.w(TAG, "Unknown exercise type: $typeStr")
                null
            }
        }.sortedBy { it.first } // Sort by display name
        
        val displayNames = exerciseList.map { it.first }
        
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            displayNames
        )
        
        binding.spinnerExercise.setAdapter(adapter)
        
        binding.spinnerExercise.setOnItemClickListener { _, _, position, _ ->
            val selectedType = exerciseList[position].second // Get the enum string
            val selectedDisplayName = exerciseList[position].first
            Log.d(TAG, "Selected exercise type: $selectedType ($selectedDisplayName)")
            
            // Load statistics for selected type
            loadExerciseStatistics(selectedType, selectedDisplayName)
        }
    }

    private fun loadExerciseStatistics(exerciseType: String, displayName: String) {
        val userId = UserSession.getUserId() ?: return
        
        Log.d(TAG, "Loading statistics for exercise type: $exerciseType ($displayName)")
        showLoading()
        
        firestore.collection("trainings")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                hideLoading()
                
                Log.d(TAG, "Received ${snapshot.documents.size} trainings for statistics")
                
                // Data structure: Map<Date, Value>
                val dataPoints = mutableMapOf<Long, Float>()
                var isCardio = false
                
                var totalExercisesChecked = 0
                var matchingExercises = 0
                
                snapshot.documents.forEach { document ->
                    try {
                        val training = document.toObject(Training::class.java)
                        val trainingDate = training?.trainingDate?.toDate()?.time
                        
                        if (trainingDate == null) {
                            Log.w(TAG, "Training ${document.id} has no date")
                            return@forEach
                        }
                        
                        Log.d(TAG, "Processing training '${training.name}' with ${training.exercises.size} exercises")
                        
                        training.exercises.forEach { exercise ->
                            totalExercisesChecked++
                            
                            Log.d(TAG, "  Exercise: name='${exercise.name}', type='${exercise.type}', duration=${exercise.duration}, reps=${exercise.repetitions}")
                            
                            // Filter by type - compare with displayName since that's what's stored in Firebase
                            if (exercise.type == displayName) {
                                matchingExercises++
                                
                                // Determine if cardio by checking if duration is not null
                                val exerciseIsCardio = exercise.duration != null
                                isCardio = exerciseIsCardio // Set for chart display
                                
                                val value = if (exerciseIsCardio) {
                                    // For cardio: use duration in seconds
                                    val duration = exercise.duration?.toFloat() ?: 0f
                                    Log.d(TAG, "    MATCH! Cardio exercise (duration not null), duration: $duration seconds")
                                    duration
                                } else {
                                    // For non-cardio: sum all repetitions
                                    val reps = exercise.repetitions?.sum()?.toFloat() ?: 0f
                                    Log.d(TAG, "    MATCH! Strength exercise (duration is null), total reps: $reps")
                                    reps
                                }
                                
                                // Sum values if multiple exercises with same type on same date
                                val previousValue = dataPoints[trainingDate] ?: 0f
                                dataPoints[trainingDate] = previousValue + value
                                
                                Log.d(TAG, "    Added $value to date ${dateFormat.format(trainingDate)}, total now: ${dataPoints[trainingDate]}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing training: ${document.id}", e)
                    }
                }
                
                Log.d(TAG, "Summary: Checked $totalExercisesChecked exercises, found $matchingExercises matches")
                Log.d(TAG, "Collected ${dataPoints.size} data points for type '$exerciseType': $dataPoints")
                
                if (dataPoints.isEmpty()) {
                    // Show empty chart
                    displayEmptyChart(displayName, isCardio)
                } else {
                    displayChart(displayName, dataPoints, isCardio)
                }
            }
            .addOnFailureListener { e ->
                hideLoading()
                Log.e(TAG, "Error loading statistics", e)
                Toast.makeText(
                    requireContext(),
                    "Error loading statistics: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun displayChart(exerciseName: String, dataPoints: Map<Long, Float>, isCardio: Boolean) {
        binding.cardChart.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE
        
        // Update titles
        binding.textviewChartTitle.text = exerciseName
        binding.textviewChartSubtitle.text = if (isCardio) {
            "Duration over time (seconds)"
        } else {
            "Total repetitions over time"
        }
        
        // Sort data by date
        val sortedData = dataPoints.toList().sortedBy { it.first }
        
        // Create entries for chart
        val entries = sortedData.mapIndexed { index, (timestamp, value) ->
            Entry(index.toFloat(), value)
        }
        
        Log.d(TAG, "Creating chart with ${entries.size} entries")
        
        // Create dataset
        val dataSet = LineDataSet(entries, if (isCardio) "Duration (sec)" else "Repetitions").apply {
            color = Color.parseColor("#6200EE")
            setCircleColor(Color.parseColor("#6200EE"))
            circleRadius = 5f
            lineWidth = 2f
            valueTextSize = 10f
            setDrawValues(true)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        
        // Setup chart
        val lineData = LineData(dataSet)
        binding.chart.apply {
            data = lineData
            description.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(true)
            
            // X-axis (dates)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        return if (index >= 0 && index < sortedData.size) {
                            dateFormat.format(sortedData[index].first)
                        } else ""
                    }
                }
            }
            
            // Y-axis (values)
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }
            axisRight.isEnabled = false
            
            // Legend
            legend.isEnabled = true
            
            // Animate
            animateX(500)
            
            invalidate()
        }
    }

    private fun displayEmptyChart(exerciseName: String, isCardio: Boolean) {
        binding.cardChart.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE
        
        // Update titles
        binding.textviewChartTitle.text = exerciseName
        binding.textviewChartSubtitle.text = if (isCardio) {
            "Duration over time (seconds) - No data yet"
        } else {
            "Total repetitions over time - No data yet"
        }
        
        // Clear chart
        binding.chart.apply {
            clear()
            data = null
            invalidate()
        }
        
        Log.d(TAG, "Displayed empty chart for '$exerciseName'")
    }

    private fun showLoading() {
        binding.progressbar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressbar.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.cardChart.visibility = View.GONE
    }

    private fun hideEmptyState() {
        binding.layoutEmptyState.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
