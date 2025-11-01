package com.example.trainingtracker

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.example.trainingtracker.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Initialize UserSession
        UserSession.init(applicationContext)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        
        // Define top-level destinations (fragments where back button is hidden)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.WelcomeFragment,
                R.id.TrainingsListFragment,
                R.id.StatisticsFragment,
                R.id.UserProfileFragment
            )
        )
        
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Setup Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        
        // Handle Bottom Navigation item clicks
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_trainings -> {
                    if (navController.currentDestination?.id != R.id.TrainingsListFragment) {
                        navController.navigate(R.id.TrainingsListFragment)
                    }
                    true
                }
                R.id.navigation_statistics -> {
                    if (navController.currentDestination?.id != R.id.StatisticsFragment) {
                        navController.navigate(R.id.StatisticsFragment)
                    }
                    true
                }
                R.id.navigation_profile -> {
                    if (navController.currentDestination?.id != R.id.UserProfileFragment) {
                        navController.navigate(R.id.UserProfileFragment)
                    }
                    true
                }
                else -> false
            }
        }

        // Hide/show bottom navigation based on current destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.WelcomeFragment,
                R.id.SignInFragment,
                R.id.SignUpFragment -> {
                    // Hide bottom navigation on auth screens
                    bottomNavigationView.visibility = View.GONE
                }
                else -> {
                    // Show bottom navigation on main app screens
                    bottomNavigationView.visibility = View.VISIBLE
                    
                    // Update selected item in bottom navigation
                    val itemId = when (destination.id) {
                        R.id.TrainingsListFragment -> R.id.navigation_trainings
                        R.id.StatisticsFragment -> R.id.navigation_statistics
                        R.id.UserProfileFragment -> R.id.navigation_profile
                        else -> null
                    }
                    
                    itemId?.let { 
                        bottomNavigationView.selectedItemId = it
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                // TODO: Handle settings click
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}