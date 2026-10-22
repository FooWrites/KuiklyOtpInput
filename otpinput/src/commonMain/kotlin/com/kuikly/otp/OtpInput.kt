package com.kuikly.otp

import com.tencent.kuikly.core.base.Border
import com.tencent.kuikly.core.base.BorderStyle
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ContainerAttr
import com.tencent.kuikly.core.base.ViewConst
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.event.Event
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Input
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

/**
 * OTP boxes for login / bind-card / change-password.
 *
 * One numeric [Input] covers the row and holds the whole code. Visual cells are not
 * separate fields, so SMS autofill, iOS one-time-code insertion, and paste are not
 * clipped by a per-box max length of 1. Backspace deletes the last digit and the
 * highlight moves back with it.
 *
 * All eight cells are mounted once and the ones past [OtpInputAttr.length] collapse to
 * zero width, so a length change never adds or removes views after mount. The cell row
 * stays in normal flow and gives the component its width; only the Input is absolute.
 *
 * Kuikly 2.4.2 does not forward Android autofill hints or iOS textContentType on
 * Input. Android hosts can call OtpAutofill.applySmsOtpHints after the page loads.
 */
class OtpInputView : ViewContainer<OtpInputAttr, OtpInputEvent>(), OtpInputHandle {
    private val binding = OtpInputBinding { change -> onCodeChange(change) }
    private var digits by observable("")
    private var codeLength by observable(OTP_DEFAULT_LENGTH)
    private var focused by observable(false)
    private var obscureDigits by observable(false)
    private var visualState by observable(OtpInputState.Normal)
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
        mountCells()
        attr.onChanged = { syncFromAttr() }
        syncFromAttr()
    }

    override fun setCode(code: String) {
        binding.setCode(code)
    }

    override fun clear() {
        binding.clear()
    }

    override fun focus() {
        binding.focus()
    }

    private fun syncFromAttr() {
        obscureDigits = attr.obscureDigits
        visualState = attr.inputState
        codeLength = attr.codeLength
        binding.setLength(attr.codeLength)
        if (attr.autoFocus && !didAutoFocus) {
            didAutoFocus = true
            focus()
        }
    }

    private fun onCodeChange(change: OtpCodeChange) {
        digits = change.code
        getViewEvent().onChangeHandler?.invoke(change.code)
        if (change.completed) {
            getViewEvent().onCompleteHandler?.invoke(change.code)
        }
    }

    private fun mountCells() {
        val ctx = this
        View {
            attr {
                flexDirectionRow()
                height(OtpInputHeight)
            }
            for (index in 0 until OTP_MAX_LENGTH) {
                val cellIndex = index
                View {
                    attr {
                        val length = ctx.codeLength
                        val shown = cellIndex < length
                        width(if (shown) OtpCellStyle.Width else 0f)
                        height(OtpInputHeight)
                        marginRight(if (cellIndex < length - 1) OtpCellStyle.Gap else 0f)
                        visibility(shown)
                        borderRadius(OtpCellStyle.Radius)
                        allCenter()
                        val colors = OtpCellStyle.colors(
                            cellIndex,
                            ctx.digits,
                            length,
                            ctx.visualState,
                            ctx.focused,
                        )
                        backgroundColor(Color(colors.background))
                        border(Border(OtpCellStyle.BorderWidth, BorderStyle.SOLID, Color(colors.border)))
                    }
                    Text {
                        attr {
                            text(OtpCellStyle.glyph(cellIndex, ctx.digits, ctx.obscureDigits))
                            fontSize(OtpCellStyle.FontSize)
                            fontWeightBold()
                            color(Color(OtpCellStyle.TextColor))
                            textAlignCenter()
                        }
                    }
                }
            }
        }
        Input {
            ctx.binding.attachInput(this)
            attr {
                absolutePositionAllZero()
                backgroundColor(Color.TRANSPARENT)
                fontSize(16f)
                color(Color.TRANSPARENT)
                tintColor(Color.TRANSPARENT)
                textAlignLeft()
                keyboardTypeNumber()
                imeNoFullscreen(true)
            }
            event {
                textDidChange { params ->
                    ctx.binding.onTextChanged(params.text)
                }
                inputFocus {
                    ctx.focused = true
                    ctx.binding.onFocus()
                }
                inputBlur {
                    ctx.focused = false
                }
            }
        }
    }
}

class OtpInputAttr : ContainerAttr() {
    internal var codeLength: Int = OTP_DEFAULT_LENGTH
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
