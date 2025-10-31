package com.example.trainingtracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.trainingtracker.databinding.FragmentSignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

/**
 * Sign Up Fragment - allows users to create a new account
 */
class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Sign Up button - implement sign up logic
        binding.buttonSignUpSubmit.setOnClickListener {
            signUpUser()
        }

        // Navigate to Sign In
        binding.textviewSignInLink.setOnClickListener {
            findNavController().navigate(R.id.action_SignUpFragment_to_SignInFragment)
        }
    }
    
    /**
     * Signs up a new user with email and password
     */
    private fun signUpUser() {
        val email = binding.inputEmail.text.toString().trim()
        val password = binding.inputPassword.text.toString()
        val repeatPassword = binding.inputRepeatPassword.text.toString()
        
        // Validate inputs
        if (email.isEmpty()) {
            binding.inputLayoutEmail.error = "Email is required"
            return
        }
        
        if (password.isEmpty()) {
            binding.inputLayoutPassword.error = "Password is required"
            return
        }
        
        if (repeatPassword.isEmpty()) {
            binding.inputLayoutRepeatPassword.error = "Please repeat password"
            return
        }
        
        // Check if passwords match
        if (password != repeatPassword) {
            binding.inputLayoutRepeatPassword.error = "Passwords do not match"
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Clear errors
        binding.inputLayoutEmail.error = null
        binding.inputLayoutPassword.error = null
        binding.inputLayoutRepeatPassword.error = null
        
        // Disable button to prevent multiple clicks
        binding.buttonSignUpSubmit.isEnabled = false
        
        // Create user with Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign up success, save user data to Firestore
                    val firebaseUser = auth.currentUser
                    firebaseUser?.let {
                        val user = User(
                            id = it.uid,
                            email = email,
                            createdAt = Timestamp.now(),
                            password = password // Note: In production, never store passwords
                        )
                        
                        // Save user to Firestore
                        firestore.collection("users")
                            .document(it.uid)
                            .set(user)
                            .addOnSuccessListener {
                                // Save user to session
                                UserSession.saveUser(user)
                                
                                Toast.makeText(
                                    requireContext(),
                                    "Registration successful!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                
                                // Navigate to main app screen (Trainings List)
                                findNavController().navigate(R.id.action_SignUpFragment_to_TrainingsListFragment)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    requireContext(),
                                    "Failed to save user data: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                                binding.buttonSignUpSubmit.isEnabled = true
                            }
                    }
                } else {
                    // If sign up fails, display a message to the user
                    Toast.makeText(
                        requireContext(),
                        "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.buttonSignUpSubmit.isEnabled = true
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
