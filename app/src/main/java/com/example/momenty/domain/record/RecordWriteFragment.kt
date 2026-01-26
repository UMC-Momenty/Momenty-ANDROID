package com.example.momenty.domain.record


import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.momenty.R
import com.example.momenty.databinding.FragmentRecordWriteBinding
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2


class RecordWriteFragment : Fragment(R.layout.fragment_record_write) {

    private lateinit var binding: FragmentRecordWriteBinding
    private lateinit var photoAdapter: PhotoPagerAdapter
    private var tabMediator: TabLayoutMediator? = null

    private val pickImages =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
            if (uris.isEmpty()) return@registerForActivityResult
            if (!this::binding.isInitialized) return@registerForActivityResult

            photoAdapter.submitList(uris)


            tabMediator?.detach()
            tabMediator = TabLayoutMediator(binding.dotsIndicator, binding.photoPager) { _, _ -> }
            tabMediator?.attach()
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRecordWriteBinding.bind(view)

        photoAdapter = PhotoPagerAdapter()
        binding.photoPager.adapter = photoAdapter


        tabMediator = TabLayoutMediator(binding.dotsIndicator, binding.photoPager) { _, _ -> }
        tabMediator?.attach()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(v.paddingLeft, top, v.paddingRight, v.paddingBottom)
            insets
        }

        binding.btnAddPhoto.setOnClickListener {
            pickImages.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnCancel.setOnClickListener { findNavController().popBackStack() }

        binding.btnSave.setOnClickListener {
            findNavController().navigate(R.id.recordFragment)
        }
    }

    override fun onDestroyView() {
        tabMediator?.detach()
        tabMediator = null
        super.onDestroyView()
    }
}
