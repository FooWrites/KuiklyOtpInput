package com.kuikly.otp

import com.tencent.kuikly.core.base.Border
import com.tencent.kuikly.core.base.BorderStyle
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.ViewRef
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.timer.clearTimeout
import com.tencent.kuikly.core.timer.setTimeout
import com.tencent.kuikly.core.views.Input
import com.tencent.kuikly.core.views.InputAttr
import com.tencent.kuikly.core.views.InputView
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.TextInputState
import com.tencent.kuikly.core.views.View

/** 输入框的视觉状态，由调用方根据校验结果切换。 */
enum class OtpInputStatus {
    NORMAL,
    ERROR,
    SUCCESS,
}

/** 格子形状。 */
enum class OtpCellShape {
    /** 四边边框，使用 [OtpInputAttr.cornerRadius]。 */
    BOX,

    /** 只有底部横线，忽略 [OtpInputAttr.cornerRadius]。 */
    UNDERLINE,
}

/**
 * 输入框各状态的颜色。
 *
 * [border] 是空格子的边框，[activeBorder] 是聚焦时当前格子的边框，[filledBorder] 是已填写格子的边框。
 * [OtpInputStatus.ERROR] 和 [OtpInputStatus.SUCCESS] 下所有格子使用 [error] / [success] 作边框色，
 * [errorCell] / [successCell] 作底色。不可编辑时使用 disabled 系列颜色，优先级高于状态色。
 */
class OtpInputColors(
    val text: Color = Color(0xFF1F2328),
    val placeholder: Color = Color(0xFFB0B7C3),
    val cursor: Color = Color(0xFF1F6FEB),
    val cell: Color = Color.WHITE,
    val border: Color = Color(0xFFD0D5DD),
    val activeBorder: Color = Color(0xFF1F6FEB),
    val filledBorder: Color = Color(0xFF98A2B3),
    val error: Color = Color(0xFFD92D20),
    val errorCell: Color = Color(0xFFFEF3F2),
    val success: Color = Color(0xFF12B76A),
    val successCell: Color = Color(0xFFECFDF3),
    val disabledText: Color = Color(0xFF98A2B3),
    val disabledCell: Color = Color(0xFFF2F4F7),
    val disabledBorder: Color = Color(0xFFE4E7EC),
)

/**
 * 验证码输入框的属性。
 *
 * 支持系统键盘输入、粘贴、删除和可选的系统短信验证码填充。
 * 尺寸类属性的单位与 Kuikly 布局一致。
 *
 * 格子是组件的直接子节点，组件默认 `flexDirectionRow()`、`alignItemsCenter()`，
 * 通过 `justifyContent` / `alignItems` 设置格子的水平排布和垂直对齐。
 */
class OtpInputAttr : ComposeAttr() {
    internal var rulesChanged: (() -> Unit)? = null

    private var lengthValue by observable(6)
    private var charsetValue by observable(OtpCharset.DIGITS)
    private var editableValue by observable(true)

    /** 验证码位数，取值 1..12，超出范围会被截断。缩短时丢弃多出的字符。 */
    var length: Int
        get() = lengthValue
        set(value) {
            lengthValue = OtpCode.clampLength(value)
            rulesChanged?.invoke()
        }

    /** 允许输入的字符。切换时按新规则过滤已有内容。 */
    var charset: OtpCharset
        get() = charsetValue
        set(value) {
            charsetValue = value
            rulesChanged?.invoke()
        }

    /** 为 false 时不能聚焦和输入，显示 disabled 系列颜色。聚焦时改为 false 会失焦。 */
    var editable: Boolean
        get() = editableValue
        set(value) {
            editableValue = value
            rulesChanged?.invoke()
        }

    /** 创建后自动聚焦并弹出键盘，只在创建时读取。 */
    var autofocus: Boolean = false

    /** 填满后自动失焦收起键盘。 */
    var blurOnComplete: Boolean = true

    /** 是否向宿主声明这是一次性验证码输入框，宿主据此开启系统短信验证码填充，见 README。 */
    var smsAutofill: Boolean by observable(true)

    /** 用 [maskChar] 遮挡已输入的字符。 */
    var mask: Boolean by observable(false)

    /** 遮挡字符。 */
    var maskChar: String by observable("•")

    /** 空格子显示的占位字符，空字符串表示不显示。聚焦的当前格子显示光标而不显示占位字符。 */
    var placeholderChar: String by observable("")

    /** 聚焦时在当前格子显示闪烁光标。 */
    var showCursor: Boolean by observable(true)

    /** 校验状态，影响格子边框颜色和底色。 */
    var status: OtpInputStatus by observable(OtpInputStatus.NORMAL)

    /** 格子形状：四边边框或下划线。 */
    var cellShape: OtpCellShape by observable(OtpCellShape.BOX)

    /** 单个格子的宽度。 */
    var cellWidth: Float by observable(44f)

    /** 单个格子的高度。 */
    var cellHeight: Float by observable(52f)

    /** 相邻格子的间距。`justifyContent` 为 SPACE_* 时，剩余空间在此基础上再分配。 */
    var cellSpacing: Float by observable(8f)

    /** 格子的圆角；下划线样式忽略此属性。 */
    var cornerRadius: Float by observable(8f)

    /** 边框（或下划线）宽度，0 表示不显示。 */
    var borderWidth: Float by observable(1f)

    /** 聚焦时当前格子的边框宽度。 */
    var activeBorderWidth: Float by observable(2f)

    /** 字符字号。 */
    var fontSize: Float by observable(22f)

    /** 各输入状态的颜色。 */
    var colors: OtpInputColors by observable(OtpInputColors())
}

/**
 * 验证码输入框事件。
 *
 * [onChange] 在内容变化时触发，包括输入、删除、粘贴、短信填充和 [OtpInputView.setCode]。
 * [onComplete] 在内容变为一个新的完整验证码时触发，同一个验证码不会重复触发。
 */
class OtpInputEvent : ComposeEvent() {
    internal var changeHandler: ((code: String) -> Unit)? = null
    internal var completeHandler: ((code: String) -> Unit)? = null
    internal var focusHandler: ((focused: Boolean) -> Unit)? = null

    fun onChange(handler: (code: String) -> Unit) {
        changeHandler = handler
    }

    fun onComplete(handler: (code: String) -> Unit) {
        completeHandler = handler
    }

    fun onFocusChange(handler: (focused: Boolean) -> Unit) {
        focusHandler = handler
    }
}

/**
 * Kuikly DSL 验证码输入框视图。
 *
 * 通过 [ViewRef] 调用 [setCode]、[clear]、[focus] 和 [blur]。
 */
class OtpInputView : ComposeView<OtpInputAttr, OtpInputEvent>() {
    private val otp = OtpCode(OTP_MAX_LENGTH, OtpCharset.DIGITS)
    private var codeText by observable("")
    private var focused by observable(false)
    private var cursorVisible by observable(false)
    private var inputRef: ViewRef<InputView>? = null
    private var blinkRef: String? = null

    /** 当前内容。 */
    val code: String
        get() = otp.code

    /** 已填满全部格子。 */
    val isComplete: Boolean
        get() = otp.isComplete

    /** 当前是否聚焦。 */
    val isFocused: Boolean
        get() = focused

    override fun createAttr(): OtpInputAttr = OtpInputAttr()

    override fun createEvent(): OtpInputEvent = OtpInputEvent()

    override fun willInit() {
        super.willInit()
        attr {
            flexDirectionRow()
            alignItemsCenter()
        }
    }

    override fun created() {
        super.created()
        attr.rulesChanged = { syncRules() }
        syncRules()
    }

    override fun viewDidLoad() {
        super.viewDidLoad()
        if (otp.code.isNotEmpty()) {
            syncNativeText()
        }
    }

    override fun viewWillUnload() {
        super.viewWillUnload()
        attr.rulesChanged = null
        stopBlink()
    }

    /** 用 [raw] 过滤后的内容替换当前内容，会触发 [OtpInputEvent.onChange] 和 [OtpInputEvent.onComplete]。 */
    fun setCode(raw: String) {
        val change = otp.replace(raw)
        syncNativeText()
        change?.let(::dispatch)
    }

    /** 清空内容。 */
    /** 清空当前验证码，并触发内容变化回调。 */
    fun clear() {
        setCode("")
    }

    /** 在可编辑状态下聚焦并弹出键盘。 */
    fun focus() {
        if (attr.editable) {
            inputRef?.view?.focus()
        }
    }

    /** 失焦并收起键盘。 */
    fun blur() {
        inputRef?.view?.blur()
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            for (index in 0 until OTP_MAX_LENGTH) {
                vif({ index < ctx.attr.length }) {
                    ctx.cell(this, index)
                }
            }
            Input {
                ref {
                    ctx.inputRef = it
                }
                attr {
                    absolutePositionAllZero()
                    backgroundColor(Color.TRANSPARENT)
                    color(Color.TRANSPARENT)
                    tintColor(Color.TRANSPARENT)
                    selectionColor(Color.TRANSPARENT)
                    fontSize(16f)
                    editable(ctx.attr.editable)
                    autofocus(ctx.attr.autofocus && ctx.attr.editable)
                    imeNoFullscreen(true)
                    returnKeyTypeDone()
                    val digits = ctx.attr.charset == OtpCharset.DIGITS
                    // H5 的数字输入框不支持设置光标位置，也会拒绝粘贴含文字的短信，统一使用文本输入框，
                    // 由宿主根据 otpKeyboard 设置 inputmode。
                    if (digits && !ctx.pagerData.isWeb) {
                        keyboardTypeNumber()
                    } else {
                        InputAttr.KEYBOARD_TYPE with "text"
                    }
                    PROP_OTP_KEYBOARD with if (digits) "numeric" else "text"
                    PROP_OTP_AUTOFILL with if (ctx.attr.smsAutofill) "on" else "off"
                }
                event {
                    textDidChange {
                        ctx.onNativeText(it.text)
                    }
                    selectionChange {
                        ctx.onSelection(it)
                    }
                    inputFocus {
                        ctx.onFocusChanged(true)
                    }
                    inputBlur {
                        ctx.onFocusChanged(false)
                    }
                    inputReturn {
                        ctx.blur()
                    }
                }
            }
        }
    }

    private fun cell(container: ViewContainer<*, *>, index: Int) {
        val ctx = this
        container.View {
            attr {
                val a = ctx.attr
                val style = ctx.cellStyle(index)
                size(a.cellWidth, a.cellHeight)
                marginLeft(if (index > 0) a.cellSpacing else 0f)
                allCenter()
                backgroundColor(style.background)
                if (a.cellShape == OtpCellShape.BOX) {
                    borderRadius(a.cornerRadius)
                    border(Border(style.borderWidth, BorderStyle.SOLID, style.border))
                } else {
                    borderRadius(0f)
                    border(Border(0f, BorderStyle.SOLID, Color.TRANSPARENT))
                }
            }
            Text {
                attr {
                    val a = ctx.attr
                    val filled = index < ctx.codeText.length
                    text(ctx.glyph(index))
                    fontSize(a.fontSize)
                    fontWeightBold()
                    color(
                        when {
                            !a.editable -> a.colors.disabledText
                            filled -> a.colors.text
                            else -> a.colors.placeholder
                        }
                    )
                }
            }
            View {
                attr {
                    val a = ctx.attr
                    val height = a.fontSize * CURSOR_HEIGHT_RATIO
                    absolutePosition(
                        top = (a.cellHeight - height) / 2f,
                        left = (a.cellWidth - CURSOR_WIDTH) / 2f
                    )
                    size(CURSOR_WIDTH, height)
                    backgroundColor(a.colors.cursor)
                    visibility(ctx.cursorVisible && ctx.cursorIndex() == index)
                }
            }
            View {
                attr {
                    val a = ctx.attr
                    val style = ctx.cellStyle(index)
                    absolutePosition(left = 0f, right = 0f, bottom = 0f)
                    height(style.borderWidth)
                    backgroundColor(style.border)
                    visibility(a.cellShape == OtpCellShape.UNDERLINE && style.borderWidth > 0f)
                }
            }
        }
    }

    private fun cellStyle(index: Int): OtpCellStyle {
        val a = attr
        val c = a.colors
        val active = focused && index == activeIndex()
        val width = if (active) a.activeBorderWidth else a.borderWidth
        return when {
            !a.editable -> OtpCellStyle(c.disabledCell, c.disabledBorder, a.borderWidth)
            a.status == OtpInputStatus.ERROR -> OtpCellStyle(c.errorCell, c.error, width)
            a.status == OtpInputStatus.SUCCESS -> OtpCellStyle(c.successCell, c.success, width)
            active -> OtpCellStyle(c.cell, c.activeBorder, width)
            index < codeText.length -> OtpCellStyle(c.cell, c.filledBorder, width)
            else -> OtpCellStyle(c.cell, c.border, width)
        }
    }

    private fun glyph(index: Int): String {
        val text = codeText
        return when {
            index < text.length -> if (attr.mask) attr.maskChar else text[index].toString()
            cursorIndex() == index && focused && attr.showCursor -> ""
            else -> attr.placeholderChar
        }
    }

    /** 聚焦时高亮的格子：未填满时为下一个待输入的格子，填满后为最后一个格子。 */
    private fun activeIndex(): Int = minOf(codeText.length, attr.length - 1)

    /** 显示光标的格子，填满后没有光标。 */
    private fun cursorIndex(): Int = if (codeText.length < attr.length) codeText.length else -1

    private fun syncRules() {
        if (!attr.editable) {
            blur()
        }
        val charsetChange = otp.updateCharset(attr.charset)
        val lengthChange = otp.updateLength(attr.length)
        if (charsetChange == null && lengthChange == null) {
            return
        }
        syncNativeText()
        charsetChange?.let(::dispatch)
        lengthChange?.let(::dispatch)
    }

    private fun onNativeText(raw: String) {
        val change = otp.input(raw)
        if (raw != otp.code) {
            syncNativeText()
        }
        change?.let(::dispatch)
    }

    private fun onSelection(state: TextInputState) {
        val end = otp.code.length
        if (focused && (state.selectionStart != end || state.selectionEnd != end)) {
            inputRef?.view?.setCursorIndex(end)
        }
    }

    private fun onFocusChanged(value: Boolean) {
        // H5 的只读输入框仍可获得焦点
        if (value && !attr.editable) {
            blur()
            return
        }
        if (focused == value) {
            return
        }
        focused = value
        if (value) {
            pinCursor()
        }
        restartBlink()
        event.focusHandler?.invoke(value)
    }

    private fun syncNativeText() {
        inputRef?.view?.setText(otp.code)
        pinCursor()
    }

    // 光标固定在末尾，删除总是从最后一个字符开始，新输入总是追加。
    // H5 设置光标位置时会主动聚焦，因此只在聚焦状态下设置。
    private fun pinCursor() {
        if (focused) {
            inputRef?.view?.setCursorIndex(otp.code.length)
        }
    }

    private fun dispatch(change: OtpCodeChange) {
        if (change.changed) {
            codeText = change.code
            restartBlink()
            event.changeHandler?.invoke(change.code)
        }
        if (change.completed) {
            event.completeHandler?.invoke(change.code)
            if (attr.blurOnComplete) {
                blur()
            }
        }
    }

    // 每次输入后光标保持常亮，停顿后再开始闪烁。
    private fun restartBlink() {
        stopBlink()
        cursorVisible = focused && attr.showCursor
        if (cursorVisible && !otp.isComplete) {
            scheduleBlink()
        }
    }

    private fun scheduleBlink() {
        blinkRef = setTimeout(CURSOR_BLINK_MS) {
            cursorVisible = !cursorVisible
            scheduleBlink()
        }
    }

    private fun stopBlink() {
        blinkRef?.let { clearTimeout(it) }
        blinkRef = null
    }

    companion object {
        private const val CURSOR_WIDTH = 2f
        private const val CURSOR_HEIGHT_RATIO = 1.1f
        private const val CURSOR_BLINK_MS = 530
    }
}

private class OtpCellStyle(val background: Color, val border: Color, val borderWidth: Float)

// 设置在原生输入框上、由宿主处理的自定义属性，宿主接入方式见 README。
// otpAutofill 取 "on" / "off"；otpKeyboard 取 "numeric" / "text"。
private const val PROP_OTP_AUTOFILL = "otpAutofill"
private const val PROP_OTP_KEYBOARD = "otpKeyboard"

fun ViewContainer<*, *>.OtpInput(init: OtpInputView.() -> Unit) {
    addChild(OtpInputView(), init)
}
