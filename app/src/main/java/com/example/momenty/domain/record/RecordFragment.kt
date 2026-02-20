package com.example.momenty.domain.record

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momenty.R
import com.example.momenty.databinding.FragmentRecordBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecordFragment : Fragment(R.layout.fragment_record) {

    private val viewModel: RecordViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentRecordBinding.bind(view)

        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            binding.topBar.updatePadding(top = top)
            insets
        }
        ViewCompat.requestApplyInsets(binding.root)



        val adapter = RecordAdapter { item ->
            val action = RecordFragmentDirections.actionRecordToMomentDetail(
                userId = 1L,
                petId = 1L,
                momentId = item.momentId,
                createdAt = item.createdAtRaw
            )
            findNavController().navigate(action)
        }


        binding.rvRecord.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecord.adapter = adapter


        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.items.collect { list ->
                adapter.submitList(list)
            }
        }


        val userId = 1L
        val petId = 1L
        viewModel.loadMoments(userId, petId)

        binding.btnRecordCreate.setOnClickListener {
            findNavController().navigate(R.id.action_record_to_write)
        }
        binding.btnRecordMonth.setOnClickListener {
            findNavController().navigate(R.id.action_record_to_album)
        }
        binding.btnRecordStatistics.setOnClickListener {
            findNavController().navigate(R.id.action_record_to_recordStats)
        }
    }
}
