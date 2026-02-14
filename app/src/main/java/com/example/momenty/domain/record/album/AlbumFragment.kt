package com.example.momenty.domain.record.album

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.momenty.R
import com.example.momenty.databinding.FragmentAlbumBinding
import com.example.momenty.domain.record.album.adapter.AlbumAdapter
import com.example.momenty.domain.record.album.model.AlbumType
import com.example.momenty.domain.record.album.model.AlbumUiModel

class AlbumFragment : Fragment(R.layout.fragment_album) {

    private lateinit var binding: FragmentAlbumBinding
    private lateinit var adapter: AlbumAdapter

    private enum class Filter { ALL, CAT, DOG }
    private var selectedFilter: Filter = Filter.ALL

    private val allAlbums = mutableListOf<AlbumUiModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAlbumBinding.bind(view)



        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val extra = resources.getDimensionPixelSize(R.dimen.top_spacing_record)
            val total = topInset + extra


            (binding.topBar.layoutParams as? ConstraintLayout.LayoutParams)?.let { lp ->
                lp.topMargin = total
                binding.topBar.layoutParams = lp
            }


            (binding.headerWhiteBg.layoutParams as? ConstraintLayout.LayoutParams)?.let { lp ->
                lp.topMargin = 0
                binding.headerWhiteBg.layoutParams = lp
            }

            insets
        }



        adapter = AlbumAdapter { album ->
            val action = AlbumFragmentDirections
                .actionAlbumToAlbumDetail(albumId = album.id, albumTitle = album.title)
            findNavController().navigate(action)
        }

        binding.rvAlbums.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = this@AlbumFragment.adapter
            itemAnimator = null
        }

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }


        allAlbums.clear()
        allAlbums.addAll(dummyAlbums())


        setupSingleSelectChips()
        applyFilter()
    }

    private fun setupSingleSelectChips() = with(binding) {
        setChipChecked(Filter.ALL)

        chipAll.setOnClickListener {
            selectedFilter = Filter.ALL
            setChipChecked(selectedFilter)
            applyFilter()
        }

        chipCat.setOnClickListener {
            selectedFilter = Filter.CAT
            setChipChecked(selectedFilter)
            applyFilter()
        }

        chipDog.setOnClickListener {
            selectedFilter = Filter.DOG
            setChipChecked(selectedFilter)
            applyFilter()
        }
    }

    private fun setChipChecked(filter: Filter) = with(binding) {
        chipAll.isChecked = (filter == Filter.ALL)
        chipCat.isChecked = (filter == Filter.CAT)
        chipDog.isChecked = (filter == Filter.DOG)
    }

    private fun applyFilter() {
        val filtered = when (selectedFilter) {
            Filter.ALL -> allAlbums
            Filter.CAT -> allAlbums.filter { it.type == AlbumType.CAT }
            Filter.DOG -> allAlbums.filter { it.type == AlbumType.DOG }
        }
        adapter.submitList(filtered)
    }

    private fun dummyAlbums(): List<AlbumUiModel> {


        return listOf(
            AlbumUiModel("choco", "초코", 25, R.drawable.dummy1, AlbumType.DOG),
            AlbumUiModel("menti", "먼티", 12, R.drawable.dummy2, AlbumType.CAT),
            AlbumUiModel("valen", "발렌", 56, R.drawable.dummy3, AlbumType.CAT),
            AlbumUiModel("tine", "타인", 3, R.drawable.dummy1, AlbumType.CAT),
            AlbumUiModel("day", "데이", 34, R.drawable.dummy2, AlbumType.DOG),
            AlbumUiModel("sarang", "사랑", 24, R.drawable.dummy3, AlbumType.DOG),
        )
    }
}
