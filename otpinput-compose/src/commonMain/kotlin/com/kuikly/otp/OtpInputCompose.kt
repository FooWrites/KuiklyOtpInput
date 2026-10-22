package com.kuikly.otp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.tencent.kuikly.compose.extension.MakeKuiklyComposeNode
import com.tencent.kuikly.compose.foundation.layout.requiredHeight
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.unit.dp

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
    MakeKuiklyComposeNode(
        factory = { OtpInputView() },
        // Column measurement uses the incoming max height unless this row reserves one.
        // requiredHeight keeps that 48.dp; a height() on the caller modifier is not an override.
        modifier = modifier.requiredHeight(OtpInputHeight.dp),
        viewUpdate = { view ->
            controller.attach(view)
            view.getViewAttr().apply {
                length(length)
                obscure(obscure)
                state(state)
                autofocus(autofocus)
            }
            view.getViewEvent().apply {
                onChange(onChange)
                onComplete(onComplete)
            }
        },
    )
}
