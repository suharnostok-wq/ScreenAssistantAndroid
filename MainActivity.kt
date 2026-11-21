package com.example.screenassistant

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 28, 28, 28)
        }
        val title = TextView(this).apply { text = "Screen Assistant"; textSize = 26f }
        val info = TextView(this).apply {
            text = "Prototype 0.2\nLocal screen commands: read screen, tap label, scroll, and type. No AI model is connected yet."
            textSize = 16f; setPadding(0, 18, 0, 18)
        }
        status = TextView(this).apply { textSize = 15f; setPadding(0, 12, 0, 12) }
        val accessibility = Button(this).apply {
            text = "Open Accessibility settings"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }
        val help = TextView(this).apply {
            text = "After enabling Screen Assistant in Accessibility, return here. The AI bubble appears over other apps. Tap the bubble to enter a command."
            setPadding(0, 18, 0, 0)
        }
        layout.addView(title); layout.addView(info); layout.addView(status); layout.addView(accessibility); layout.addView(help)
        setContentView(layout)
    }

    override fun onResume() {
        super.onResume()
        status.text = if (isServiceEnabled()) "Accessibility: Enabled ✓\nFloating assistant should be available." else "Accessibility: Not enabled yet. Enable Screen Assistant below."
        if (isServiceEnabled()) Toast.makeText(this, "Screen Assistant aktif. Bubble tersedia di layar.", Toast.LENGTH_SHORT).show()
    }

    private fun isServiceEnabled(): Boolean {
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        return enabled.split(":").any { it.equals("$packageName/${ScreenControlService::class.java.name}", ignoreCase = true) }
    }
}
