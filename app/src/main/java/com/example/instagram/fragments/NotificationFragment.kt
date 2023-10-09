package com.example.instagram.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.DateTime
import com.example.instagram.HomeActivity
import com.example.instagram.MainViewModel
import com.example.instagram.R
import com.example.instagram.TimeFormatting
import com.example.instagram.adapters.FOLLOWLOG
import com.example.instagram.adapters.LIKELOG
import com.example.instagram.adapters.NotificationAdapter
import com.example.instagram.database.AppDatabase
import com.example.instagram.databinding.FragmentNotificationBinding
import com.example.instagram.viewmodels.NotificationsFragViewModel
import kotlinx.coroutines.launch

const val KEY = "open_comment_on_post"
const val COMM_ID = "comm_id"

private const val TAG = "NotificationFragment_CommTag"

class NotificationFragment : Fragment() {
    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: NotificationsFragViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var db: AppDatabase
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())
        viewModel = ViewModelProvider(this)[NotificationsFragViewModel::class.java]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        notificationAdapter = NotificationAdapter(mainViewModel.loggedInProfileId!!, ::spanBuilder, ::openProfile, ::openPost, ::openCommentOnPost)
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notification, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            viewModel.getActivityLog(mainViewModel.loggedInProfileId!!)
        }
        binding.toolbar.setNavigationIcon(R.drawable.arrow_back_24)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.swipeRefreshNotification.setOnRefreshListener {
            lifecycleScope.launch {
                viewModel.getActivityLog(mainViewModel.loggedInProfileId!!)
            }
        }
        binding.notificationRV.apply {
            adapter = notificationAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }
        
        viewModel.placeHolderReference.observe(viewLifecycleOwner) { placeHolderReference ->
            binding.progressBar.visibility = View.INVISIBLE
            binding.notificationRV.visibility = View.VISIBLE
            if (placeHolderReference.size < 1) {
                binding.apply {
                    swipeRefreshNotification.visibility = View.INVISIBLE
                    noNotificationInstruction.visibility = View.VISIBLE
                }
            } else {
                binding.apply {
                    noNotificationInstruction.visibility = View.INVISIBLE
                    swipeRefreshNotification.visibility = View.VISIBLE
                }
            }
            if (binding.swipeRefreshNotification.isRefreshing) {
                binding.swipeRefreshNotification.isRefreshing = false
            }
//            if (placeHolderReference.size == notificationAdapter.itemCount) return@observe
            notificationAdapter.setNewList(viewModel.followLogs, viewModel.likeLogs, viewModel.commentLogs, placeHolderReference)
            
        }
    }
    
    private fun spanBuilder(username: String, timeLong: Long, type: Int): SpannableStringBuilder {
        val time = DateTime.timeFormatter(timeLong, TimeFormatting.COMMENT)
        val builder = SpannableStringBuilder()
        builder.append(username)
        builder.setSpan(StyleSpan(Typeface.BOLD), 0, username.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.append(
            when (type) {
                FOLLOWLOG -> resources.getString(R.string.started_following_you)
                LIKELOG -> resources.getString(R.string.liked_your_post)
                else -> resources.getString(R.string.commented_on_your_post)
            }
        )
        val spanLen = builder.length
        builder.append(time)
        builder.setSpan(RelativeSizeSpan(0.9f), spanLen, spanLen + time.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.setSpan(ForegroundColorSpan(resources.getColor(R.color.grey, requireActivity().theme)), spanLen, spanLen + time.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return builder
    }
    
    private fun openCommentOnPost(postId: Long, commentId: Long) {
        (requireActivity() as HomeActivity).navHostFragment.childFragmentManager.setFragmentResult(KEY, bundleOf(COMM_ID to commentId))
        openPost(postId)
    }
    
    private fun openPost(postId: Long) {
        val action = NotificationFragmentDirections.actionNotificationFragmentToOnePostFragment(postId, HIDE_DELETE_BTN)
        findNavController().navigate(action)
    }
    
    private fun openProfile(profileId: Long) {
        val action = NotificationFragmentDirections.actionNotificationFragmentToProfileFragment(profileId)
        findNavController().navigate(action)
    }
    
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}