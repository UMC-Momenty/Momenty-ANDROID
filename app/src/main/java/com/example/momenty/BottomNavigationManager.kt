package com.example.momenty

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class BottomNavigationManager(
    private val context: Context,
    private val navigationView: View
) {
    private val unselectedColor = android.graphics.Color.parseColor("#C8B4A0")
    private val selectedColor = android.graphics.Color.WHITE

    fun setOnNavigationItemSelectedListener(listener: (String) -> Unit) {
        navigationView.findViewById<LinearLayout>(R.id.nav_record)?.setOnClickListener {
            selectItem("record")
            listener("record")
        }
        navigationView.findViewById<LinearLayout>(R.id.nav_calendar)?.setOnClickListener {
            selectItem("calendar")
            listener("calendar")
        }
        navigationView.findViewById<LinearLayout>(R.id.nav_home)?.setOnClickListener {
            selectItem("home")
            listener("home")
        }
        navigationView.findViewById<LinearLayout>(R.id.nav_chatbot)?.setOnClickListener {
            selectItem("chatbot")
            listener("chatbot")
        }
        navigationView.findViewById<LinearLayout>(R.id.nav_mypage)?.setOnClickListener {
            selectItem("mypage")
            listener("mypage")
        }
    }

    fun selectItem(selectedKey: String) {
        val items = listOf(
            Triple("record", R.id.nav_record_icon, R.id.nav_record_text),
            Triple("calendar", R.id.nav_calendar_icon, R.id.nav_calendar_text),
            Triple("home", R.id.nav_home_icon, R.id.nav_home_text),
            Triple("chatbot", R.id.nav_chatbot_icon, R.id.nav_chatbot_text),
            Triple("mypage", R.id.nav_mypage_icon, R.id.nav_mypage_text)
        )

        items.forEach { (key, iconId, textId) ->
            val icon = navigationView.findViewById<ImageView>(iconId)
            val text = navigationView.findViewById<TextView>(textId)
            
            if (key == selectedKey) {
                icon?.setColorFilter(selectedColor)
                text?.setTextColor(selectedColor)
            } else {
                icon?.setColorFilter(unselectedColor)
                text?.setTextColor(unselectedColor)
            }
        }
    }
}