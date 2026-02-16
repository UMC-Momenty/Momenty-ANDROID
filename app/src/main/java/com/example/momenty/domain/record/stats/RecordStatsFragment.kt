package com.example.momenty.domain.record.stats

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.momenty.R
import com.example.momenty.databinding.FragmentRecordStatsBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RecordStatsFragment : Fragment(R.layout.fragment_record_stats) {

    private var _binding: FragmentRecordStatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RecordCalendarAdapter
    private val cal: Calendar = Calendar.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRecordStatsBinding.bind(view)

        applyStatusBarInset()

        setupWeekHeader()
        setupCalendar()
        setupClickListeners()

        renderMonth()
    }


    private fun applyStatusBarInset() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val extra = resources.getDimensionPixelSize(R.dimen.top_spacing_record)


            (binding.llHeader.layoutParams as? androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)?.let { lp ->
                lp.topMargin = top + extra
                binding.llHeader.layoutParams = lp
            }

            insets
        }
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { findNavController().popBackStack() }

        binding.ivPrev.setOnClickListener {
            cal.add(Calendar.MONTH, -1)
            renderMonth()
        }
        binding.ivNext.setOnClickListener {
            cal.add(Calendar.MONTH, +1)
            renderMonth()
        }
    }

    private fun setupCalendar() {
        adapter = RecordCalendarAdapter()
        binding.rvCalendar.apply {
            layoutManager = GridLayoutManager(requireContext(), 7)
            adapter = this@RecordStatsFragment.adapter
            itemAnimator = null
        }
    }

    private fun setupWeekHeader() {
        val weekdays = resources.getStringArray(R.array.calendar_date)
        binding.llWeek.removeAllViews()

        weekdays.forEach { day ->
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                weight = 1f
            }
            val tv = TextView(requireContext()).apply {
                layoutParams = params
                text = day
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.body_1))
                includeFontPadding = false
                typeface = ResourcesCompat.getFont(requireContext(), R.font.pretendard_semibold)
                    ?: Typeface.DEFAULT
            }
            binding.llWeek.addView(tv)
        }
    }

    private fun renderMonth() {
        // 상단 YYYY년 M월
        binding.tvYm.text = SimpleDateFormat("yyyy년 M월", Locale.KOREAN).format(cal.time)

        val year = cal.get(Calendar.YEAR)
        val month0 = cal.get(Calendar.MONTH)
        val month = month0 + 1


        val recordedDays = when (month) {
            12 -> setOf(4, 12, 17, 22, 28, 31)
            1 -> setOf(1, 2, 5, 9, 20)
            else -> setOf(3, 8, 15, 21)
        }

        val totalDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)


        val stats = MonthlyStats(
            year = year,
            month = month,
            totalDays = totalDaysInMonth,
            recordedDays = recordedDays.size,
            moodGood = 10,
            moodSoso = 1,
            moodBad = 1
        )


        binding.tvSummaryTitle.text = "${month}월 요약"
        binding.tvSummary.text = "${month}월에는 ${stats.recordRatePercent}% 기록했고, 행복한 날이 가장 많았어요."


        val happyP = stats.goodPercent
        val sosoP = stats.sosoPercent
        val sadP = stats.badPercent

        binding.tvHappyPercent.text = "${happyP}%"
        binding.tvSadPercent.text = "${sadP}%"
        binding.tvSosoPercent.text = "${sosoP}%"

        setBarWidth(binding.barHappy, happyP)
        setBarWidth(binding.barSoso, sosoP)
        setBarWidth(binding.barSad, sadP)


        val days = RecordCalendarFactory.createMonthDays(
            year = year,
            month0 = month0,
            recordedDays = recordedDays
        )
        adapter.submit(days)
    }


    private fun setBarWidth(bar: View, percent: Int) {
        binding.statChart.post {
            val axisW = binding.vAxis.width
            val maxW = binding.statChart.width - axisW - dpToPx(8) - dpToPx(40) // 여유(퍼센트 텍스트 공간)

            val safePercent = percent.coerceIn(0, 100)
            val w = (maxW * (safePercent / 100f)).toInt().coerceAtLeast(if (safePercent == 0) 0 else dpToPx(2))

            bar.layoutParams = bar.layoutParams.apply { width = w }
            bar.requestLayout()
        }
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
