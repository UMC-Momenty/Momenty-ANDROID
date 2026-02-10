package com.example.momenty.domain.community.detail

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momenty.R
import com.example.momenty.databinding.FragmentCommunityDetailBinding

class CommunityDetailFragment : Fragment(R.layout.fragment_community_detail) {

    private lateinit var binding: FragmentCommunityDetailBinding

    private val vm: CommunityDetailViewModel by viewModels()

    private lateinit var photoAdapter: CommunityDetailPhotoAdapter
    private lateinit var commentAdapter: CommunityCommentAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCommunityDetailBinding.bind(view)

        binding.vm = vm
        binding.lifecycleOwner = viewLifecycleOwner


        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(v.paddingLeft, top, v.paddingRight, v.paddingBottom)
            insets
        }

        val postId = arguments?.getLong("postId") ?: -1L
        if (postId != -1L) vm.loadDummy(postId)



        photoAdapter = CommunityDetailPhotoAdapter()
        binding.rvPhotos.apply {
            adapter = photoAdapter
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = null
        }

        commentAdapter = CommunityCommentAdapter(
            onClickMuteReply = {
                // TODO: 나중에 api연결
                Toast.makeText(requireContext(), "대댓글 알림 끄기(더미)", Toast.LENGTH_SHORT).show()
            },
            onClickDelete = { vm.deleteComment(it.id) }
        )

        binding.rvComments.adapter = commentAdapter


        binding.rvComments.apply {
            adapter = commentAdapter
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = null
        }

        vm.photos.observe(viewLifecycleOwner) { photoAdapter.submitList(it) }
        vm.comments.observe(viewLifecycleOwner) { commentAdapter.submitList(it) }

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
    }
}
