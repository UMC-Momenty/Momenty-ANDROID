package com.example.momenty.domain.community.write

import android.os.Bundle
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.momenty.R
import com.example.momenty.databinding.FragmentCommunityWriteBinding
import com.example.momenty.presentation.util.GridSpacingItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.fragment.findNavController
import com.example.momenty.domain.community.CommunityCategory


@AndroidEntryPoint
class CommunityWriteFragment : Fragment(R.layout.fragment_community_write) {

    private lateinit var binding: FragmentCommunityWriteBinding
    private val vm: CommunityWriteViewModel by viewModels()

    private lateinit var photoAdapter: WritePhotoAdapter

    private val pickImages =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) { uris ->
            if (uris.isNullOrEmpty()) return@registerForActivityResult
            vm.addPhotos(uris, max = 10)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentCommunityWriteBinding.bind(view)


        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(v.paddingLeft, top, v.paddingRight, v.paddingBottom)
            insets
        }

        binding.vm = vm
        binding.lifecycleOwner = viewLifecycleOwner


        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnCancel.setOnClickListener { findNavController().popBackStack() }

        binding.btnSubmit.setOnClickListener { findNavController().popBackStack() }


        setupCategoryChips()


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


        setupPhotoRecycler()


        vm.photos.observe(viewLifecycleOwner) { list ->
            photoAdapter.submitList(list)
        }
    }

    private fun setupPhotoRecycler() {
        val spacingPx = resources.getDimensionPixelSize(R.dimen.photo_grid_spacing)

        binding.rvPhotos.apply {
            adapter = photoAdapter
            layoutManager = GridLayoutManager(requireContext(), 4)
            itemAnimator = null


            val alreadyAdded = (0 until itemDecorationCount).any { idx ->
                getItemDecorationAt(idx) is GridSpacingItemDecoration
            }
            if (!alreadyAdded) {
                addItemDecoration(
                    GridSpacingItemDecoration(
                        spanCount = 4,
                        spacing = spacingPx,
                        includeEdge = false
                    )
                )
            }
        }
    }

    private fun setupCategoryChips() {
        val chips = listOf(
            binding.chipAll,
            binding.chipQna,
            binding.chipInfo,
            binding.chipReview
        )

        fun select(index: Int, category: CommunityCategory) {
            chips.forEachIndexed { i, chip -> chip.setChecked(i == index) }
            vm.setCategory(category)
        }

        select(0, CommunityCategory.ALL)

        binding.chipAll.setOnClickListener { select(0, CommunityCategory.ALL) }
        binding.chipQna.setOnClickListener { select(1, CommunityCategory.QNA) }
        binding.chipInfo.setOnClickListener { select(2, CommunityCategory.INFO) }
        binding.chipReview.setOnClickListener { select(3, CommunityCategory.REVIEW) }
    }
}
