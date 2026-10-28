package com.kuikly.otp.h5

import com.tencent.kuikly.core.render.web.export.IKuiklyRenderViewExport
import com.tencent.kuikly.core.render.web.export.IKuiklyRenderViewPropExternalHandler
import org.w3c.dom.HTMLInputElement

/**
 * 处理 OtpInput 设置在原生输入框上的属性：
 * `otpAutofill` 对应 `autocomplete="one-time-code"`，支持的浏览器会在键盘上方提供短信验证码；
 * `otpKeyboard` 对应 `inputmode`，组件在 H5 上使用文本输入框，由它弹出数字键盘。
 * 同时去掉输入框的聚焦描边。
 */
class KROtpInputPropHandler : IKuiklyRenderViewPropExternalHandler {
    override fun setViewExternalProp(
        renderViewExport: IKuiklyRenderViewExport,
        propKey: String,
        propValue: Any
    ): Boolean {
        val attribute = attributeOf(propKey) ?: return false
        val input = renderViewExport.ele as? HTMLInputElement ?: return true
        // 输入框透明覆盖在格子上，去掉浏览器默认的聚焦描边
        input.style.outline = "none"
        when (val value = propValue as? String) {
            "on" -> input.setAttribute(attribute, "one-time-code")
            "numeric", "text" -> input.setAttribute(attribute, value)
            else -> input.removeAttribute(attribute)
        }
        return true
    }

    override fun resetViewExternalProp(renderViewExport: IKuiklyRenderViewExport, propKey: String): Boolean {
        val attribute = attributeOf(propKey) ?: return false
        (renderViewExport.ele as? HTMLInputElement)?.removeAttribute(attribute)
        return true
    }

    private fun attributeOf(propKey: String): String? = when (propKey) {
        "otpAutofill" -> "autocomplete"
        "otpKeyboard" -> "inputmode"
        else -> null
    }
}
