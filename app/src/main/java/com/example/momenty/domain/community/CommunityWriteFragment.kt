package com.example.momenty.domain.community

import android.os.Bundle
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momenty.R
import com.example.momenty.databinding.FragmentCommunityWriteBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommunityWriteFragment : Fragment(R.layout.fragment_community_write) {

    private lateinit var binding: FragmentCommunityWriteBinding
    private val vm: CommunityWriteViewModel by viewModels()
    private lateinit var photoAdapter: WritePhotoAdapter

    private val pickImages =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(4)) { uris ->
            if (uris.isNullOrEmpty()) return@registerForActivityResult


            photoAdapter.submitList(uris)
            vm.addPhotos(uris, max = 4)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCommunityWriteBinding.bind(view)

        binding.vm = vm
        binding.lifecycleOwner = viewLifecycleOwner

        // 뒤로가기 / 취소
        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.btnCancel.setOnClickListener { parentFragmentManager.popBackStack() }

        // 등록
        binding.btnSubmit.setOnClickListener {
            // TODO: 글 등록 처리
        }

        // 카테고리
        binding.chipAll.setOnClickListener { vm.setCategory(CommunityCategory.ALL) }
        binding.chipQna.setOnClickListener { vm.setCategory(CommunityCategory.QNA) }
        binding.chipInfo.setOnClickListener { vm.setCategory(CommunityCategory.INFO) }
        binding.chipReview.setOnClickListener { vm.setCategory(CommunityCategory.REVIEW) }

        // 사진 RecyclerView
        photoAdapter = WritePhotoAdapter(
            onAddClick = {
                pickImages.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onRemoveClick = { uri ->
                vm.removePhoto(uri)
            }
        )

        binding.rvPhotos.apply {
            adapter = photoAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }
}
