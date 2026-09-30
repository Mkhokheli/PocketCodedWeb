package com.mkhokheli.pocketcodedweb

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.Editable
import android.text.InputType
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.inputmethod.BaseInputConnection
import android.widget.EditText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.abs

/** Platform DynamicLayout avoids thousands of Compose paragraph layouts for large documents. */
@Composable
internal fun LargeFileCodeEditor(
    value: TextFieldValue,
    palette: WebPalette,
    modifier: Modifier,
    highlight: (String) -> AnnotatedString,
    onValueChange: (TextFieldValue) -> Unit,
) {
    AndroidView(
        modifier = modifier,
        factory = { LargeDocumentEditText(it) },
        update = { it.bind(value, palette, highlight, onValueChange) },
        onRelease = { it.dispose() },
    )
}

internal class LargeDocumentEditText(context: Context) : EditText(context) {
    private var applying = false
    private var source = ""
    private var notifyChange: ((TextFieldValue) -> Unit)? = null
    private var highlightSource: ((String) -> AnnotatedString)? = null
    private var currentPalette: WebPalette? = null
    private val gutterPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val syntaxSpans = ArrayList<ForegroundColorSpan>()
    private var downX = 0f
    private var downY = 0f
    private var dragged = false
    private var measuredWidthSpec: Int? = null
    private val highlightTask = Runnable { highlightVisibleSection() }

    init {
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        typeface = Typeface.MONOSPACE
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        setHorizontallyScrolling(true)
        breakStrategy = android.text.Layout.BREAK_STRATEGY_SIMPLE
        hyphenationFrequency = android.text.Layout.HYPHENATION_FREQUENCY_NONE
        // A drag must not summon the keyboard and turn later scroll gestures into typing.
        showSoftInputOnFocus = false
        gravity = android.view.Gravity.TOP or android.view.Gravity.START
        includeFontPadding = false
        setLineSpacing(0f, 1.25f)
        background = null
        val density = resources.displayMetrics.density
        setPadding((60 * density).toInt(), (4 * density).toInt(), (12 * density).toInt(), (20 * density).toInt())
        isVerticalScrollBarEnabled = true
        isHorizontalScrollBarEnabled = true
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(text: Editable?) {
                if (applying || text == null) return
                source = text.toString()
                publishValue()
                scheduleHighlight()
            }
        })
    }

    fun bind(value: TextFieldValue, palette: WebPalette, highlight: (String) -> AnnotatedString, changed: (TextFieldValue) -> Unit) {
        notifyChange = changed
        highlightSource = highlight
        val newPalette = currentPalette != palette
        if (newPalette) {
            currentPalette = palette
            setBackgroundColor(palette.panel.toArgb())
            setTextColor(palette.text.toArgb())
            setHintTextColor(palette.subtleText.toArgb())
        }
        applying = true
        try {
            if (source !== value.text && source != value.text) {
                val x = scrollX
                val y = scrollY
                source = value.text
                setText(source)
                syntaxSpans.clear()
                scrollTo(x, y)
                scheduleHighlight()
            }
            val start = value.selection.start.coerceIn(0, length())
            val end = value.selection.end.coerceIn(0, length())
            if (selectionStart != start || selectionEnd != end) setSelection(start, end)
        } finally { applying = false }
        if (newPalette) scheduleHighlight()
    }

    private fun publishValue() {
        if (applying || selectionStart < 0 || selectionEnd < 0) return
        val editable = text ?: return
        val start = BaseInputConnection.getComposingSpanStart(editable)
        val end = BaseInputConnection.getComposingSpanEnd(editable)
        notifyChange?.invoke(TextFieldValue(source, TextRange(selectionStart, selectionEnd),
            if (start >= 0 && end >= start) TextRange(start, end) else null))
    }

    override fun onSelectionChanged(start: Int, end: Int) {
        super.onSelectionChanged(start, end)
        publishValue()
    }

    // Keep the user's viewport when the cursor, IME or a completion changes selection.
    override fun bringPointIntoView(offset: Int): Boolean = false
    override fun requestRectangleOnScreen(rectangle: Rect?, immediate: Boolean): Boolean = false

    override fun onScrollChanged(x: Int, y: Int, oldX: Int, oldY: Int) {
        super.onScrollChanged(x, y, oldX, oldY)
        scheduleHighlight()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_UP) {
            val slop = ViewConfiguration.get(context).scaledTouchSlop
            dragged = dragged || abs(event.x - downX) > slop || abs(event.y - downY) > slop
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x; downY = event.y; dragged = false
                removeCallbacks(highlightTask)
            }
            MotionEvent.ACTION_MOVE -> {
                val slop = ViewConfiguration.get(context).scaledTouchSlop
                if (abs(event.x - downX) > slop || abs(event.y - downY) > slop) dragged = true
            }
            MotionEvent.ACTION_UP -> if (dragged) scheduleHighlight() else post(highlightTask)
            MotionEvent.ACTION_CANCEL -> scheduleHighlight()
        }
        val handled = super.onTouchEvent(event)
        if (event.actionMasked == MotionEvent.ACTION_UP && !dragged) {
            requestFocus()
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager)
                .showSoftInput(this, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }
        return handled
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // TextView compares its non-ellipsized DynamicLayout width (VERY_WIDE for
        // horizontal scrolling) with the viewport on every measure. That rebuilds
        // every paragraph even when the viewport and text layout are unchanged.
        // Editable's DynamicLayout already reflows actual edits. Reuse it when
        // Compose supplies an exact viewport of the same width.
        if (layout != null && measuredWidthSpec == widthMeasureSpec &&
            MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY &&
            MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
            return
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        measuredWidthSpec = widthMeasureSpec
    }

    private fun scheduleHighlight() {
        removeCallbacks(highlightTask)
        postDelayed(highlightTask, EDITOR_SCROLL_HIGHLIGHT_IDLE_MS)
    }

    private fun highlightVisibleSection() {
        val layout = layout ?: return
        val editable = text ?: return
        val first = layout.getLineForVertical(scrollY)
        val last = layout.getLineForVertical(scrollY + height)
        val visibleStart = layout.getOffsetForHorizontal(first, scrollX.toFloat())
        val visibleEnd = layout.getOffsetForHorizontal(last, (scrollX + width - totalPaddingLeft - totalPaddingRight).toFloat())
        val window = editorHighlightWindow(source, visibleStart, visibleEnd, 2_000)
        val highlighted = highlightSource?.invoke(source.substring(window.start, window.end)) ?: return
        applying = true
        try {
            syntaxSpans.forEach(editable::removeSpan)
            syntaxSpans.clear()
            highlighted.spanStyles.forEach { range ->
                if (range.start < range.end) {
                    val span = ForegroundColorSpan(range.item.color.toArgb())
                    editable.setSpan(span, window.start + range.start, window.start + range.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    syntaxSpans.add(span)
                }
            }
        } finally { applying = false }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val palette = currentPalette ?: return
        val layout = layout ?: return
        val first = layout.getLineForVertical(scrollY)
        val last = layout.getLineForVertical(scrollY + height)
        canvas.save()
        canvas.translate(scrollX.toFloat(), scrollY.toFloat())
        gutterPaint.color = palette.panel.toArgb()
        canvas.drawRect(0f, 0f, paddingLeft.toFloat() - 4f, height.toFloat(), gutterPaint)
        gutterPaint.color = palette.subtleText.toArgb()
        gutterPaint.textSize = textSize
        gutterPaint.typeface = Typeface.MONOSPACE
        gutterPaint.textAlign = Paint.Align.RIGHT
        for (line in first..last) {
            canvas.drawText((line + 1).toString(), paddingLeft.toFloat() - 12f,
                (layout.getLineBaseline(line) + totalPaddingTop - scrollY).toFloat(), gutterPaint)
        }
        canvas.restore()
    }

    fun dispose() { removeCallbacks(highlightTask); notifyChange = null; highlightSource = null }
    override fun onDetachedFromWindow() { removeCallbacks(highlightTask); super.onDetachedFromWindow() }
}
