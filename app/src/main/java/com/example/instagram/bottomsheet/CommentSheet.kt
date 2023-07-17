package com.example.instagram.bottomsheet

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.instagram.R
import com.example.instagram.databinding.BottomsheetCommentsBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.properties.Delegates

private const val POST_ID_KEY = "post_id"
private const val TAG = "CommTag_CommentSheet"

class CommentSheet : BottomSheetDialogFragment() {
    private lateinit var binding: BottomsheetCommentsBinding
    private var postId: Long by Delegates.notNull()

    companion object {
        fun newInstance(postId: Long): CommentSheet {
            val args = Bundle()
            args.putLong(POST_ID_KEY, postId)

            val fragment = CommentSheet()
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        postId = requireArguments().getLong(POST_ID_KEY)
        Log.d(TAG, "Comment box (post id = $postId)")
        return super.onCreateDialog(savedInstanceState) as BottomSheetDialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.bottomsheet_comments, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        bottomSheetBehavior.isFitToContents = true
        bottomSheetBehavior.isDraggable = true
    }



}