package com.kuikly.otp

import com.tencent.kuikly.core.base.Border
import com.tencent.kuikly.core.base.BorderStyle
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ContainerAttr
import com.tencent.kuikly.core.base.ViewConst
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.ViewRef
import com.tencent.kuikly.core.base.event.Event
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Input
import com.tencent.kuikly.core.views.InputView
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

/**
 * Visual state of the boxes. The caller owns transitions; editing does not clear them.
 */
enum class OtpInputState {
    Normal,
    Error,
    Success,
}

/**
 * Imperative handle shared by 自研 DSL ([ViewRef]) and Compose DSL.
 */
class OtpInputController {
    internal var input: OtpInputView? = null

    fun attach(view: OtpInputView) {
        input = view
    }

    fun setCode(code: String) {
        input?.setCode(code)
    }

    fun clear() {
        input?.clear()
    }

    fun focus() {
        input?.focus()
    }
}

/**
 * OTP boxes for login / bind-card / change-password.
 *
 * One numeric [Input] covers the row and holds the whole code. Visual cells are not
 * separate fields, so SMS autofill, iOS one-time-code insertion, and paste are not
 * clipped by a per-box max length of 1. Backspace deletes the last digit and the
 * highlight moves back with it.
 *
 * Kuikly 2.4.2 does not forward Android autofill hints or iOS textContentType on
 * Input. Android hosts can call OtpAutofill.applySmsOtpHints after the page loads.
 */
class OtpInputView : ViewContainer<OtpInputAttr, OtpInputEvent>() {
    private var inputRef: ViewRef<InputView>? = null
    private var digits by observable("")
    private var focused by observable(false)
    private var obscureDigits by observable(false)
    private var visualState by observable(OtpInputState.Normal)
    private var mountedLength = -1
    private var completedFor: String? = null
    private var pushingText = false
    private var didAutoFocus = false

    override fun viewName(): String = ViewConst.TYPE_VIEW

    override fun createAttr(): OtpInputAttr {
        // height() writes through Attr.flexNode, which is still null inside createAttr.
        // The view flex node already exists, so reserve the row here.
        flexNode.styleHeight = OtpInputHeight
        return OtpInputAttr()
    }

    override fun createEvent(): OtpInputEvent = OtpInputEvent()

    override fun didInit() {
        super.didInit()
        attr.onChanged = { syncFromAttr() }
        syncFromAttr()
    }

    fun setCode(raw: String) {
        val next = normalizeOtp(raw, attr.codeLength)
        pushText(next)
        if (next != digits) {
            updateCode(next)
        }
    }

    fun clear() {
        setCode("")
    }

    fun focus() {
        inputRef?.view?.focus()
    }

    private fun syncFromAttr() {
        obscureDigits = attr.obscureDigits
        visualState = attr.inputState
        val length = attr.codeLength
        if (length != mountedLength) {
            mountedLength = length
            removeChildren()
            inputRef = null
            if (digits.length > length) {
                val trimmed = digits.substring(0, length)
                updateCode(trimmed)
            }
            mountCells(length)
            if (digits.isNotEmpty()) {
                pushText(digits)
            }
        }
        if (attr.autoFocus && !didAutoFocus) {
            didAutoFocus = true
            focus()
        }
    }

    private fun mountCells(length: Int) {
        val ctx = this
        View {
            attr {
                absolutePositionAllZero()
                flexDirectionRow()
            }
            for (index in 0 until length) {
                val cellIndex = index
                View {
                    attr {
                        width(CELL_WIDTH)
                        height(OtpInputHeight)
                        if (cellIndex < length - 1) {
                            marginRight(CELL_GAP)
                        }
                        borderRadius(8f)
                        allCenter()
                        val colors = ctx.cellColors(cellIndex, length)
                        backgroundColor(colors.background)
                        border(Border(1.5f, BorderStyle.SOLID, colors.border))
                    }
                    Text {
                        attr {
                            val value = ctx.digits
                            val hidden = ctx.obscureDigits
                            val glyph = when {
                                cellIndex >= value.length -> ""
                                hidden -> "•"
                                else -> value[cellIndex].toString()
                            }
                            text(glyph)
                            fontSize(20f)
                            fontWeightBold()
                            color(Color(0xFF1F2328L))
                            textAlignCenter()
                        }
                    }
                }
            }
        }
        Input {
            ref { ctx.inputRef = it }
            attr {
                absolutePositionAllZero()
                backgroundColor(Color.TRANSPARENT)
                fontSize(16f)
                color(Color.TRANSPARENT)
                tintColor(Color.TRANSPARENT)
                textAlignLeft()
                keyboardTypeNumber()
                imeNoFullscreen(true)
                autofocus(ctx.attr.autoFocus)
            }
            event {
                textDidChange { params ->
                    ctx.onInputText(params.text)
                }
                inputFocus {
                    ctx.focused = true
                }
                inputBlur {
                    ctx.focused = false
                }
            }
        }
    }

    private fun onInputText(raw: String) {
        if (pushingText) {
            return
        }
        val next = normalizeOtp(raw, attr.codeLength)
        if (next != raw) {
            pushText(next)
        }
        if (next != digits) {
            updateCode(next)
        }
    }

    private fun pushText(text: String) {
        val input = inputRef?.view ?: return
        pushingText = true
        input.setText(text)
        pushingText = false
    }

    private fun updateCode(next: String) {
        digits = next
        getViewEvent().onChangeHandler?.invoke(next)
        if (next.length == attr.codeLength) {
            if (completedFor != next) {
                completedFor = next
                getViewEvent().onCompleteHandler?.invoke(next)
            }
        } else {
            completedFor = null
        }
    }

    private fun cellColors(index: Int, length: Int): CellColors {
        val value = digits
        val state = visualState
        val active = focused && value.length < length && index == value.length
        val filled = index < value.length
        return when (state) {
            OtpInputState.Error -> CellColors(Color(0xFFD92D20L), Color(0xFFFFF6F6L))
            OtpInputState.Success -> CellColors(Color(0xFF12B76AL), Color(0xFFF3FCF7L))
            OtpInputState.Normal -> when {
                active -> CellColors(Color(0xFF1F6FEBL), Color.WHITE)
                filled -> CellColors(Color(0xFF1F2328L), Color.WHITE)
                else -> CellColors(Color(0xFFD0D5DDL), Color.WHITE)
            }
        }
    }

    private class CellColors(val border: Color, val background: Color)

    private companion object {
        const val CELL_WIDTH = 40f
        const val CELL_GAP = 8f
    }
}

class OtpInputAttr : ContainerAttr() {
    internal var codeLength: Int = 6
    internal var obscureDigits: Boolean = false
    internal var inputState: OtpInputState = OtpInputState.Normal
    internal var autoFocus: Boolean = false
    internal var onChanged: (() -> Unit)? = null

    fun length(value: Int) {
        codeLength = clampOtpLength(value)
        onChanged?.invoke()
    }

    /** When true, cells draw bullets. The underlying field still stores digits. */
    fun obscure(value: Boolean) {
        obscureDigits = value
        onChanged?.invoke()
    }

    fun state(value: OtpInputState) {
        inputState = value
        onChanged?.invoke()
    }

    fun autofocus(value: Boolean) {
        autoFocus = value
        onChanged?.invoke()
    }
}

class OtpInputEvent : Event() {
    internal var onChangeHandler: ((String) -> Unit)? = null
    internal var onCompleteHandler: ((String) -> Unit)? = null

    fun onChange(handler: (String) -> Unit) {
        onChangeHandler = handler
    }

    fun onComplete(handler: (String) -> Unit) {
        onCompleteHandler = handler
    }
}

fun ViewContainer<*, *>.OtpInput(init: OtpInputView.() -> Unit) {
    addChild(OtpInputView(), init)
}

/**
 * Vertical space reserved by [OtpInput] / [OtpInputView] in a parent column, in dp.
 * Callers do not need an outer height wrapper.
 */
const val OtpInputHeight = 48f
