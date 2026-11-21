package com.example.screenassistant

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class ScreenControlService : AccessibilityService() {
    private var wm: WindowManager? = null
    private var bubble: TextView? = null
    private var panel: LinearLayout? = null
    private var outputView: TextView? = null

    override fun onServiceConnected() { showBubble() }
    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {}
    override fun onInterrupt() {}

    private fun showBubble() {
        if (bubble != null) return
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        bubble = TextView(this).apply {
            text = "AI"; textSize = 18f; gravity = Gravity.CENTER
            setTextColor(0xffffffff.toInt()); setBackgroundColor(0xff3949ab.toInt())
            setPadding(22, 16, 22, 16); contentDescription = "Screen Assistant floating button"
            setOnClickListener { showPanel() }
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.END; x = 18; y = 240 }
        try { wm?.addView(bubble, params) } catch (_: Exception) { bubble = null }
    }

    private fun showPanel() {
        if (panel != null) { closePanel(); return }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(18, 18, 18, 18); setBackgroundColor(0xfff2f3f8.toInt())
        }
        val heading = TextView(this).apply { text = "Screen Assistant 0.2\nLocal commands • confirm before actions"; textSize = 17f }
        val command = EditText(this).apply { hint = "read screen / tap: label / scroll down / type: text" }
        val run = Button(this).apply { text = "Review command" }
        val output = TextView(this).apply { text = "Try: read screen, tap: Settings, scroll down, scroll up, type: hello"; setPadding(0, 12, 0, 12) }
        outputView = output
        val close = Button(this).apply { text = "Close"; setOnClickListener { closePanel() } }
        run.setOnClickListener { reviewCommand(command.text.toString(), output) }
        root.addView(heading); root.addView(command); root.addView(run)
        root.addView(ScrollView(this).apply { addView(output) }, LinearLayout.LayoutParams(320, 220))
        root.addView(close)
        panel = root
        val params = WindowManager.LayoutParams(
            dp(340), WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL; y = dp(80) }
        try { wm?.addView(root, params) } catch (_: Exception) { panel = null }
    }

    private fun reviewCommand(raw: String, output: TextView) {
        val trimmed = raw.trim()
        val cmd = trimmed.lowercase()
        when {
            cmd == "read screen" || cmd == "baca layar" -> output.text = readScreenText()
            cmd == "scroll down" || cmd == "scroll bawah" -> confirm("Scroll layar ke bawah?", output) { scroll(false) }
            cmd == "scroll up" || cmd == "scroll atas" -> confirm("Scroll layar ke atas?", output) { scroll(true) }
            trimmed.startsWith("tap:", true) || trimmed.startsWith("klik:", true) -> {
                val label = trimmed.substringAfter(":").trim()
                if (label.isBlank()) output.text = "Isi label tombol/teks setelah tap:, contoh: tap: Settings"
                else confirm("Cari dan tekan elemen bernama:\n\n$label ?", output) { tapByLabel(label, output) }
            }
            trimmed.startsWith("type:", true) -> {
                val text = trimmed.substringAfter(":").trim()
                confirm("Ketik teks pada kolom yang sedang aktif?\n\n$text", output) { typeFocused(text, output) }
            }
            else -> output.text = "Perintah belum dikenal.\nGunakan:\n• read screen\n• tap: label\n• scroll down / scroll up\n• type: teks"
        }
    }

    private fun confirm(message: String, output: TextView, action: () -> Unit) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Konfirmasi tindakan")
            .setMessage(message)
            .setNegativeButton("Batal", null)
            .setPositiveButton("Jalankan") { _, _ -> action() }
            .show()
    }

    private fun readScreenText(): String {
        val root = rootInActiveWindow ?: return "Tidak dapat membaca layar aktif. Pastikan Accessibility aktif dan buka aplikasi yang ingin dibaca."
        val lines = mutableListOf<String>()
        collectReadableText(root, lines)
        return if (lines.isEmpty()) "Tidak menemukan teks yang dapat dibaca (kolom sensitif atau layar kosong mungkin disembunyikan)."
        else "Teks layar yang terbaca:\n" + lines.distinct().take(60).joinToString("\n")
    }

    private fun collectReadableText(node: AccessibilityNodeInfo, lines: MutableList<String>) {
        if (node.isPassword) return
        if (!node.isEditable) {
            val text = node.text?.toString()?.trim().orEmpty()
            val desc = node.contentDescription?.toString()?.trim().orEmpty()
            if (text.isNotBlank()) lines.add(text)
            else if (desc.isNotBlank()) lines.add(desc)
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collectReadableText(child, lines)
        }
    }

    private fun tapByLabel(label: String, output: TextView) {
        val root = rootInActiveWindow
        if (root == null) { output.text = "Layar aktif tidak dapat diakses."; return }
        val target = findTarget(root, label)
        if (target == null) { output.text = "Tidak menemukan elemen dengan label '$label'."; return }
        var clickable: AccessibilityNodeInfo? = target
        while (clickable != null && !clickable.isClickable) clickable = clickable.parent
        val success = clickable?.performAction(AccessibilityNodeInfo.ACTION_CLICK) ?: false
        output.text = if (success) "Tap berhasil dikirim ke '$label'." else "Elemen ditemukan, tetapi tidak dapat ditekan."
    }

    private fun findTarget(node: AccessibilityNodeInfo, label: String): AccessibilityNodeInfo? {
        if (!node.isPassword) {
            val text = node.text?.toString().orEmpty()
            val desc = node.contentDescription?.toString().orEmpty()
            if (text.equals(label, ignoreCase = true) || desc.equals(label, ignoreCase = true)) return node
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findTarget(child, label)
            if (found != null) return found
        }
        return null
    }

    private fun scroll(up: Boolean) {
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm?.defaultDisplay?.getRealMetrics(metrics)
        val x = metrics.widthPixels * 0.5f
        val startY = if (up) metrics.heightPixels * 0.72f else metrics.heightPixels * 0.30f
        val endY = if (up) metrics.heightPixels * 0.30f else metrics.heightPixels * 0.72f
        val path = Path().apply { moveTo(x, startY); lineTo(x, endY) }
        val gesture = GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(path, 0, 420)).build()
        val accepted = dispatchGesture(gesture, null, null)
        outputView?.text = if (accepted) "Gestur scroll dikirim." else "Gestur scroll gagal dikirim."
    }

    private fun typeFocused(text: String, output: TextView) {
        val root = rootInActiveWindow ?: run { output.text = "Tidak ada layar aktif."; return }
        val focused = findFocusedEditable(root)
        if (focused == null) { output.text = "Tidak ada kolom teks aktif."; return }
        val args = Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text) }
        val success = focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        output.text = if (success) "Teks berhasil dikirim ke kolom aktif." else "Aplikasi menolak aksi mengetik."
    }

    private fun findFocusedEditable(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isFocused && node.isEditable && !node.isPassword) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findFocusedEditable(child)
            if (found != null) return found
        }
        return null
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private fun closePanel() { try { panel?.let { wm?.removeView(it) } } catch (_: Exception) {}; panel = null; outputView = null }
    override fun onDestroy() { closePanel(); try { bubble?.let { wm?.removeView(it) } } catch (_: Exception) {}; bubble = null; super.onDestroy() }
}
