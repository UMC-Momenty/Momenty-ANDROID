package com.example.momenty.domain.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momenty.databinding.FragmentCalendarAlarmBinding
import com.google.android.material.snackbar.Snackbar

class CalendarAlarmFragment: Fragment() {
    private var _binding: FragmentCalendarAlarmBinding? = null
    private val binding get() = _binding!!

    private lateinit var alarmAdapter: AlarmAdapter
    private val viewModel: AlarmViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarAlarmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        // 알람 데이터 로드
        viewModel.loadAlarms()
    }

    /**
     * RecyclerView 설정
     */
    private fun setupRecyclerView() {
        alarmAdapter = AlarmAdapter(
            onToggleChanged = { alarm, isEnabled ->
                viewModel.toggleAlarm(alarm, isEnabled)
            },
            onAlarmClick = { alarm ->
                showEditAlarmDialog(alarm)
            }
        )

        binding.rvAlarmList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = alarmAdapter
            setHasFixedSize(true)
        }
    }

    /**
     * 클릭 리스너 설정
     */
    private fun setupClickListeners() {
        // 뒤로가기 버튼
        binding.ivAlarmBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // 알람 추가 버튼
        binding.ivAlarmAdd.setOnClickListener {
            showAddAlarmDialog()
        }
    }

    /**
     * ViewModel 관찰
     */
    private fun observeViewModel() {
        viewModel.alarms.observe(viewLifecycleOwner) { alarms ->
            alarmAdapter.submitList(alarms)

            // 알람이 없을 때 안내 메시지 표시 (선택사항)
            if (alarms.isEmpty()) {
                Snackbar.make(binding.root, "등록된 알람이 없습니다", Snackbar.LENGTH_SHORT).show()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    /**
     * 알람 추가 다이얼로그
     */
    private fun showAddAlarmDialog() {
        // TODO: 알람 추가 다이얼로그 구현
        Snackbar.make(binding.root, "알람 추가 기능 구현 예정", Snackbar.LENGTH_SHORT).show()
    }

    /**
     * 알람 수정 다이얼로그
     */
    private fun showEditAlarmDialog(alarm: Alarm) {
        // TODO: 알람 수정 다이얼로그 구현
        Snackbar.make(binding.root, "${alarm.title} 수정 기능 구현 예정", Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = CalendarAlarmFragment()
    }
}