package com.kuikly.otp

/** Kuikly DSL 和 Compose DSL 示例页共用的文案和颜色。 */
internal object DemoOtp {
    const val TEXT = 0xFF1F2328L
    const val SECONDARY = 0xFF667085L
    const val PRIMARY = 0xFF1F6FEBL
    const val SURFACE = 0xFFF2F4F7L
    const val SUCCESS = 0xFF12B76AL
    const val ERROR = 0xFFD92D20L

    fun expected(length: Int, digits: Boolean): String =
        (if (digits) "123456789012" else "A1B2C3D4E5F6").take(length)

    /** 模拟粘贴的整段短信，组件会从中提取验证码。 */
    fun sms(code: String): String = "【示例】您的验证码为 $code，5 分钟内有效。"

    /** [verified] 为 null 表示尚未填满。 */
    fun hint(code: String, verified: Boolean?): String = when (verified) {
        true -> "校验通过：$code"
        false -> "验证码错误：$code"
        null -> if (code.isEmpty()) "等待输入" else "已输入：$code"
    }

    fun hintColor(verified: Boolean?): Long = when (verified) {
        true -> SUCCESS
        false -> ERROR
        null -> SECONDARY
    }
}
