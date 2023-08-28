package com.example.instagram.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.instagram.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

private const val TAG = "UploadDialog_CommTag"

class UploadDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Uploading")
            .setCancelable(false)
            .setMessage("The post is being scheduled for upload.")
            .setPositiveButton("Go to Home") { _, _ ->
                findNavController().popBackStack(R.id.postFragment, true)
            }
            .create()
        isCancelable = false
        return dialog
    }
}