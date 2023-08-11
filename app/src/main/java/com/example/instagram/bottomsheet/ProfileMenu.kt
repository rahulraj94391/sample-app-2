package com.example.instagram.bottomsheet

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


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
        this.dismiss()
        
    }
    
    private fun onSavedPostClicked() {
        findNavController().navigate(R.id.action_profileMenu2_to_savedPostFragment)
        this.dismiss()
    }
    
    private fun onLogoutClicked() {
        val sharedPref: SharedPreferences = requireActivity().getSharedPreferences(MSharedPreferences.SHARED_PREF_NAME, Context.MODE_PRIVATE)
        sharedPref.edit().apply {
            putLong(MSharedPreferences.LOGGED_IN_PROFILE_ID, -1)
            putBoolean(MSharedPreferences.IS_LOGGED_IN, false)
            apply()
        }
        startActivity(Intent(requireContext(), MainActivity::class.java))
        this.dismiss()
        requireActivity().finish()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val view = requireActivity().window.decorView.rootView!!
            view.setRenderEffect(null)
        }
    }
}