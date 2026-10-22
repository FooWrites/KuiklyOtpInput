package com.kuikly.otp

import com.tencent.kuikly.core.views.InputView

/**
 * Imperative calls that [OtpInputController] forwards to a mounted OTP input.
 */
interface OtpInputHandle {
    fun setCode(code: String)
    fun clear()
    fun focus()
}

/**
 * Imperative handle shared by 自研 DSL ([com.tencent.kuikly.core.base.ViewRef]) and Compose DSL.
 */
class OtpInputController {
    private var handle: OtpInputHandle? = null

    fun attach(handle: OtpInputHandle) {
        this.handle = handle
    }

    fun detach(handle: OtpInputHandle) {
        if (this.handle === handle) {
            this.handle = null
        }
    }

    fun setCode(code: String) {
        handle?.setCode(code)
    }

    fun clear() {
        handle?.clear()
    }

    fun focus() {
        handle?.focus()
    }
}

/**
 * Connects the single numeric [InputView] to [OtpCodeState]. Both DSLs drive their
 * Input through this, so normalization, cursor handling and onComplete dedupe match.
 *
 * The field text is rewritten to the normalized digits and the cursor is kept at the
 * end, so a tap in the middle cannot make backspace delete a digit other than the last.
 */
class OtpInputBinding(private val onCodeChange: (OtpCodeChange) -> Unit) : OtpInputHandle {
    val state = OtpCodeState()
    private var input: InputView? = null

    fun attachInput(view: InputView) {
        input = view
        if (state.code.isNotEmpty()) {
            pushText()
        }
    }

    fun setLength(length: Int) {
        val change = state.setLength(length) ?: return
        pushText()
        onCodeChange(change)
    }

    fun onTextChanged(raw: String) {
        val change = state.accept(raw)
        if (raw != state.code) {
            pushText()
        } else {
            pinCursor()
        }
        change?.let(onCodeChange)
    }

    fun onFocus() {
        pinCursor()
    }

    override fun setCode(code: String) {
        val change = state.accept(code)
        pushText()
        change?.let(onCodeChange)
    }

    override fun clear() {
        setCode("")
    }

    override fun focus() {
        input?.focus()
    }

    private fun pushText() {
        input?.setText(state.code)
        pinCursor()
    }

    private fun pinCursor() {
        input?.setCursorIndex(state.code.length)
    }
}
