package com.example.momenty.domain.community

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.momenty.R
import com.example.momenty.databinding.FragmentCommunityBinding
import com.example.momenty.presentation.component.UFilterChip

class CommunityFragment : Fragment(R.layout.fragment_community) {

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!



    private val adapter by lazy {
        CommunityAdapter { post ->
            val args = Bundle().apply { putLong("postId", post.id) }
            findNavController().navigate(R.id.action_toCommunityDetail, args)
        }
    }

    private fun selectFilter(selected: UFilterChip, chips: List<UFilterChip>) {
        chips.forEach { it.setChecked(it == selected) }
    }

    private val allPosts = mutableListOf<CommunityPostUiModel>()
    private var selectedCategory: CommunityCategory = CommunityCategory.ALL
    private var keyword: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCommunityBinding.bind(view)

        binding.rvCommunity.adapter = adapter

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(v.paddingLeft, top, v.paddingRight, v.paddingBottom)
            insets
        }


        allPosts.clear()
        allPosts.addAll(
            listOf(
                CommunityPostUiModel(
                    id = 1,
                    category = CommunityCategory.QNA,
                    author = "익명",
                    dateText = "2026.01.29",
                    title = "질문",
                    content = "질문 있어요! UFilterChip 선택 토글이 왜 안 되죠?",
                    likeCount = 12,
                    commentCount = 3
                ),
                CommunityPostUiModel(
                    id = 2,
                    category = CommunityCategory.INFO,
                    author = "도리",
                    dateText = "2026.01.29",
                    title = "정보공유합니다",
                    content = "정보공유합니다: 길게길게길게 일단은 길게",
                    likeCount = 7,
                    commentCount = 1
                ),
                CommunityPostUiModel(
                    id = 3,
                    category = CommunityCategory.REVIEW,
                    author = "익명",
                    dateText = "2026.01.28",
                    title = "후기",
                    content = "후기 남겨요! 너무 졸리네요\n한줄더\n한줄더\n한줄더\n한줄더\n한줄더",
                    likeCount = 20,
                    commentCount = 5
                )
            )
        )

        applyFilter()

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.btnWrite.setOnClickListener {
            findNavController().navigate(R.id.action_toCommunityWrite)
        }

        val chips = listOf(binding.chipAll, binding.chipQna, binding.chipInfo, binding.chipReview)

        selectFilter(binding.chipAll, chips)

        binding.chipAll.setOnClickListener {
            selectedCategory = CommunityCategory.ALL
            selectFilter(binding.chipAll, chips)
            applyFilter()
        }

        binding.chipQna.setOnClickListener {
            selectedCategory = CommunityCategory.QNA
            selectFilter(binding.chipQna, chips)
            applyFilter()
        }

        binding.chipInfo.setOnClickListener {
            selectedCategory = CommunityCategory.INFO
            selectFilter(binding.chipInfo, chips)
            applyFilter()
        }

        binding.chipReview.setOnClickListener {
            selectedCategory = CommunityCategory.REVIEW
            selectFilter(binding.chipReview, chips)
            applyFilter()
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                keyword = s?.toString().orEmpty()
                applyFilter()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }




    private fun applyFilter() {
        val filtered = allPosts
            .asSequence()
            .filter { selectedCategory == CommunityCategory.ALL || it.category == selectedCategory }
            .filter { keyword.isBlank() || it.content.contains(keyword, ignoreCase = true) }
            .toList()

        adapter.submitList(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
