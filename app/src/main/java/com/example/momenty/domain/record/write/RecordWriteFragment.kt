package com.example.momenty.domain.record.write

import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.momenty.R
import com.example.momenty.data.remote.moment.EmotionDto
import com.example.momenty.databinding.FragmentRecordWriteBinding
import com.example.momenty.domain.record.RecordViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.navOptions


@AndroidEntryPoint
class RecordWriteFragment : Fragment(R.layout.fragment_record_write) {

    private lateinit var binding: FragmentRecordWriteBinding

    private lateinit var pagerAdapter: PhotoPagerAdapter
    private lateinit var thumbAdapter: RecordThumbAdapter

    private var tabMediator: TabLayoutMediator? = null

    private val photos = mutableListOf<Uri>()
    private val MAX_COUNT = 5


    private val viewModel: RecordViewModel by viewModels()

    private enum class Mood {
        GOOD, SOSO, BAD;

        fun toEmotionDto(): EmotionDto = when (this) {
            GOOD -> EmotionDto.HAPPINESS
            SOSO -> EmotionDto.NEUTRAL
            BAD -> EmotionDto.SADNESS
        }
    }

    private var selectedMood: Mood = Mood.SOSO

    private val pickImages =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(MAX_COUNT)) { uris ->
            if (uris.isEmpty() || !this::binding.isInitialized) return@registerForActivityResult

            val merged = (photos + uris).distinct().take(MAX_COUNT)
            photos.clear()
            photos.addAll(merged)

            renderPhotos(moveToLast = true)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRecordWriteBinding.bind(view)

        pagerAdapter = PhotoPagerAdapter()
        binding.photoPager.adapter = pagerAdapter

        thumbAdapter = RecordThumbAdapter(
            onAddClick = { launchPickerIfCanAdd() },
            onRemoveClick = { uri ->
                photos.remove(uri)
                renderPhotos(moveToLast = false)
            }
        )

        binding.rvThumbs.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = thumbAdapter
            itemAnimator = null
            isNestedScrollingEnabled = false


            if (itemDecorationCount == 0) {
                val spacing = resources.getDimensionPixelSize(R.dimen.thumb_grid_spacing) // 예: 8dp
                addItemDecoration(GridSpacingItemDecoration(spanCount = 4, spacingPx = spacing))
            }
        }


        setupMoodToggle()


        renderPhotos(moveToLast = false)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val extra = resources.getDimensionPixelSize(R.dimen.top_spacing_record)

            (binding.topContainer.layoutParams as? ConstraintLayout.LayoutParams)?.let {
                it.topMargin = top + extra
                binding.topContainer.layoutParams = it
            }
            insets
        }

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnCancel.setOnClickListener { findNavController().popBackStack() }


        binding.btnSave.setOnClickListener {
            val userId = 1L
            val petId = 1L

            val emotion = selectedMood.toEmotionDto()
            val content = binding.etContent.text?.toString().orEmpty()

            viewModel.createMoment(
                userId = userId,
                petId = petId,
                emotion = emotion,
                content = content,
                imageUris = photos.toList(),
                contentResolver = requireContext().contentResolver,
                onSuccess = {
                    val options = navOptions {
                        popUpTo(R.id.recordFragment) { inclusive = false }
                        launchSingleTop = true
                    }
                    findNavController().navigate(R.id.recordFragment, null, options)
                },
                onFail = { e ->
                    e.printStackTrace()

                }
            )
        }
    }

    private fun launchPickerIfCanAdd() {
        if (photos.size >= MAX_COUNT) return
        pickImages.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun renderPhotos(moveToLast: Boolean) {
        val list = photos.toList()

        pagerAdapter.submitList(list) {
            setupDotsIndicator(list.size)
            if (moveToLast && list.isNotEmpty()) {
                binding.photoPager.setCurrentItem(list.lastIndex, true)
            }
        }

        thumbAdapter.submitList(list)
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


    private fun setupMoodToggle() = with(binding) {

        fun renderMoodIcons() {
            btnHappy.setImageResource(
                if (selectedMood == Mood.GOOD) R.drawable.ic_mood_good_on
                else R.drawable.ic_mood_good
            )
            btnNormal.setImageResource(
                if (selectedMood == Mood.SOSO) R.drawable.ic_mood_soso_on
                else R.drawable.ic_mood_soso
            )
            btnSad.setImageResource(
                if (selectedMood == Mood.BAD) R.drawable.ic_mood_bad_on
                else R.drawable.ic_mood_bad
            )
        }

        btnHappy.setOnClickListener {
            selectedMood = Mood.GOOD
            renderMoodIcons()
        }
        btnNormal.setOnClickListener {
            selectedMood = Mood.SOSO
            renderMoodIcons()
        }
        btnSad.setOnClickListener {
            selectedMood = Mood.BAD
            renderMoodIcons()
        }

        renderMoodIcons()
    }

    override fun onDestroyView() {
        tabMediator?.detach()
        tabMediator = null
        super.onDestroyView()
    }


    private class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacingPx: Int
    ) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            if (position == RecyclerView.NO_POSITION) return

            val column = position % spanCount


            outRect.left = column * spacingPx / spanCount
            outRect.right = spacingPx - (column + 1) * spacingPx / spanCount


            if (position >= spanCount) {
                outRect.top = spacingPx
            }
        }
    }
}

