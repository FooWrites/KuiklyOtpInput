package com.kuikly.otp

import android.os.Build
import android.view.View
import android.widget.EditText
import com.tencent.kuikly.core.render.android.export.IKuiklyRenderViewExport
import com.tencent.kuikly.core.render.android.export.IKuiklyRenderViewPropExternalHandler

/**
 * 处理 OtpInput 设置在原生输入框上的 `otpAutofill` 属性：声明 Autofill 提示 `smsOTPCode`，
 * 由系统自动填充服务或输入法提供短信验证码建议。Android 8.0 以下不处理。
 */
object KROtpAutofillHandler : IKuiklyRenderViewPropExternalHandler {
    private const val PROP_OTP_AUTOFILL = "otpAutofill"
    private const val VALUE_OFF = "off"

    // 与 androidx.autofill.HintConstants.AUTOFILL_HINT_SMS_OTP 相同
    private const val AUTOFILL_HINT_SMS_OTP = "smsOTPCode"

    override fun setViewExternalProp(
        renderViewExport: IKuiklyRenderViewExport,
        propKey: String,
        propValue: Any
    ): Boolean {
        if (propKey != PROP_OTP_AUTOFILL) {
            return false
        }
        (renderViewExport as? EditText)?.let { apply(it, propValue != VALUE_OFF) }
        return true
    }

    override fun resetViewExternalProp(renderViewExport: IKuiklyRenderViewExport, propKey: String): Boolean {
        if (propKey != PROP_OTP_AUTOFILL) {
            return false
        }
        (renderViewExport as? EditText)?.let { apply(it, false) }
        return true
    }

    private fun apply(view: EditText, enabled: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        if (enabled) {
            view.setAutofillHints(AUTOFILL_HINT_SMS_OTP)
            view.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_YES
        } else {
            view.setAutofillHints()
            view.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_AUTO
        }
    }
}
