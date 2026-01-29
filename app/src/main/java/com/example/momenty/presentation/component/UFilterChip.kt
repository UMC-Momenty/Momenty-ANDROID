package com.example.momenty.presentation.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.example.momenty.R
import com.example.momenty.databinding.ViewFilterChipBinding

class UFilterChip @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding =
        ViewFilterChipBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        isClickable = true
        isFocusable = true

        val a = context.obtainStyledAttributes(attrs, R.styleable.UFilterChip)
        binding.tvText.text = a.getString(R.styleable.UFilterChip_text) ?: ""
        a.recycle()
    }

    fun setText(text: String) {
        binding.tvText.text = text
    }
}
