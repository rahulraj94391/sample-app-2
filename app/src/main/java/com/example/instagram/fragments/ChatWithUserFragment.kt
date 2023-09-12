package com.example.instagram.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.R
import com.example.instagram.adapters.ChatAdapter
import com.example.instagram.databinding.FragmentChatWithUserBinding

class ChatWithUserFragment : Fragment() {
    private var _binding: FragmentChatWithUserBinding? = null
    private val binding: FragmentChatWithUserBinding get() = _binding!!
    private lateinit var chatAdapter: ChatAdapter
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatAdapter = ChatAdapter()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat_with_user, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.chatRV.adapter = chatAdapter
        binding.chatRV.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
    }
}