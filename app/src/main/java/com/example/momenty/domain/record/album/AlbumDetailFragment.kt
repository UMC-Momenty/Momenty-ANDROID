package com.example.momenty.domain.record.album

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.example.momenty.R
import com.example.momenty.databinding.FragmentAlbumDetailBinding
import com.example.momenty.domain.record.album.adapter.AlbumDetailPhotoAdapter
import com.example.momenty.presentation.util.GridSpacingItemDecoration

class AlbumDetailFragment : Fragment(R.layout.fragment_album_detail) {

    private lateinit var binding: FragmentAlbumDetailBinding
    private lateinit var adapter: AlbumDetailPhotoAdapter

    private val args: AlbumDetailFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAlbumDetailBinding.bind(view)


        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val extra = resources.getDimensionPixelSize(R.dimen.top_spacing_record)
            val total = topInset + extra


            (binding.topBar.layoutParams as? ConstraintLayout.LayoutParams)?.let { lp ->
                lp.topMargin = total
                binding.topBar.layoutParams = lp
            }


            binding.headerWhiteBg.layoutParams = binding.headerWhiteBg.layoutParams.apply {
                height = total
            }

            insets
        }




        binding.btnBack.setOnClickListener { findNavController().popBackStack() }


        binding.tvAlbumName.text = args.albumTitle

        adapter = AlbumDetailPhotoAdapter()

        binding.rvPhotos.apply {
            layoutManager = GridLayoutManager(requireContext(), 5)
            this.adapter = this@AlbumDetailFragment.adapter
            itemAnimator = null

            if (itemDecorationCount == 0) {
                val spacing = resources.getDimensionPixelSize(R.dimen.photo_grid_spacing)
                addItemDecoration(GridSpacingItemDecoration(5, spacing, includeEdge = false)
                )
            }
        }




        binding.tvAlbumName.text = args.albumTitle

        val photos = dummyPhotosByAlbumId(args.albumId)

        binding.tvAlbumCount.text = "${photos.size}개의 항목"
        adapter.submitList(photos)

    }

    private fun dummyPhotosByAlbumId(id: String): List<Int> {
        return when (id) {
            "choco" -> listOf(
                R.drawable.dummy1,
                R.drawable.dummy1,
                R.drawable.dummy1,
                R.drawable.dummy1,
                R.drawable.dummy1,
            )

            "menti" -> listOf(
                R.drawable.dummy2,
                R.drawable.dummy2,
                R.drawable.dummy2,
                R.drawable.dummy2,
            )

            "valen" -> listOf(
                R.drawable.dummy3,
                R.drawable.dummy3,
                R.drawable.dummy3,
                R.drawable.dummy3,
                R.drawable.dummy3,
                R.drawable.dummy3,
            )

            "tine" -> listOf(
                R.drawable.dummy1,
                R.drawable.dummy2,
                R.drawable.dummy3,
            )

            "day" -> listOf(
                R.drawable.dummy2,
                R.drawable.dummy2,
                R.drawable.dummy1,
                R.drawable.dummy3,
            )

            "sarang" -> listOf(
                R.drawable.dummy3,
                R.drawable.dummy3,
                R.drawable.dummy3,
            )

            else -> emptyList()
        }
    }
}
