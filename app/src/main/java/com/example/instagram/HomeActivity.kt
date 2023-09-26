package com.example.instagram

import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.instagram.databinding.ActivityHomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val HIDDEN = true
const val NOT_HIDDEN = false
const val DURATION = 80L

private const val TAG = "HomeActivity_CommTag"

class HomeActivity : AppCompatActivity() {
    lateinit var haptics: Haptics
    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController
    private lateinit var mainViewModel: MainViewModel
    lateinit var navHostFragment: NavHostFragment
    private var BOTTOM_NAV_CURRENT_STATE = NOT_HIDDEN
    private val broadcastReceiver = InternetBroadcastReceiver(::showHideInternetLabel)
    private var internetConnectivityJob: Job? = null
    
    
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
        
        // haptic
        haptics = Haptics(this)
        
        
        // setup bottom navigation view with nav controller
        navHostFragment = supportFragmentManager.findFragmentById(R.id.fragContainerView) as NavHostFragment
        navController = navHostFragment.findNavController()
        binding.bottomNavView.setupWithNavController(navController)
        binding.bottomNavView.setOnItemReselectedListener {
            //            Log.d(TAG, "item = $it")
            val reselectedDestinationId = it.itemId
            navController.popBackStack(reselectedDestinationId, inclusive = false)
            /*when (it.itemId) {
                R.id.homeFragment -> {}
                R.id.postFragment -> {}
                R.id.searchFragment -> {}
                R.id.profileFragment -> {}
            }*/
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
            /*val state = when (fm.findFragmentById(R.id.fragContainerView)) {
                is HomeFragment, is PostFragment, is SearchFragment, is ProfileFragment -> NOT_HIDDEN
                else -> HIDDEN
            }
            if (BOTTOM_NAV_CURRENT_STATE != state) {
                if (state == HIDDEN) {
                    hideBottomNavigationView()
                } else {
                    showBottomNavigationView()
                }
            }*/
            
        }
        
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            //            Log.e(TAG, "destination = ${destination.label}")
        }
    }
    
    private fun hideBottomNavigationView() {
        BOTTOM_NAV_CURRENT_STATE = HIDDEN
        val bnv = binding.bottomNavView
        bnv.clearAnimation()
        bnv.animate().alpha(0.0f).translationY(bnv.height.toFloat()).duration = DURATION
        CoroutineScope(Dispatchers.Main).launch {
            delay(DURATION)
            bnv.visibility = View.GONE
        }
    }
    
    private fun showBottomNavigationView() {
        BOTTOM_NAV_CURRENT_STATE = NOT_HIDDEN
        val bnv = binding.bottomNavView
        bnv.visibility = View.VISIBLE
        bnv.clearAnimation()
        bnv.animate().alpha(1.0f).translationY(0f).duration = DURATION
    }
    
    override fun onResume() {
        super.onResume()
        val filter = IntentFilter()
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(broadcastReceiver, filter)
        
        // this removes the tooltip from the menu of bottom navigation view
        binding.bottomNavView.menu.forEach {
            val view = binding.bottomNavView.findViewById<View>(it.itemId)
            view.setOnLongClickListener {
                true
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        unregisterReceiver(broadcastReceiver)
    }
    
    private fun showHideInternetLabel(status: Boolean) {
        internetConnectivityJob?.cancel()
        internetConnectivityJob = lifecycleScope.launch {
            if (status) { // internet is not connected
                binding.internetNotification.apply {
                    text = getString(R.string.internet_not_connected)
                    setBackgroundColor(resources.getColor(R.color.internet_not_available))
                    visibility = View.VISIBLE
                }
            } else { // internet is connected
                binding.internetNotification.apply {
                    text = getString(R.string.internet_is_connected)
                    setBackgroundColor(resources.getColor(R.color.internet_available))
                    delay(1000)
                    visibility = View.GONE
                }
            }
        }
    }
}