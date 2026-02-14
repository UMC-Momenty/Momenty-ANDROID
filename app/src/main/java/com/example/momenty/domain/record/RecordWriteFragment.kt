package com.example.momenty.domain.record

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.momenty.R
import com.example.momenty.databinding.FragmentRecordWriteBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class RecordWriteFragment : Fragment(R.layout.fragment_record_write) {

    private lateinit var binding: FragmentRecordWriteBinding
    private lateinit var photoAdapter: PhotoPagerAdapter
    private var tabMediator: TabLayoutMediator? = null

    private val pickImages =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
            if (uris.isEmpty()) return@registerForActivityResult
            if (!this::binding.isInitialized) return@registerForActivityResult


            photoAdapter.submitList(uris)
            binding.photoPager.post { setupDotsIndicator(uris.size) }


        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRecordWriteBinding.bind(view)

        photoAdapter = PhotoPagerAdapter()
        binding.photoPager.adapter = photoAdapter


        setupDotsIndicator(count = 0)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val extra = resources.getDimensionPixelSize(R.dimen.top_spacing_record)

            val lp = binding.topContainer.layoutParams as ViewGroup.MarginLayoutParams
            lp.topMargin = top + extra
            binding.topContainer.layoutParams = lp
            insets
        }

        binding.btnAddPhoto.setOnClickListener {
            pickImages.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnCancel.setOnClickListener { findNavController().popBackStack() }
        binding.btnSave.setOnClickListener { findNavController().navigate(R.id.recordFragment) }
    }

    private fun setupDotsIndicator(count: Int) {
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
