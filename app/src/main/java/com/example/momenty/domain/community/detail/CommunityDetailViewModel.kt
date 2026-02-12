package com.example.momenty.domain.community.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.momenty.R

class CommunityDetailViewModel : ViewModel() {

    val post = MutableLiveData(
        CommunityDetailPostUiModel(
            categoryText = "질문",
            author = "익명",
            dateText = "2024.01.15",
            title = "제목",
            content = "커뮤니티 텍스트란\n5줄 제한\n5줄 이상 넘어갈 시 더보기 처리\n.\n.\n.",
            likeCount = 12
        )
    )


    val postUi = MediatorLiveData<CommunityDetailPostUiModel>().apply {
        addSource(post) { value = it }
    }

    val photos = MutableLiveData<List<Int>>(emptyList())

    private val _comments = MutableLiveData<List<CommunityCommentUiModel>>(emptyList())
    val comments: LiveData<List<CommunityCommentUiModel>> = _comments

    val commentInput = MutableLiveData("")

    val photoCount = MediatorLiveData<Int>().apply {
        addSource(photos) { value = it.size }
    }

    val commentCount = MediatorLiveData<Int>().apply {
        addSource(_comments) { value = it.size }
    }

    val canSendComment = MediatorLiveData<Boolean>().apply {
        addSource(commentInput) { value = !it.isNullOrBlank() }
    }


    fun loadDummy(postId: Long) {
        when (postId) {
            1L -> {
                post.value = CommunityDetailPostUiModel(
                    categoryText = "질문",
                    author = "익명",
                    dateText = "2026.01.29",
                    title = "질문",
                    content = "질문 있어요! UFilterChip 선택 토글이 왜 안 되죠?",
                    likeCount = 12
                )
                photos.value = listOf(
                    R.drawable.ic_launcher_foreground,
                    R.drawable.ic_launcher_foreground
                )
            }

            2L -> {
                post.value = CommunityDetailPostUiModel(
                    categoryText = "정보",
                    author = "도리",
                    dateText = "2026.01.29",
                    title = "정보공유합니다",
                    content = "정보공유합니다: 길게길게길게 일단은 길게",
                    likeCount = 7
                )
                photos.value = emptyList()
            }

            3L -> {
                post.value = CommunityDetailPostUiModel(
                    categoryText = "후기",
                    author = "익명",
                    dateText = "2026.01.28",
                    title = "후기",
                    content = "후기 남겨요! 너무 졸리네요\n한줄더\n한줄더\n한줄더\n한줄더\n한줄더",
                    likeCount = 20
                )
                photos.value = listOf(
                    R.drawable.ic_launcher_foreground
                )
            }
        }
    }

    fun addDummyComment(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val newItem = CommunityCommentUiModel(
            id = System.currentTimeMillis(),
            author = "익명",
            content = trimmed,
            dateText = "2026.02.08"
        )

        _comments.value = _comments.value.orEmpty() + newItem
        commentInput.value = ""
    }

    fun deleteComment(id: Long) {
        _comments.value = _comments.value.orEmpty().filterNot { it.id == id }
    }


    fun onClickSendComment() {
        addDummyComment(commentInput.value.orEmpty())
    }
}
