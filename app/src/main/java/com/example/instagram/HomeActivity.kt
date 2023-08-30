package com.example.instagram

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.instagram.databinding.ActivityHomeBinding
import com.example.instagram.fragments.HomeFragment
import com.example.instagram.fragments.PostFragment
import com.example.instagram.fragments.ProfileFragment
import com.example.instagram.fragments.SearchFragment

const val HIDDEN = true
const val NOT_HIDDEN = false

private const val TAG = "HomeActivity_CommTag"

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController
    private lateinit var mainViewModel: MainViewModel
    private var BOTTOM_NAV_CURRENT_STATE = NOT_HIDDEN
    
    
    override fun onCreate(savedInstanceState: Bundle?) {/*IMPORTANT    ----    START */ // first put the current user id in the Main ViewModel as the Home fragment starts creating once the Activity
        // reaches the {DataBindingUtil.setContentView(this, R.layout.activity_home)} line as the home fragment is the
        // first screen that shows up to the user.
        // without this the app crashes as the view model factory uses logged in id from main view model to create the home fragment.
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        val sharedPreferences = getSharedPreferences(MSharedPreferences.SHARED_PREF_NAME, Context.MODE_PRIVATE)
        mainViewModel.loggedInProfileId = sharedPreferences.getLong(
            MSharedPreferences.LOGGED_IN_PROFILE_ID, -1
        )/*IMPORTANT    ----    END */
        
        
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
                R.id.searchFragment -> {
                    onSearchReselected()
                }
                
                R.id.profileFragment -> {}
            }
        }
        
        
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
        
        //        Log.d(TAG, "onCreate: before backstackChangeListener")
        navHostFragment.childFragmentManager.addOnBackStackChangedListener {
            val fm = navHostFragment.childFragmentManager
            //            Log.d(TAG, "backstack count = $backStackCount")
            val fragment = fm.findFragmentById(R.id.fragContainerView)
            val state = when (fragment) {
                is HomeFragment, is PostFragment, is SearchFragment, is ProfileFragment -> NOT_HIDDEN
                else -> HIDDEN
            }
            if (BOTTOM_NAV_CURRENT_STATE != state) {
                if (state == HIDDEN) {
                    hideBottomNavigationView()
                } else {
                    showBottomNavigationView()
                }
            }
            
        }
        
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            //            Log.e(TAG, "destination = ${destination.label}")
        }
    }
    
    private fun hideBottomNavigationView() {
        BOTTOM_NAV_CURRENT_STATE = HIDDEN
        val view = binding.bottomNavView
        /*view.clearAnimation()
        view.animate().translationY(view.height.toFloat()).duration = 150*/
        
    }
    
    private fun showBottomNavigationView() {
        BOTTOM_NAV_CURRENT_STATE = NOT_HIDDEN
        val view = binding.bottomNavView
        /*view.clearAnimation()
        view.animate().translationY(0f).duration = 150*/
        
    }
    
    override fun onResume() {
        super.onResume()
        binding.bottomNavView.menu.forEach {
            val view = binding.bottomNavView.findViewById<View>(it.itemId)
            view.setOnLongClickListener {
                true
            }
        }
    }
    
    
    private fun onSearchReselected() {
        // Log.d(TAG, "onSearchReselected: ")
        // supportFragmentManager.popBackStack(R.id.searchFragment, FragmentManager.POP_BACK_STACK_INCLUSIVE )
    }
}