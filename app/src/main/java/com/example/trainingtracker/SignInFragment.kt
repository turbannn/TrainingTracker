package com.example.trainingtracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.trainingtracker.databinding.FragmentSignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Sign In Fragment - allows users to sign in
 */
class SignInFragment : Fragment() {

    private var _binding: FragmentSignInBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Sign In button - implement sign in logic
        binding.buttonSignInSubmit.setOnClickListener {
            signInUser()
        }

        // Navigate to Sign Up
        binding.textviewSignUpLink.setOnClickListener {
            findNavController().navigate(R.id.action_SignInFragment_to_SignUpFragment)
        }
    }
    
    /**
     * Signs in an existing user with email and password
     */
    private fun signInUser() {
        val email = binding.inputEmail.text.toString().trim()
        val password = binding.inputPassword.text.toString()
        
        // Validate inputs
        if (email.isEmpty()) {
            binding.inputLayoutEmail.error = "Email is required"
            return
        }
        
        if (password.isEmpty()) {
            binding.inputLayoutPassword.error = "Password is required"
            return
        }
        
        // Clear errors
        binding.inputLayoutEmail.error = null
        binding.inputLayoutPassword.error = null
        
        // Disable button to prevent multiple clicks
        binding.buttonSignInSubmit.isEnabled = false
        
        // Sign in with Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, retrieve user data from Firestore
                    val firebaseUser = auth.currentUser
                    firebaseUser?.let { user ->
                        // Fetch user data from Firestore
                        firestore.collection("users")
                            .document(user.uid)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    // User data retrieved successfully
                                    val userData = document.toObject(User::class.java)
                                    
                                    // Save user to session
                                    userData?.let {
                                        UserSession.saveUser(it)
                                    }
                                    
                                    Toast.makeText(
                                        requireContext(),
                                        "Welcome back, ${userData?.email ?: email}!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    
                                    // Navigate to main app screen (Trainings List)
                                    findNavController().navigate(R.id.action_SignInFragment_to_TrainingsListFragment)
                                    
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "User data not found",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                binding.buttonSignInSubmit.isEnabled = true
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    requireContext(),
                                    "Failed to retrieve user data: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                                binding.buttonSignInSubmit.isEnabled = true
                            }
                    }
                } else {
                    // If sign in fails, display a message to the user
                    Toast.makeText(
                        requireContext(),
                        "Sign in failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.buttonSignInSubmit.isEnabled = true
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
