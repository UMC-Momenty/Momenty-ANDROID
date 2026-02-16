package com.example.momenty.presentation.component

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Checkable
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.momenty.R
import com.example.momenty.databinding.ViewFilterChipBinding
import android.content.res.ColorStateList

class UFilterChip @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), Checkable {

    private val binding =
        ViewFilterChipBinding.inflate(LayoutInflater.from(context), this, true)

    private var _checked = false

    private var radiusPx = 999f * resources.displayMetrics.density
    private var strokeWidthPx = (1f * resources.displayMetrics.density).toInt()

    private var bgNormal = ContextCompat.getColor(context, R.color.white)
    private var bgChecked = ContextCompat.getColor(context, R.color.primary)

    private var strokeNormal = ContextCompat.getColor(context, R.color.white)
    private var strokeChecked = ContextCompat.getColor(context, R.color.primary)

    private var textNormal = ContextCompat.getColor(context, R.color.neutral_text)
    private var textChecked = ContextCompat.getColor(context, R.color.white)

    private var iconResId: Int = 0

    init {
        isClickable = true
        isFocusable = true

        val a = context.obtainStyledAttributes(attrs, R.styleable.UFilterChip, defStyleAttr, 0)

        binding.tvText.text = a.getString(R.styleable.UFilterChip_text).orEmpty()
        _checked = a.getBoolean(R.styleable.UFilterChip_checked, false)

        radiusPx = a.getDimension(R.styleable.UFilterChip_chipRadius, radiusPx)
        strokeWidthPx = a.getDimensionPixelSize(R.styleable.UFilterChip_chipStrokeWidth, strokeWidthPx)

        bgNormal = a.getColor(R.styleable.UFilterChip_chipBgColor, bgNormal)
        bgChecked = a.getColor(R.styleable.UFilterChip_chipCheckedBgColor, bgChecked)

        strokeNormal = a.getColor(R.styleable.UFilterChip_chipStrokeColor, strokeNormal)
        strokeChecked = a.getColor(R.styleable.UFilterChip_chipCheckedStrokeColor, strokeChecked)

        textNormal = a.getColor(R.styleable.UFilterChip_chipTextColor, textNormal)
        textChecked = a.getColor(R.styleable.UFilterChip_chipCheckedTextColor, textChecked)

        iconResId = a.getResourceId(R.styleable.UFilterChip_chipIcon, 0)

        a.recycle()

        val label = binding.tvText.text?.toString().orEmpty().trim()

        if (label.isEmpty()) {
            binding.tvText.visibility = GONE

            (binding.ivIcon.layoutParams as? android.widget.LinearLayout.LayoutParams)?.let { lp ->
                lp.marginEnd = 0
                binding.ivIcon.layoutParams = lp
            }
            (binding.root as? android.widget.LinearLayout)?.gravity =
                android.view.Gravity.CENTER
        } else {
            binding.tvText.visibility = VISIBLE

            (binding.ivIcon.layoutParams as? android.widget.LinearLayout.LayoutParams)?.let { lp ->
                lp.marginEnd = if (binding.ivIcon.visibility == VISIBLE) {
                    (6f * resources.displayMetrics.density).toInt()
                } else 0
                binding.ivIcon.layoutParams = lp
            }
        }

        binding.ivIcon.isVisible = iconResId != 0
        if (iconResId != 0) binding.ivIcon.setImageResource(iconResId)

        setOnClickListener { toggle() }
        updateStyle()
    }

    override fun isChecked() = _checked

    override fun setChecked(checked: Boolean) {
        if (_checked == checked) return
        _checked = checked
        updateStyle()
    }

    override fun toggle() = setChecked(!_checked)



    private fun updateStyle() {
        val bg = if (_checked) bgChecked else bgNormal
        val stroke = if (_checked) strokeChecked else strokeNormal
        val txt = if (_checked) textChecked else textNormal

        binding.root.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radiusPx
            setColor(bg)
            setStroke(strokeWidthPx, stroke)
        }

        binding.tvText.setTextColor(txt)


        if (binding.ivIcon.visibility == VISIBLE) {
            val iconColor = if (_checked) ContextCompat.getColor(context, R.color.white)
            else ContextCompat.getColor(context, R.color.primary)
            binding.ivIcon.imageTintList = ColorStateList.valueOf(iconColor)
        }
    }

}
