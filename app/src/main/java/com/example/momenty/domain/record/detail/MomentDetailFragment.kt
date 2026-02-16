package com.example.momenty.domain.record.detail

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.momenty.R
import com.example.momenty.databinding.FragmentMomentDetailBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MomentDetailFragment : Fragment(R.layout.fragment_moment_detail) {

    private lateinit var binding: FragmentMomentDetailBinding
    private val viewModel: MomentDetailViewModel by viewModels()


    private val args: MomentDetailFragmentArgs by navArgs()

    private val pagerAdapter = MomentPhotoPagerAdapter()
    private var tabMediator: TabLayoutMediator? = null



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMomentDetailBinding.bind(view)




        // 상단 inset
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            binding.headerContainer.updatePadding(top = top)
            insets
        }
        ViewCompat.requestApplyInsets(binding.root)

        binding.ivBack.setOnClickListener { findNavController().popBackStack() }

        binding.photoPager.adapter = pagerAdapter


        viewModel.load(
            userId = args.userId,
            petId = args.petId,
            momentId = args.momentId,
            createdAt = args.createdAt
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.ui.collect { ui ->
                if (ui == null) return@collect

                binding.tvDate.text = ui.dateText
                binding.tvMoodChip.text = ui.moodChipText
                binding.tvTitleBold.text = ui.titleText
                binding.tvBody.text = ui.bodyText

                pagerAdapter.submitList(ui.imageUrls)
                setupDots(ui.imageUrls.size)
            }
        }
    }

    private fun setupDots(count: Int) {
        binding.dotsIndicator.visibility = if (count >= 2) View.VISIBLE else View.GONE
        tabMediator?.detach()
        tabMediator = null

        if (count >= 2) {
            tabMediator = TabLayoutMediator(binding.dotsIndicator, binding.photoPager) { _, _ -> }
            tabMediator?.attach()
        }
    }

    override fun onDestroyView() {
        tabMediator?.detach()
        tabMediator = null
        super.onDestroyView()
    }
}
