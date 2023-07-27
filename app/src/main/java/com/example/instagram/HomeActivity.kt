package com.example.instagram

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.instagram.databinding.ActivityHomeBinding

private const val TAG = "CommTag_HomeActivity"

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController
    private lateinit var mainViewModel: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        /*IMPORTANT    ----    START */
        // first put the current user id in the Main ViewModel as the Home fragment starts creating once the Activity
        // reaches the {DataBindingUtil.setContentView(this, R.layout.activity_home)} line as the home fragment is the
        // first screen that shows up to the user.
        // without this the app crashes as the view model factory uses logged in id from main view model to create the home fragment.
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        val sharedPreferences = getSharedPreferences(MSharedPreferences.SHARED_PREF_NAME, Context.MODE_PRIVATE)
        mainViewModel.loggedInProfileId = sharedPreferences.getLong(MSharedPreferences.LOGGED_IN_PROFILE_ID, -1)
        /*IMPORTANT    ----    END */


        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        // setup bottom navigation view with nav controller
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragContainerView) as NavHostFragment
        navController = navHostFragment.findNavController()
        binding.bottomNavView.setupWithNavController(navController)
        binding.bottomNavView.setOnItemReselectedListener {
            when (it.itemId) {
                R.id.homeFragment -> {}
                R.id.postFragment -> {}
                R.id.searchFragment -> {onSearchReselected()}
                R.id.profileFragment -> {}
            }
        }


        val drawable = getDrawable(R.drawable.loading_error)

        /*navController.addOnDestinationChangedListener { controller, dest, args ->
            when (dest.id) {
                R.id.profileFragment -> {
                    val profileId = NavArgument.Builder().setDefaultValue(mainViewModel.loggedInProfileId!!).build()
                    dest.addArgument("profileId", profileId)
                    val updatedArguments = args ?: Bundle()
                    updatedArguments.putLong("profileId", mainViewModel.loggedInProfileId!!)
                    controller.currentDestination?.addInDefaultArgs(updatedArguments)
                }
            }
        }*/

    }

    private fun onSearchReselected() {
//        Log.d(TAG, "onSearchReselected: ")
//        supportFragmentManager.popBackStack(R.id.searchFragment, FragmentManager.POP_BACK_STACK_INCLUSIVE )
    }


    override fun onNavigateUp(): Boolean {
//        Log.d(TAG, "onNavigateUp: ")
        return super.onNavigateUp()
    }

    override fun onBackPressed() {
//        Log.d(TAG, "onBackPressed: ")
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
//        Log.d(TAG, "onDestroy")
    }
}