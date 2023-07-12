package com.example.instagram.fragments

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.view.contains
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R
import com.example.instagram.adapters.KeepAtLeastOneImage
import com.example.instagram.adapters.SearchUserAdapter
import com.example.instagram.adapters.SearchUsernameClickListener
import com.example.instagram.adapters.SelectedPostImageAdapter
import com.example.instagram.databinding.FragmentPostBinding
import com.example.instagram.viewmodels.PostFragViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch

private const val TAG = "CommTag_PostFragment"

class PostFragment : Fragment(), KeepAtLeastOneImage, SearchUsernameClickListener {
    private lateinit var binding: FragmentPostBinding
    private lateinit var viewModel: PostFragViewModel

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
    private lateinit var searchView: SearchView
    private lateinit var chipGroup: ChipGroup
    private lateinit var searchForTagRV: RecyclerView
    private lateinit var searchTagAdapter: SearchUserAdapter
    // select profile tag bottom sheet dialog ___ END


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_post, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[PostFragViewModel::class.java]
        binding.postText.addTextChangedListener(CustomTextWatcher())
        binding.btnPost.setOnClickListener { onPostBtnClicked() }


        //
        selectedPicsDialog = createPicsBottomSheetDialog()
        initializePicBottomSheet()
        binding.btnSelectPics.setOnClickListener { selectedPicsDialog.show() }


        //
        tagDialog = createTagBottomSheetDialog()
        initializeTagBottomSheet()
        binding.btnTagPerson.setOnClickListener { tagDialog.show() }

    }


    private fun initializeTagBottomSheet() {
        doneBtnTag = tagDialog.findViewById(R.id.btnDone)
        searchView = tagDialog.findViewById(R.id.tagSearchView)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    lifecycleScope.launch {
                        viewModel.getSearchResult(newText)
                    }
                }
                return true
            }
        })
        chipGroup = tagDialog.findViewById(R.id.chipTagProfile)
        searchForTagRV = tagDialog.findViewById(R.id.searchForTagRV)
        searchTagAdapter = SearchUserAdapter(mutableListOf(), this, R.layout.row_user_search_slim)
        searchForTagRV.adapter = searchTagAdapter
        searchForTagRV.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        doneBtnTag.setOnClickListener {
            tagDialog.dismiss()
        }


        viewModel.tempListTagUser.observe(viewLifecycleOwner) {
            Log.d(TAG, "initializeTagBottomSheet: ${viewModel.tempListTagUser.value}")
            searchTagAdapter.setNewList(it)
        }

    }

    private fun addTagChipToChipGroup(profileId: Long): Chip {
        val chip = Chip(requireContext())
        val chipDrawable: ChipDrawable = ChipDrawable.createFromAttributes(requireContext(), null, 0, com.google.android.material.R.style.Widget_MaterialComponents_Chip_Entry)
        chip.setChipDrawable(chipDrawable)
        chip.isCheckable = false
        chip.isClickable = false
        chip.setChipIconResource(R.drawable.person_filled)
        chip.iconStartPadding = 4f
        chip.setPadding(60, 10, 60, 10)
        lifecycleScope.launch {
            chip.text = viewModel.getNameOfUser(profileId)
        }
        chip.setOnCloseIconClickListener {
            chipGroup.removeView(chip)
        }
        chipGroup.addView(chip)
        return chip
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

        if (!isInternetActive(requireContext())) {
            Toast.makeText(requireContext(), "Internet not active.", Toast.LENGTH_SHORT).show()
        }

        if (text.isBlank()) {
            Toast.makeText(requireContext(), "Text can't be blank.", Toast.LENGTH_SHORT).show()
            return
        }
        if (imagesSize < 1) {
            Toast.makeText(requireContext(), "Select photos to upload.", Toast.LENGTH_SHORT).show()
            return
        }
        prepareTagsOnPost()
        viewModel.insertPost()
        binding.postText.setText("")

        recyclerView2.visibility = View.GONE
        insLabel.visibility = View.VISIBLE
        reselectBtn.isEnabled = false
        chipGroup.removeAllViews()
    }

    private fun prepareTagsOnPost() {
        for (i in viewModel.finalTagUserIds) {
            if (chipGroup.contains(i.first)) {
                viewModel.tagsToUpload.add(i.second)
            }
        }
    }

    private fun isInternetActive(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
            return true
        }
        return false
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

    override fun onClick(pos: Int) {
        val chip = addTagChipToChipGroup(viewModel.tempListTagUser.value!![pos].profile_id)
        viewModel.finalTagUserIds.add(Pair(chip, viewModel.tempListTagUser.value!![pos].profile_id))
        searchView.setQuery("", false)
        viewModel.tempListTagUser.postValue(mutableListOf())
    }
}

