package com.kuikly.otp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.ComposeContainer
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Button
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.timer.setTimeout

@Page("otp_input_compose_demo")
class OtpInputComposeDemoPage : ComposeContainer() {
    override fun willInit() {
        super.willInit()
        setContent {
            OtpInputComposeDemo(
                preset = pageData.params.optString("preset", ""),
                visual = pageData.params.optString("state", ""),
            )
        }
    }
}

@Composable
private fun OtpInputComposeDemo(preset: String, visual: String) {
    val controller = remember { OtpInputController() }
    // Product-facing status; empty by default.
    var hint by remember { mutableStateOf("") }
    var fieldState by remember { mutableStateOf(OtpInputState.Normal) }
    var obscure by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        if (preset.isNotEmpty()) {
            setTimeout(50) {
                val nextState = visualState(visual)
                fieldState = nextState
                controller.setCode(preset)
                hint = if (nextState == OtpInputState.Error) "验证码不正确" else "已填满"
            }
        }
        onDispose { }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
    ) {
        Text(
            text = "验证码",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2328),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "6 位。可粘贴整串短信，或点「粘贴整串」。",
            fontSize = 14.sp,
            color = Color(0xFF667085),
        )
        Spacer(modifier = Modifier.height(16.dp))
        OtpInput(
            modifier = Modifier.fillMaxWidth(),
            length = 6,
            obscure = obscure,
            state = fieldState,
            controller = controller,
            onChange = { code ->
                if (fieldState != OtpInputState.Error) {
                    hint = if (code.isEmpty()) "" else "已输入"
                }
            },
            onComplete = {
                if (fieldState != OtpInputState.Error) {
                    hint = "已填满"
                }
            },
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            DemoButton(text = "粘贴整串", primary = true, modifier = Modifier.weight(1f)) {
                controller.setCode("短信验证码839201，请勿泄露")
                fieldState = OtpInputState.Normal
                hint = "已填满"
            }
            Spacer(modifier = Modifier.width(8.dp))
            DemoButton(text = "清空", primary = false, modifier = Modifier.weight(1f)) {
                controller.clear()
                fieldState = OtpInputState.Normal
                hint = ""
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            DemoButton(text = "错误态", primary = false, modifier = Modifier.weight(1f)) {
                fieldState = OtpInputState.Error
                hint = "验证码不正确"
            }
            Spacer(modifier = Modifier.width(8.dp))
            DemoButton(text = "成功态", primary = false, modifier = Modifier.weight(1f)) {
                fieldState = OtpInputState.Success
                hint = "已填满"
            }
            Spacer(modifier = Modifier.width(8.dp))
            DemoButton(
                text = if (obscure) "明文" else "密文",
                primary = false,
                modifier = Modifier.weight(1f),
            ) {
                obscure = !obscure
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = hint,
            fontSize = 14.sp,
            color = if (fieldState == OtpInputState.Error) Color(0xFFD92D20) else Color(0xFF667085),
            modifier = Modifier.height(22.dp),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "4 位密文", fontSize = 14.sp, color = Color(0xFF667085))
        Spacer(modifier = Modifier.height(8.dp))
        OtpInput(
            modifier = Modifier.fillMaxWidth(),
            length = 4,
            obscure = true,
        )
    }
}

@Composable
private fun DemoButton(
    text: String,
    primary: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val background = if (primary) Color(0xFF1F6FEB) else Color(0xFFF2F4F7)
    val content = if (primary) Color.White else Color(0xFF1F2328)
    Button(
        onClick = onClick,
        modifier = modifier
            .height(40.dp)
            .background(background, RoundedCornerShape(8.dp)),
    ) {
        Text(text = text, fontSize = 14.sp, color = content)
    }
}
