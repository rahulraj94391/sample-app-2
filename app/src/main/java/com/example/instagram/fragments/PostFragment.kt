package com.example.instagram.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R
import com.example.instagram.adapters.KeepAtLeastOneImage
import com.example.instagram.adapters.SelectedPostImageAdapter
import com.example.instagram.databinding.FragmentPostBinding
import com.example.instagram.viewmodels.PostFragmentViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup

const val IMAGE_PICKER_REQ_CODE = 976
private const val TAG = "CommTag_PostFragment"

class PostFragment : Fragment(), KeepAtLeastOneImage {
    private lateinit var binding: FragmentPostBinding
    private lateinit var viewModel: PostFragmentViewModel

    // select pic bottom sheet dialog refs ___ START
    private lateinit var selectedPicsDialog: Dialog
    private lateinit var reselectBtn: MaterialButton
    private lateinit var doneBtn: MaterialButton
    private lateinit var insLabel: TextView
    private lateinit var recyclerView2: RecyclerView
    private lateinit var selectedPicsAdapter: SelectedPostImageAdapter
    // select pic bottom sheet dialog refs ___ END


    // select profile tag bottom sheet dialog ___ START
    private lateinit var tagDialog: Dialog
    private lateinit var doneBtnTag: MaterialButton
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var chipGroup: ChipGroup
    // select profile tag bottom sheet dialog ___ END


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_post, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[PostFragmentViewModel::class.java]
        binding.postText.addTextChangedListener(CustomTextWatcher())
        binding.btnPost.setOnClickListener { onPostBtnClicked() }

        selectedPicsDialog = createPicsBottomSheetDialog()
        initializePicBottomSheet()
        binding.btnSelectPics.setOnClickListener { selectedPicsDialog.show() }


        tagDialog = createTagBottomSheetDialog()
        initializeTagBottomSheet()
        binding.btnTagPerson.setOnClickListener { tagDialog.show() }


    }

    private fun initializeTagBottomSheet() {
        doneBtnTag = tagDialog.findViewById(R.id.btnDone)
        autoCompleteTextView = tagDialog.findViewById(R.id.autoCompleteTV)
        chipGroup = tagDialog.findViewById(R.id.chipTagProfile)

        doneBtnTag.setOnClickListener { tagDialog.dismiss() }
    }

    private fun createTagBottomSheetDialog(): Dialog {
        val dialog = Dialog(requireContext())
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.selected_tag_profile)
        }
        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            attributes?.windowAnimations = R.style.DialogAnimation
            setGravity(Gravity.BOTTOM)
        }
        return dialog
    }

    private fun initializePicBottomSheet() {
//        selectedPicsDialog.setCanceledOnTouchOutside(false)
        reselectBtn = selectedPicsDialog.findViewById(R.id.btnReselect)
        doneBtn = selectedPicsDialog.findViewById(R.id.btnDone)
        insLabel = selectedPicsDialog.findViewById(R.id.tapToSelectLabel)
        recyclerView2 = selectedPicsDialog.findViewById(R.id.selectedPicRV2)
        selectedPicsAdapter = SelectedPostImageAdapter(viewModel.postImagesUri, this)
        recyclerView2.adapter = selectedPicsAdapter
        recyclerView2.layoutManager = GridLayoutManager(requireContext(), 3)

        reselectBtn.isEnabled = false
        insLabel.setOnClickListener { resultLauncherInitial.launch("image/*") }
        reselectBtn.setOnClickListener { resultLauncherInitial.launch("image/*") }
        doneBtn.setOnClickListener {
            selectedPicsDialog.dismiss()
        }
    }

    private fun onPostBtnClicked() {
        val text = viewModel.postText
        val imagesSize = viewModel.postImagesUri.size

        if (text.isBlank()) {
            Toast.makeText(requireContext(), "Text can't be blank.", Toast.LENGTH_SHORT).show()
            return
        }
        if (imagesSize < 1) {
            Toast.makeText(requireContext(), "Select photos to upload.", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.insertPost()
        binding.postText.setText("")

        recyclerView2.visibility = View.GONE
        insLabel.visibility = View.VISIBLE
        reselectBtn.isEnabled = false
    }

    private val resultLauncherInitial = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        viewModel.postImagesUri = mutableListOf()
        for (i in uris.indices) {
            if (i == 6) break
            viewModel.postImagesUri.add(uris[i])
        }

        if (viewModel.postImagesUri.isNotEmpty()) {
            selectedPicsAdapter.setNewList(viewModel.postImagesUri)
            insLabel.visibility = View.GONE
            recyclerView2.visibility = View.VISIBLE
            reselectBtn.isEnabled = true
        }
    }

    private fun createPicsBottomSheetDialog(): Dialog {
        val dialog = Dialog(requireContext())
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.bottomsheet_selected_pics)
        }
        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            attributes?.windowAnimations = R.style.DialogAnimation
            setGravity(Gravity.BOTTOM)
        }
        return dialog
    }

    inner class CustomTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            viewModel.postText = s.toString()

            Log.d(TAG, "afterTextChanged: ${viewModel.postText}")
        }
    }

    override fun postAtLeastOnePhoto() {
        Toast.makeText(requireContext(), "Post at least one photo.", Toast.LENGTH_SHORT).show()
    }
}

