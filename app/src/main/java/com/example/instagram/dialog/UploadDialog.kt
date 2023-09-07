package com.example.instagram.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.instagram.R
import com.example.instagram.databinding.DialogUploadProgressBinding
import com.example.instagram.viewmodels.PostFragViewModel
import com.example.instagram.worker.IS_UPLOAD_FINISHED
import com.google.android.material.dialog.MaterialAlertDialogBuilder

private const val TAG = "UploadDialog_CommTag"

class UploadDialog : DialogFragment() {
    private lateinit var viewModel: PostFragViewModel
    private lateinit var dialog: Dialog
    private lateinit var binding: DialogUploadProgressBinding
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DataBindingUtil.inflate(requireActivity().layoutInflater, R.layout.dialog_upload_progress, null, false)
        
        dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setCancelable(false)
            .create()
        isCancelable = false
        return dialog
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[PostFragViewModel::class.java]
    }
    
    override fun onResume() {
        super.onResume()
        binding.btnGoToHome.setOnClickListener { findNavController().popBackStack(R.id.postFragment, true) }
        viewModel.uuidWorkReq.observe(this) { uuid ->
            WorkManager.getInstance(requireContext()).getWorkInfoByIdLiveData(uuid).observe(this) { workInfo ->
                if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                    val value = workInfo.outputData.getBoolean(IS_UPLOAD_FINISHED, false)
                    if (value) {
                        binding.uploadIndicator.visibility = View.INVISIBLE
                        binding.uploadCompletedIV.visibility = View.VISIBLE
                        binding.uploadMessage.text = "Photos have been uploaded."
                    }
                }
            }
        }
    }
    
}