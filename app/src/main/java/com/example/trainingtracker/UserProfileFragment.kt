package com.example.trainingtracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.trainingtracker.databinding.FragmentUserProfileBinding
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * User Profile Fragment - displays user information
 */
class UserProfileFragment : Fragment() {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load user data from UserSession
        loadUserData()

        // Logout button
        binding.buttonLogout.setOnClickListener {
            logout()
        }
    }

    private fun loadUserData() {
        val user = UserSession.getUser()

        if (user != null) {
            // Extract username from email (part before @)
            val username = user.email.substringBefore("@")
            
            // Set welcome text
            binding.textviewWelcome.text = "Welcome, $username"

            // Set email
            binding.textviewEmail.text = user.email

            // Set user ID
            binding.textviewUserId.text = user.id

            // Format and set created date
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val createdDate = dateFormat.format(user.createdAt.toDate())
            binding.textviewCreatedAt.text = createdDate
        } else {
            // If no user data, redirect to sign in
            binding.textviewWelcome.text = "Welcome, Guest"
            binding.textviewEmail.text = "Not logged in"
            binding.textviewUserId.text = "N/A"
            binding.textviewCreatedAt.text = "N/A"
        }
    }

    private fun logout() {
        // Clear user session
        UserSession.clearSession()

        // Navigate back to welcome screen
        findNavController().navigate(R.id.action_UserProfileFragment_to_WelcomeFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
