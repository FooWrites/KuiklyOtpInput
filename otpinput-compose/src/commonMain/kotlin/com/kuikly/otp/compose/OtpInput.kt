package com.kuikly.otp.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.extension.MakeKuiklyComposeNode
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.BoxScope
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.alpha
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.RectangleShape
import com.tencent.kuikly.compose.ui.platform.LocalDensity
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.TextUnit
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.views.InputAttr
import com.tencent.kuikly.core.views.InputView
import com.tencent.kuikly.core.views.TextInputState
import kotlinx.coroutines.delay
import com.tencent.kuikly.core.base.Color as KuiklyColor

/** 输入框的视觉状态，由调用方根据校验结果切换。 */
enum class OtpInputStatus {
    NORMAL,
    ERROR,
    SUCCESS,
}

/** 格子形状。 */
enum class OtpCellShape {
    /** 四边边框，使用 `cornerRadius`。 */
    BOX,

    /** 只有底部横线，忽略 `cornerRadius`。 */
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
    val cell: Color = Color.White,
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
 * Compose DSL 验证码输入框的状态。
 *
 * [code] 和 [isFocused] 是 Compose 状态。[setCode]、[clear]、[focus]、[blur] 需在 [OtpInput] 组合后调用，
 * 组合前调用 [setCode] 也会生效，内容在组合时按 `length` 和 `charset` 重新过滤。
 */
@Stable
class OtpInputState {
    private val otp = OtpCode(OTP_MAX_LENGTH, OtpCharset.DIGITS)
    private var input: InputView? = null
    internal var onChange: (String) -> Unit = {}
    internal var onComplete: (String) -> Unit = {}
    internal var onFocusChange: (Boolean) -> Unit = {}
    internal var blurOnComplete: Boolean = true
    internal var enabled: Boolean = true

    private var codeState by mutableStateOf("")

    /** 当前内容。 */
    val code: String
        get() = codeState

    /** 当前是否聚焦。 */
    var isFocused: Boolean by mutableStateOf(false)
        private set

    /** 已填满全部格子。 */
    val isComplete: Boolean
        get() = code.length == otp.length

    /** 用 [raw] 过滤后的内容替换当前内容，并触发相应回调。 */
    fun setCode(raw: String) {
        val change = otp.replace(raw)
        syncNativeText()
        change?.let(::dispatch)
    }

    /** 清空当前验证码，并触发内容变化回调。 */
    fun clear() {
        setCode("")
    }

    /** 在启用状态下聚焦并弹出键盘。 */
    fun focus() {
        if (enabled) {
            input?.focus()
        }
    }

    /** 失焦并收起键盘。 */
    fun blur() {
        input?.blur()
    }

    internal fun attach(view: InputView) {
        input = view
        if (otp.code.isNotEmpty()) {
            syncNativeText()
        }
    }

    internal fun detach() {
        input = null
    }

    internal fun applyRules(length: Int, charset: OtpCharset) {
        val charsetChange = otp.updateCharset(charset)
        val lengthChange = otp.updateLength(length)
        if (charsetChange == null && lengthChange == null) {
            return
        }
        syncNativeText()
        charsetChange?.let(::dispatch)
        lengthChange?.let(::dispatch)
    }

    internal fun onNativeText(raw: String) {
        val change = otp.input(raw)
        if (raw != otp.code) {
            syncNativeText()
        }
        change?.let(::dispatch)
    }

    internal fun onSelection(state: TextInputState) {
        val end = otp.code.length
        if (isFocused && (state.selectionStart != end || state.selectionEnd != end)) {
            input?.setCursorIndex(end)
        }
    }

    internal fun onFocusChanged(value: Boolean) {
        // H5 的只读输入框仍可获得焦点
        if (value && !enabled) {
            blur()
            return
        }
        if (isFocused == value) {
            return
        }
        isFocused = value
        if (value) {
            pinCursor()
        }
        onFocusChange(value)
    }

    private fun syncNativeText() {
        input?.setText(otp.code)
        pinCursor()
    }

    // 光标固定在末尾，删除总是从最后一个字符开始，新输入总是追加。
    // H5 设置光标位置时会主动聚焦，因此只在聚焦状态下设置。
    private fun pinCursor() {
        if (isFocused) {
            input?.setCursorIndex(otp.code.length)
        }
    }

    private fun dispatch(change: OtpCodeChange) {
        if (change.changed) {
            codeState = change.code
            onChange(change.code)
        }
        if (change.completed) {
            onComplete(change.code)
            if (blurOnComplete) {
                blur()
            }
        }
    }
}

@Composable
fun rememberOtpInputState(): OtpInputState = remember { OtpInputState() }

/**
 * Compose DSL 验证码输入框。
 *
 * ```
 * val otpState = rememberOtpInputState()
 * OtpInput(
 *     state = otpState,
 *     length = 6,
 *     horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
 *     onComplete = { code -> verify(code) },
 * )
 * otpState.clear()
 * ```
 *
 * 支持系统键盘输入、粘贴、删除和可选的系统短信验证码填充。
 * 组件尺寸默认由格子决定；通过 [modifier] 指定更大的尺寸时，格子按 [horizontalArrangement] 和 [verticalAlignment] 排布。
 *
 * @param length 验证码位数，取值 1..12，超出范围会被截断。缩短时丢弃多出的字符。
 * @param charset 允许输入的字符。切换时按新规则过滤已有内容。
 * @param enabled 为 false 时不能聚焦和输入，显示 disabled 系列颜色。
 * @param autoFocus 首次组合后自动聚焦并弹出键盘。
 * @param blurOnComplete 填满后自动失焦收起键盘。
 * @param smsAutofill 是否向宿主声明这是一次性验证码输入框，宿主据此开启系统短信验证码填充，见 README。
 * @param mask 用 [maskChar] 遮挡已输入的字符。
 * @param placeholderChar 空格子显示的占位字符，空字符串表示不显示。聚焦的当前格子显示光标而不显示占位字符。
 * @param showCursor 聚焦时在当前格子显示闪烁光标。
 * @param borderWidth 边框（或下划线）宽度，0 表示不显示。
 * @param activeBorderWidth 聚焦时当前格子的边框宽度。
 * @param onComplete 内容变为一个新的完整验证码时触发，同一个验证码不会重复触发。
 */
@Composable
fun OtpInput(
    modifier: Modifier = Modifier,
    state: OtpInputState = rememberOtpInputState(),
    length: Int = 6,
    charset: OtpCharset = OtpCharset.DIGITS,
    enabled: Boolean = true,
    autoFocus: Boolean = false,
    blurOnComplete: Boolean = true,
    smsAutofill: Boolean = true,
    mask: Boolean = false,
    maskChar: String = "•",
    placeholderChar: String = "",
    showCursor: Boolean = true,
    status: OtpInputStatus = OtpInputStatus.NORMAL,
    cellShape: OtpCellShape = OtpCellShape.BOX,
    cellWidth: Dp = 44.dp,
    cellHeight: Dp = 52.dp,
    cornerRadius: Dp = 8.dp,
    borderWidth: Dp = 1.dp,
    activeBorderWidth: Dp = 2.dp,
    fontSize: TextUnit = 22.sp,
    colors: OtpInputColors = OtpInputColors(),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    onChange: (code: String) -> Unit = {},
    onComplete: (code: String) -> Unit = {},
    onFocusChange: (focused: Boolean) -> Unit = {},
) {
    val cellCount = OtpCode.clampLength(length)
    SideEffect {
        state.onChange = onChange
        state.onComplete = onComplete
        state.onFocusChange = onFocusChange
        state.blurOnComplete = blurOnComplete
        state.enabled = enabled
        state.applyRules(cellCount, charset)
        if (!enabled && state.isFocused) {
            state.blur()
        }
    }

    val code = state.code
    val focused = state.isFocused && enabled
    val cursorIndex = if (focused && showCursor && code.length < cellCount) code.length else -1
    val activeIndex = if (focused) minOf(code.length, cellCount - 1) else -1
    val cursorHeight = with(LocalDensity.current) { fontSize.toDp() } * CURSOR_HEIGHT_RATIO

    // 子项继承 Box 的最小约束：modifier 指定尺寸时 Row 撑满，否则由格子决定尺寸。
    Box(modifier = modifier, propagateMinConstraints = true) {
        Row(
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment,
        ) {
            repeat(cellCount) { index ->
                val filled = index < code.length
                val style = cellStyle(
                    colors = colors,
                    enabled = enabled,
                    status = status,
                    active = index == activeIndex,
                    filled = filled,
                    borderWidth = borderWidth,
                    activeBorderWidth = activeBorderWidth,
                )
                val glyph = when {
                    filled -> if (mask) maskChar else code[index].toString()
                    index == cursorIndex -> ""
                    else -> placeholderChar
                }
                val glyphColor = when {
                    !enabled -> colors.disabledText
                    filled -> colors.text
                    else -> colors.placeholder
                }
                OtpCell(
                    width = cellWidth,
                    height = cellHeight,
                    shape = cellShape,
                    cornerRadius = cornerRadius,
                    style = style,
                ) {
                    Text(
                        text = glyph,
                        color = glyphColor,
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                    )
                    if (index == cursorIndex) {
                        OtpCursor(code, colors.cursor, cursorHeight)
                    }
                }
            }
        }
        OtpNativeInput(
            state = state,
            charset = charset,
            enabled = enabled,
            autoFocus = autoFocus,
            smsAutofill = smsAutofill,
        )
    }
}

@Composable
private fun OtpCell(
    width: Dp,
    height: Dp,
    shape: OtpCellShape,
    cornerRadius: Dp,
    style: OtpCellStyle,
    content: @Composable BoxScope.() -> Unit,
) {
    val box = shape == OtpCellShape.BOX
    val cellShape = if (box) RoundedCornerShape(cornerRadius) else RectangleShape
    Box(
        // Kuikly Compose 移除 border 修饰符时不会清除原生边框，因此始终设置，下划线样式下宽度为 0。
        modifier = Modifier
            .size(width, height)
            .background(style.background, cellShape)
            .border(
                width = if (box) style.borderWidth else 0.dp,
                color = if (box) style.border else Color.Transparent,
                shape = cellShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
        if (!box && style.borderWidth > 0.dp) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(style.borderWidth)
                    .background(style.border)
            )
        }
    }
}

// 每次输入后光标保持常亮，停顿后再开始闪烁。闪烁只重组光标本身。
@Composable
private fun OtpCursor(code: String, color: Color, height: Dp) {
    var visible by remember { mutableStateOf(true) }
    LaunchedEffect(code) {
        visible = true
        while (true) {
            delay(CURSOR_BLINK_MS)
            visible = !visible
        }
    }
    Box(
        modifier = Modifier
            .size(CURSOR_WIDTH, height)
            .alpha(if (visible) 1f else 0f)
            .background(color)
    )
}

@Composable
private fun BoxScope.OtpNativeInput(
    state: OtpInputState,
    charset: OtpCharset,
    enabled: Boolean,
    autoFocus: Boolean,
    smsAutofill: Boolean,
) {
    DisposableEffect(state) {
        onDispose { state.detach() }
    }
    MakeKuiklyComposeNode<InputView>(
        factory = { InputView() },
        modifier = Modifier.matchParentSize(),
        viewInit = {
            state.attach(this)
            getViewAttr().run {
                backgroundColor(KuiklyColor.TRANSPARENT)
                color(KuiklyColor.TRANSPARENT)
                tintColor(KuiklyColor.TRANSPARENT)
                selectionColor(KuiklyColor.TRANSPARENT)
                fontSize(16f)
                imeNoFullscreen(true)
                returnKeyTypeDone()
                autofocus(autoFocus && enabled)
            }
            getViewEvent().run {
                textDidChange { state.onNativeText(it.text) }
                selectionChange { state.onSelection(it) }
                inputFocus { state.onFocusChanged(true) }
                inputBlur { state.onFocusChanged(false) }
                inputReturn { state.blur() }
            }
        },
        viewUpdate = { view ->
            view.getViewAttr().run {
                editable(enabled)
                val digits = charset == OtpCharset.DIGITS
                // H5 的数字输入框不支持设置光标位置，也会拒绝粘贴含文字的短信，统一使用文本输入框，
                // 由宿主根据 otpKeyboard 设置 inputmode。
                if (digits && !view.getPager().pageData.isWeb) {
                    keyboardTypeNumber()
                } else {
                    InputAttr.KEYBOARD_TYPE with "text"
                }
                PROP_OTP_KEYBOARD with if (digits) "numeric" else "text"
                PROP_OTP_AUTOFILL with if (smsAutofill) "on" else "off"
            }
        },
    )
}

private class OtpCellStyle(val background: Color, val border: Color, val borderWidth: Dp)

private fun cellStyle(
    colors: OtpInputColors,
    enabled: Boolean,
    status: OtpInputStatus,
    active: Boolean,
    filled: Boolean,
    borderWidth: Dp,
    activeBorderWidth: Dp,
): OtpCellStyle {
    val width = if (active) activeBorderWidth else borderWidth
    return when {
        !enabled -> OtpCellStyle(colors.disabledCell, colors.disabledBorder, borderWidth)
        status == OtpInputStatus.ERROR -> OtpCellStyle(colors.errorCell, colors.error, width)
        status == OtpInputStatus.SUCCESS -> OtpCellStyle(colors.successCell, colors.success, width)
        active -> OtpCellStyle(colors.cell, colors.activeBorder, width)
        filled -> OtpCellStyle(colors.cell, colors.filledBorder, width)
        else -> OtpCellStyle(colors.cell, colors.border, width)
    }
}

// 设置在原生输入框上、由宿主处理的自定义属性，宿主接入方式见 README。
// otpAutofill 取 "on" / "off"；otpKeyboard 取 "numeric" / "text"。
private const val PROP_OTP_AUTOFILL = "otpAutofill"
private const val PROP_OTP_KEYBOARD = "otpKeyboard"

private val CURSOR_WIDTH = 2.dp
private const val CURSOR_HEIGHT_RATIO = 1.1f
private const val CURSOR_BLINK_MS = 530L
