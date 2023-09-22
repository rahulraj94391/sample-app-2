package com.example.instagram.bottomsheet

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.instagram.MSharedPreferences
import com.example.instagram.MainActivity
import com.example.instagram.R
import com.example.instagram.databinding.BottomsheetMyProfileMenuBinding
import com.example.instagram.fragments.BIOMETRIC_KEY
import com.example.instagram.fragments.SETTINGS_PREF_NAME
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder


private const val TAG = "CommTag_ProfileMenu"

class ProfileMenu : BottomSheetDialogFragment() {
    lateinit var binding: BottomsheetMyProfileMenuBinding
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState) as BottomSheetDialog
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.bottomsheet_my_profile_menu, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.savedPost.setOnClickListener { onSavedPostClicked() }
        binding.settings.setOnClickListener { onSettingsClicked() }
        binding.logout.setOnClickListener { onLogoutClicked() }
        
        val bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        //        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.isFitToContents = true
        bottomSheetBehavior.isDraggable = true
        //        bottomSheetBehavior.halfExpandedRatio = 0.6f
        //        bottomSheetBehavior.maxHeight = Resources.getSystem().displayMetrics.heightPixels /*not working as intended*/
        
        //        val layout = dialog!!.findViewById<CoordinatorLayout>(R.id.bottomSheetLayout)
        //        layout.minimumHeight = Resources.getSystem().displayMetrics.heightPixels
        
    }
    
    private fun onSettingsClicked() {
        val action = ProfileMenuDirections.actionProfileMenu2ToSettingsFragment()
        findNavController().navigate(action)
        this.dismiss()
    }
    
    private fun onSavedPostClicked() {
        findNavController().navigate(R.id.action_profileMenu2_to_savedPostFragment)
        this.dismiss()
    }
    
    private fun logoutUser() {
        val sharedPref: SharedPreferences = requireActivity().getSharedPreferences(MSharedPreferences.SHARED_PREF_NAME, Context.MODE_PRIVATE)
        sharedPref.edit().apply {
            putLong(MSharedPreferences.LOGGED_IN_PROFILE_ID, -1)
            putBoolean(MSharedPreferences.IS_LOGGED_IN, false)
            apply()
        }
        startActivity(Intent(requireContext(), MainActivity::class.java))
        requireActivity().finish()
    }
    
    private fun onLogoutClicked() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.logout))
            .setMessage(resources.getString(R.string.logout_from_instagram))
            .setCancelable(false)
            .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                setBiometricToFalse()
                logoutUser()
                dismiss()
            }
            .setNegativeButton(resources.getString(R.string.no)) { dialogInterface, _ ->
                dialogInterface.cancel()
                dismiss()
            }
            .show()
    }
    
    private fun setBiometricToFalse() {
        val settingsPref = requireActivity().getSharedPreferences(SETTINGS_PREF_NAME, Context.MODE_PRIVATE)
        settingsPref.edit().putBoolean(BIOMETRIC_KEY, false).apply()
    }
}