package com.example.instagram.screen_commentView

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.COMM_ID
import com.example.instagram.HomeActivity
import com.example.instagram.OPEN_AND_LOCATE_COMMENT_KEY
import com.example.instagram.R
import com.example.instagram.common.Haptics
import com.example.instagram.common.MainViewModel
import com.example.instagram.common.util.ImageUtil
import com.example.instagram.databinding.BottomsheetCommentsBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

class CommentSheet : BottomSheetDialogFragment() {
    private lateinit var binding: BottomsheetCommentsBinding
    private var postId: Long by Delegates.notNull()
    private val args: CommentSheetArgs by navArgs()
    private lateinit var mainViewModel: MainViewModel
    private lateinit var viewModel: CommentBottomSheetModel
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var imageUtil: ImageUtil
    private lateinit var haptics: Haptics
    
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        postId = args.postId
        return super.onCreateDialog(savedInstanceState) as BottomSheetDialog
    }
    
    override fun onDestroy() {
        super.onDestroy()
        binding.commentRV.adapter = null
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        imageUtil = ImageUtil(requireContext())
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        viewModel = ViewModelProvider(this)[CommentBottomSheetModel::class.java]
        binding = DataBindingUtil.inflate(inflater, R.layout.bottomsheet_comments, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        haptics = Haptics(requireContext())
        lifecycleScope.launch {
            viewModel.getComments(postId, mainViewModel.loggedInProfileId!!)
        }
        val bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        with(bottomSheetBehavior) {
            isFitToContents = true
            isDraggable = true
            // halfExpandedRatio = 0.6f
            state = BottomSheetBehavior.STATE_EXPANDED
        }
        
        /*bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            // TODO: Improve bottom sheet behaviour
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    // Adjust the height dynamically
                    val layoutParams = bottomSheet.layoutParams
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    bottomSheet.layoutParams = layoutParams
                }
                else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    // Set the initial peek height
                    bottomSheetBehavior.peekHeight = 40
                }

            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                Log.d(TAG, "onSlide: slideOffset = $slideOffset")
                if (slideOffset > 0 && slideOffset < 1) {
                    Log.e(TAG, "onSlide: if-block")
                    // The bottom sheet is being dragged
                    // You can implement additional logic here if needed
                }
                else if (slideOffset == 0f) {
                    // The bottom sheet is completely collapsed
                    // You can close or hide the bottom sheet here
                    Log.i(TAG, "onSlide: else-block")
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }
        })*/
        
        commentAdapter = CommentAdapter(this::showDeleteCommentDialog, mainViewModel.loggedInProfileId!!)
        binding.commentRV.adapter = commentAdapter
        binding.commentRV.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        
        viewModel.comments.observe(viewLifecycleOwner) {
            commentAdapter.updateList(it)
            (requireActivity() as HomeActivity).navHostFragment.childFragmentManager.setFragmentResultListener(OPEN_AND_LOCATE_COMMENT_KEY, viewLifecycleOwner) { _, bundle ->
                val commentId = bundle.getLong(COMM_ID)
                val pos = commentAdapter.findPosition(commentId)
                if (pos != -1) {
                    lifecycleScope.launch {
                        binding.commentRV.scrollToPosition(pos)
                        delay(200)
                        val vh = binding.commentRV.findViewHolderForAdapterPosition(pos)
                        vh?.itemView?.setBackgroundColor(requireActivity().resources.getColor(R.color.highlight))
                        delay(400)
                        vh?.itemView?.setBackgroundColor(resources.getColor(android.R.color.transparent))
                    }
                }
                (requireActivity() as HomeActivity).navHostFragment.childFragmentManager.clearFragmentResultListener(OPEN_AND_LOCATE_COMMENT_KEY)
            }
        }
        
        viewModel.commenterImages.observe(viewLifecycleOwner) {
            commentAdapter.updateImages(it)
        }
        
        lifecycleScope.launch {
            val url = imageUtil.getProfilePictureUrl(mainViewModel.loggedInProfileId!!) ?: return@launch
            val bitmap = ImageUtil(requireContext()).getBitmap(url)
            withContext(Dispatchers.Main) {
                binding.profileImage.setImageBitmap(bitmap)
            }
        }
        binding.commentButton.setOnClickListener { postComment() }
    }
    
    private suspend fun addProfilePicUrlWithOnNewComment() {
        val url = imageUtil.getProfilePictureUrl(mainViewModel.loggedInProfileId!!) ?: return
        commentAdapter.addImageUrlToList(url)
    }
    
    private fun postComment() {
        val commentText = binding.commentBox.text.toString()
        if (isCommentQualified(commentText)) {
            haptics.doubleClick()
            return
        }
        
        haptics.light()
        lifecycleScope.launch {
            viewModel.insertComment(commentText, mainViewModel.loggedInProfileId!!, postId)
            addProfilePicUrlWithOnNewComment()
            viewModel.getComments(postId, mainViewModel.loggedInProfileId!!)
        }
        binding.commentBox.setText("")
    }
    
    private fun showDeleteCommentDialog(pos: Int) {
        if (viewModel.comments.value!![pos].profileId != mainViewModel.loggedInProfileId!!) return
        MaterialAlertDialogBuilder(requireContext()).setMessage("Delete Comment ?").setCancelable(true).setPositiveButton("Yes") { _, _ ->
            lifecycleScope.launch {
                val commentId = viewModel.comments.value!![pos].commentId
                viewModel.deleteComment(commentId)
                viewModel.getComments(postId, mainViewModel.loggedInProfileId!!)
            }
        }.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.cancel()
        }.show()
    }
    
    private fun isCommentQualified(commentText: String): Boolean {
        return if (commentText.isBlank()) {
            Toast.makeText(requireContext(), "Cannot post blank comment.", Toast.LENGTH_SHORT).show()
            true
        } else {
            false
        }
    }
}