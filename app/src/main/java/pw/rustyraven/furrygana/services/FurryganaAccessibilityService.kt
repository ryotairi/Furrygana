package pw.rustyraven.furrygana.services

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.graphics.Rect
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import com.atilika.kuromoji.ipadic.Tokenizer
import pw.rustyraven.furrygana.isKanji
import pw.rustyraven.furrygana.katakanaToHiragana

@SuppressLint("AccessibilityPolicy")
class FurryganaAccessibilityService : AccessibilityService() {
    private lateinit var windowManager: WindowManager
    private lateinit var rootContainer: FrameLayout
    private lateinit var tokenizer: Tokenizer

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        tokenizer = Tokenizer()

        rootContainer = FrameLayout(this)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )
        windowManager.addView(rootContainer, params)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return;
        if (intArrayOf(
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            ).any { it == event.eventType }
        ) return;

        rootContainer.removeAllViews()

        val rootNode = rootInActiveWindow ?: return
        traverseNode(rootNode)

    }

    fun traverseNode(node: AccessibilityNodeInfo?) {
        if (node == null) return;
        val text = node.text?.toString()

        val bounds = Rect()
        node.getBoundsInScreen(bounds)

        if (!text.isNullOrBlank() && node.isVisibleToUser) {
//            val info = """
//            Text: $text
//            Position: X=${bounds.left} Y=${bounds.top}
//            Size: Width=${bounds.width()} Height=${bounds.height()}
//            ---
//        """.trimIndent()
//            Log.d("AccessibilityScanner", info)

            if (text.any { it.isKanji() }) {
//                Log.d("AccessibilityScanner", "Found kanji: $text")
                val tokens = tokenizer.tokenize(text)
                var finalText = ""
                for (token in tokens) {
//                    Log.d("AccessibilityScanner", "${token.surface}: ${token.reading}")
                    finalText += if (token.reading == "*") {
                        token.surface
                    } else {
                        token.reading.katakanaToHiragana()
                    }
                }
                drawText(finalText.trim(), bounds.left, bounds.top - bounds.height())
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            traverseNode(child)
        }
    }

    private fun drawText(text: String, x: Int, y: Int) {
        val tv = TextView(this).apply {
            this.text = text
            this.x = x.toFloat()
            this.y = (y - 50).toFloat() // позиция внутри контейнера
            this.setTextColor(Color.RED)
            this.setBackgroundColor(Color.BLACK)
            setPadding(8, 4, 8, 4)
        }
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        rootContainer.addView(tv, params)
    }

    override fun onInterrupt() {
        rootContainer.removeAllViews()
    }
}