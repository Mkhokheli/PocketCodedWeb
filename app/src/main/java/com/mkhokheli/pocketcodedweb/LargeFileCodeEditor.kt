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
    session: EditorSession? = null,
    palette: WebPalette,
    modifier: Modifier,
    highlight: (String) -> AnnotatedString,
    onValueChange: (TextFieldValue) -> Unit,
    breakpoints: Set<Int> = emptySet(),
    onBreakpoint: (Int) -> Unit = {},
    sourceJump: WebSourceJump? = null,
) {
    AndroidView(
        modifier = modifier,
        factory = { LargeDocumentEditText(it) },
        update = {
            it.bindSession(session)
            it.bind(value, palette, highlight, onValueChange)
            it.bindDevelopment(breakpoints, onBreakpoint, sourceJump)
        },
        onRelease = { it.dispose() },
    )
}

internal class LargeDocumentEditText(context: Context) : EditText(context) {
    private var session: EditorSession? = null
    private var hasBoundValue = false
    private var followCursor = true
    private val revealCaretTask = Runnable {
        if (hasFocus() && followCursor && !dragged) revealCaret(selectionEnd)
    }
    fun bindSession(updated: EditorSession?) {
        if (session === updated) return
        session = updated
        hasBoundValue = false
        followCursor = updated?.followCursor ?: true
        updated?.let { saved ->
            val savedX = saved.horizontalScroll
            val savedY = saved.verticalScroll
            post { if (session === saved) scrollTo(savedX, savedY) }
        }
    }
    private fun followEditedCursor() {
        followCursor = true
        session?.followCursor = true
        removeCallbacks(revealCaretTask)
        post(revealCaretTask)
    }
    private fun revealCaret(offset: Int): Boolean {
        val currentLayout = layout ?: return false
        if (offset !in 0..length()) return false
        val line = currentLayout.getLineForOffset(offset)
        val margin = 12f * resources.displayMetrics.density
        val x = currentLayout.getPrimaryHorizontal(offset)
        val targetX = editorCursorScrollTarget(scrollX,
            width - totalPaddingLeft - totalPaddingRight,
            (currentLayout.width - width + totalPaddingLeft + totalPaddingRight).coerceAtLeast(0),
            x, x + 2f, margin)
        val targetY = editorCursorScrollTarget(scrollY,
            height - totalPaddingTop - totalPaddingBottom,
            (currentLayout.height - height + totalPaddingTop + totalPaddingBottom).coerceAtLeast(0),
            currentLayout.getLineTop(line).toFloat(), currentLayout.getLineBottom(line).toFloat(), margin)
        if (targetX == scrollX && targetY == scrollY) return false
        scrollTo(targetX, targetY)
        return true
    }
    private var breakpoints: Set<Int> = emptySet()
    private var onBreakpoint: ((Int) -> Unit)? = null
    private var gutterGesture = false
    private var lastJump: Long? = null
    fun bindDevelopment(points: Set<Int>, toggle: (Int) -> Unit, jump: WebSourceJump?) {
        if (breakpoints != points) { breakpoints = points; invalidate() }
        onBreakpoint = toggle
        if (jump != null && lastJump != jump.version) {
            lastJump = jump.version
            post {
                val currentLayout = layout ?: return@post
                val line = (jump.line - 1).coerceIn(0, currentLayout.lineCount - 1)
                scrollTo(0, currentLayout.getLineTop(line))
                invalidate()
            }
        }
    }
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
                followEditedCursor()
                scheduleHighlight()
            }
        })
    }

    fun bind(value: TextFieldValue, palette: WebPalette, highlight: (String) -> AnnotatedString, changed: (TextFieldValue) -> Unit) {
        val restoringSession = !hasBoundValue
        notifyChange = changed
        highlightSource = highlight
        val newPalette = currentPalette != palette
        if (newPalette) {
            currentPalette = palette
            setBackgroundColor(palette.editorBackground.toArgb())
            setTextColor(palette.text.toArgb())
            setHintTextColor(palette.subtleText.toArgb())
            highlightColor = palette.accent.copy(alpha = 0.18f).toArgb()
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
                if (!restoringSession) followEditedCursor()
            }
            val start = value.selection.start.coerceIn(0, length())
            val end = value.selection.end.coerceIn(0, length())
            if (selectionStart != start || selectionEnd != end) {
                setSelection(start, end)
                if (!restoringSession) followEditedCursor()
            }
        } finally { applying = false; hasBoundValue = true }
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
        if (!applying && hasBoundValue && hasFocus()) followEditedCursor()
    }

    // Deliberately reveal the caret inside this editor; never scroll an outer panel/page.
    override fun bringPointIntoView(offset: Int): Boolean =
        if (hasFocus() && followCursor && !dragged) revealCaret(offset) else false
    override fun requestRectangleOnScreen(rectangle: Rect?, immediate: Boolean): Boolean = false

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        removeCallbacks(revealCaretTask)
        post(revealCaretTask)
    }

    override fun onScrollChanged(x: Int, y: Int, oldX: Int, oldY: Int) {
        super.onScrollChanged(x, y, oldX, oldY)
        session?.let { it.horizontalScroll = x; it.verticalScroll = y }
        scheduleHighlight()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) gutterGesture = event.x < paddingLeft
        if (gutterGesture) {
            if (event.actionMasked == MotionEvent.ACTION_DOWN) { downX = event.x; downY = event.y }
            if (event.actionMasked == MotionEvent.ACTION_UP) {
                val slop = ViewConfiguration.get(context).scaledTouchSlop
                if (abs(event.x - downX) < slop && abs(event.y - downY) < slop) {
                    layout?.let { measured ->
                        val sourceY = event.y + scrollY - totalPaddingTop
                        if (measured.lineCount > 0 && sourceY >= measured.getLineTop(0) &&
                                sourceY < measured.getLineBottom(measured.lineCount - 1)) {
                            onBreakpoint?.invoke(measured.getLineForVertical(sourceY.toInt()) + 1)
                        }
                    }
                }
                gutterGesture = false
            }
            if (event.actionMasked == MotionEvent.ACTION_CANCEL) gutterGesture = false
            return true
        }
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
                if (abs(event.x - downX) > slop || abs(event.y - downY) > slop) {
                    dragged = true
                    followCursor = false
                    session?.followCursor = false
                }
            }
            MotionEvent.ACTION_UP -> if (dragged) scheduleHighlight() else post(highlightTask)
            MotionEvent.ACTION_CANCEL -> scheduleHighlight()
        }
        val handled = super.onTouchEvent(event)
        if (event.actionMasked == MotionEvent.ACTION_UP && !dragged) {
            requestFocus()
            followEditedCursor()
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager)
                .showSoftInput(this, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }
        if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) dragged = false
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
        gutterPaint.color = palette.editorBackground.toArgb()
        canvas.drawRect(0f, 0f, paddingLeft.toFloat() - 4f, height.toFloat(), gutterPaint)
        gutterPaint.color = palette.subtleText.toArgb()
        gutterPaint.textSize = textSize
        gutterPaint.typeface = Typeface.MONOSPACE
        gutterPaint.textAlign = Paint.Align.CENTER
        val numberCenter = (paddingLeft.toFloat() - 4f) / 2f
        for (line in first..last) {
            gutterPaint.color = if (line + 1 in breakpoints) palette.error.toArgb() else palette.subtleText.toArgb()
            if (line + 1 in breakpoints) {
                canvas.drawCircle(numberCenter,
                    (layout.getLineTop(line) + layout.getLineBottom(line)) / 2f + totalPaddingTop - scrollY,
                    3f * resources.displayMetrics.density, gutterPaint)
            } else canvas.drawText((line + 1).toString(), numberCenter,
                (layout.getLineBaseline(line) + totalPaddingTop - scrollY).toFloat(), gutterPaint)
        }
        canvas.restore()
    }

    fun dispose() { removeCallbacks(highlightTask); removeCallbacks(revealCaretTask); session = null; notifyChange = null; highlightSource = null; onBreakpoint = null }
    override fun onDetachedFromWindow() { removeCallbacks(highlightTask); removeCallbacks(revealCaretTask); super.onDetachedFromWindow() }
}
