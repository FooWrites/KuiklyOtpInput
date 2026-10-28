package com.kuikly.otp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kuikly.otp.compose.OtpCellShape
import com.kuikly.otp.compose.OtpCharset
import com.kuikly.otp.compose.OtpInput
import com.kuikly.otp.compose.OtpInputStatus
import com.kuikly.otp.compose.rememberOtpInputState
import com.tencent.kuikly.compose.ComposeContainer
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
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
import com.tencent.kuikly.compose.material3.ButtonDefaults
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

@Page("otp_input_compose_demo")
internal class OtpInputComposeDemoPage : ComposeContainer() {
    override fun willInit() {
        super.willInit()
        setContent {
            OtpInputDemo(pagerData.statusBarHeight)
        }
    }
}

private enum class DemoAlignment(val title: String) {
    START("靠左"),
    CENTER("居中"),
    SPACE_BETWEEN("两端"),
}

@Composable
private fun OtpInputDemo(statusBarHeight: Float) {
    val state = rememberOtpInputState()
    var length by remember { mutableStateOf(6) }
    var charset by remember { mutableStateOf(OtpCharset.DIGITS) }
    var cellShape by remember { mutableStateOf(OtpCellShape.BOX) }
    var alignment by remember { mutableStateOf(DemoAlignment.START) }
    var mask by remember { mutableStateOf(false) }
    var enabled by remember { mutableStateOf(true) }
    var verified by remember { mutableStateOf<Boolean?>(null) }
    val expectedCode = DemoOtp.expected(length, charset == OtpCharset.DIGITS)
    val spacing = if (length > 6) 6.dp else 8.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(start = 16.dp, top = (statusBarHeight + 16).dp, end = 16.dp, bottom = 16.dp),
    ) {
        Text(text = "Compose DSL 验证码输入框", fontSize = 20.sp, color = Color(DemoOtp.TEXT))
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "输入 $expectedCode 校验通过，其他内容校验失败。",
            fontSize = 14.sp,
            color = Color(DemoOtp.SECONDARY),
        )
        Spacer(modifier = Modifier.height(20.dp))
        OtpInput(
            state = state,
            modifier = Modifier.fillMaxWidth(),
            length = length,
            charset = charset,
            enabled = enabled,
            mask = mask,
            placeholderChar = "-",
            status = when (verified) {
                true -> OtpInputStatus.SUCCESS
                false -> OtpInputStatus.ERROR
                null -> OtpInputStatus.NORMAL
            },
            cellShape = cellShape,
            cellWidth = if (length > 6) 36.dp else 44.dp,
            horizontalArrangement = when (alignment) {
                DemoAlignment.START -> Arrangement.spacedBy(spacing)
                DemoAlignment.CENTER -> Arrangement.spacedBy(spacing, Alignment.CenterHorizontally)
                DemoAlignment.SPACE_BETWEEN -> Arrangement.SpaceBetween
            },
            onChange = { verified = null },
            onComplete = { code -> verified = code == expectedCode },
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = DemoOtp.hint(state.code, verified),
            fontSize = 14.sp,
            color = Color(DemoOtp.hintColor(verified)),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            DemoButton("粘贴短信", primary = true, modifier = Modifier.weight(1f)) {
                state.setCode(DemoOtp.sms(expectedCode))
            }
            Spacer(modifier = Modifier.width(8.dp))
            DemoButton("清空", primary = false, modifier = Modifier.weight(1f)) {
                state.clear()
            }
            Spacer(modifier = Modifier.width(8.dp))
            DemoButton("聚焦", primary = false, modifier = Modifier.weight(1f)) {
                state.focus()
            }
        }
        // 位数和字符集变化后内容可能被截断或重新填满，先清除校验结果，由组件回调重新给出。
        DemoOptions("位数", listOf("4" to 4, "6" to 6, "8" to 8), length) {
            verified = null
            length = it
        }
        DemoOptions("字符", listOf("数字" to OtpCharset.DIGITS, "字母数字" to OtpCharset.ALPHANUMERIC), charset) {
            verified = null
            charset = it
        }
        DemoOptions("样式", listOf("方框" to OtpCellShape.BOX, "下划线" to OtpCellShape.UNDERLINE), cellShape) {
            cellShape = it
        }
        DemoOptions("对齐", DemoAlignment.entries.map { it.title to it }, alignment) {
            alignment = it
        }
        DemoOptions("遮挡", listOf("关" to false, "开" to true), mask) {
            mask = it
        }
        DemoOptions("可编辑", listOf("是" to true, "否" to false), enabled) {
            enabled = it
        }
    }
}

@Composable
private fun DemoButton(title: String, primary: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(if (primary) DemoOtp.PRIMARY else DemoOtp.SURFACE),
            contentColor = if (primary) Color.White else Color(DemoOtp.TEXT),
        ),
    ) {
        Text(text = title, fontSize = 15.sp)
    }
}

@Composable
private fun <T> DemoOptions(label: String, values: List<Pair<String, T>>, selected: T, onSelect: (T) -> Unit) {
    Spacer(modifier = Modifier.height(14.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(DemoOtp.SECONDARY),
            modifier = Modifier.width(64.dp),
        )
        for ((name, value) in values) {
            val active = value == selected
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .background(Color(if (active) DemoOtp.PRIMARY else DemoOtp.SURFACE), RoundedCornerShape(14.dp))
                    .clickable { onSelect(value) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(text = name, fontSize = 13.sp, color = if (active) Color.White else Color(DemoOtp.TEXT))
            }
        }
    }
}
