package com.kuikly.otp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.extension.MakeKuiklyComposeNode
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.requiredHeight
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.foundation.text.BasicText
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.views.InputView
import com.tencent.kuikly.core.base.Color as KuiklyColor

/**
 * Compose DSL OTP boxes. Cells are Compose Row / Box / BasicText; a transparent numeric
 * Input is laid over them and holds the whole code, same as the 自研 DSL [OtpInputView].
 *
 * The Input is a single native leaf, so Compose gives it a frame directly; there is no
 * Flex-laid-out child tree under it.
 */
@Composable
fun OtpInput(
    modifier: Modifier = Modifier,
    length: Int = 6,
    obscure: Boolean = false,
    state: OtpInputState = OtpInputState.Normal,
    autofocus: Boolean = false,
    controller: OtpInputController = remember { OtpInputController() },
    onChange: (String) -> Unit = {},
    onComplete: (String) -> Unit = {},
) {
    val cellCount = OtpCodeState.clampLength(length)
    val currentOnChange by rememberUpdatedState(onChange)
    val currentOnComplete by rememberUpdatedState(onComplete)
    var code by remember { mutableStateOf("") }
    var focused by remember { mutableStateOf(false) }
    val binding = remember {
        OtpInputBinding { change ->
            code = change.code
            currentOnChange(change.code)
            if (change.completed) {
                currentOnComplete(change.code)
            }
        }
    }
    SideEffect {
        binding.setLength(cellCount)
    }
    DisposableEffect(controller) {
        controller.attach(binding)
        onDispose { controller.detach(binding) }
    }

    // A height() on the caller modifier does not resize the row.
    Box(modifier = modifier.requiredHeight(OtpInputHeight.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(OtpCellStyle.Gap.dp)) {
            for (index in 0 until cellCount) {
                val colors = OtpCellStyle.colors(index, code, cellCount, state, focused)
                val shape = RoundedCornerShape(OtpCellStyle.Radius.dp)
                Box(
                    modifier = Modifier
                        .size(OtpCellStyle.Width.dp, OtpInputHeight.dp)
                        .background(Color(colors.background), shape)
                        .border(OtpCellStyle.BorderWidth.dp, Color(colors.border), shape),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = OtpCellStyle.glyph(index, code, obscure),
                        style = TextStyle(
                            color = Color(OtpCellStyle.TextColor),
                            fontSize = OtpCellStyle.FontSize.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
            }
        }
        MakeKuiklyComposeNode(
            factory = { InputView() },
            modifier = Modifier.matchParentSize(),
            viewInit = {
                getViewAttr().apply {
                    backgroundColor(KuiklyColor.TRANSPARENT)
                    fontSize(16f)
                    color(KuiklyColor.TRANSPARENT)
                    tintColor(KuiklyColor.TRANSPARENT)
                    textAlignLeft()
                    keyboardTypeNumber()
                    imeNoFullscreen(true)
                    autofocus(autofocus)
                }
                getViewEvent().apply {
                    textDidChange { binding.onTextChanged(it.text) }
                    inputFocus {
                        focused = true
                        binding.onFocus()
                    }
                    inputBlur { focused = false }
                }
                binding.attachInput(this)
            },
        )
    }
}
