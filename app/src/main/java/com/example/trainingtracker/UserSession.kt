package com.example.trainingtracker

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson

/**
 * UserSession - manages the current user session
 * Stores and retrieves user data for the logged-in user
 */
object UserSession {
    
    private const val PREFS_NAME = "TrainingTrackerPrefs"
    private const val KEY_USER_DATA = "user_data"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    
    private var currentUser: User? = null
    private lateinit var prefs: SharedPreferences
    private val gson = Gson()
    
    /**
     * Initialize the session manager
     */
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadUserFromPrefs()
    }
    
    /**
     * Save user data to session and SharedPreferences
     */
    fun saveUser(user: User) {
        currentUser = user
        prefs.edit().apply {
            putString(KEY_USER_DATA, gson.toJson(user))
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }
    
    /**
     * Get current logged-in user
     */
    fun getUser(): User? {
        return currentUser
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        return firebaseUser != null && prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    /**
     * Clear user session (logout)
     */
    fun clearSession() {
        currentUser = null
        prefs.edit().apply {
            remove(KEY_USER_DATA)
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
        FirebaseAuth.getInstance().signOut()
    }
    
    /**
     * Load user data from SharedPreferences
     */
    private fun loadUserFromPrefs() {
        val userJson = prefs.getString(KEY_USER_DATA, null)
        if (userJson != null) {
            currentUser = gson.fromJson(userJson, User::class.java)
        }
    }
    
    /**
     * Get user ID
     */
    fun getUserId(): String? {
        return currentUser?.id ?: FirebaseAuth.getInstance().currentUser?.uid
    }
    
    /**
     * Get user email
     */
    fun getUserEmail(): String? {
        return currentUser?.email
    }
}
