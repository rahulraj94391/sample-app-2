package com.example.instagram.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.instagram.R
import com.example.instagram.databinding.DialogDownloadProgressBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SaveImagesDialog() : DialogFragment() {
    private lateinit var binding: DialogDownloadProgressBinding
    private lateinit var dialog: Dialog
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DataBindingUtil.inflate(requireActivity().layoutInflater, R.layout.dialog_download_progress, null, false)
        
        dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setCancelable(false)
            .create()
        isCancelable = false
        return dialog
    }
}