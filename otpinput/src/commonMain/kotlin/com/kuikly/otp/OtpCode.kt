package com.kuikly.otp

/** 验证码允许输入的字符。 */
enum class OtpCharset {
    /** 只允许数字 0-9，弹出数字键盘。 */
    DIGITS,

    /** 允许数字和英文字母，字母统一转为大写，弹出文本键盘。 */
    ALPHANUMERIC,
}

internal const val OTP_MIN_LENGTH = 1
internal const val OTP_MAX_LENGTH = 12

/** [changed] 表示文本有变化；[completed] 表示刚好进入一个新的完整验证码，同一个完整验证码只报告一次。 */
internal class OtpCodeChange(val code: String, val changed: Boolean, val completed: Boolean)

/**
 * 验证码文本与完成状态，不依赖视图。
 *
 * 底层输入框的文本始终与 [code] 一致，因此 [input] 可以通过前缀判断本次是追加还是整体替换：
 * 一次插入的内容过滤后足够填满全部格子（粘贴整段短信、输入法或系统一次写入验证码）时，用从中提取的验证码替换原有内容；
 * 否则按原文过滤后截取到 [length]。
 *
 * 提取验证码时，按不允许的字符把文本切分成段，取长度恰好为 [length] 的段，`ALPHANUMERIC` 下优先取含数字的段，
 * 以避开短信中的日期、尾号等其他数字和签名等单词；没有这样的段时按过滤后的前 [length] 个字符。
 */
internal class OtpCode(length: Int, charset: OtpCharset) {
    var length: Int = clampLength(length)
        private set
    var charset: OtpCharset = charset
        private set
    var code: String = ""
        private set
    private var completedCode: String? = null

    val isComplete: Boolean
        get() = code.length == length

    /** 底层输入框文本变化。没有需要通知的变化时返回 null。 */
    fun input(raw: String): OtpCodeChange? {
        val inserted = if (raw.length > code.length && raw.startsWith(code)) raw.substring(code.length) else ""
        val next = if (filter(inserted).length >= length) extract(inserted) else filter(raw).take(length)
        return commit(next)
    }

    /** 整体替换为从 [raw] 中提取的验证码。 */
    fun replace(raw: String): OtpCodeChange? = commit(extract(raw))

    fun updateLength(value: Int): OtpCodeChange? {
        val next = clampLength(value)
        if (next == length) {
            return null
        }
        length = next
        return commit(code.take(next))
    }

    fun updateCharset(value: OtpCharset): OtpCodeChange? {
        if (value == charset) {
            return null
        }
        charset = value
        return commit(filter(code).take(length))
    }

    private fun extract(raw: String): String {
        val segments = mutableListOf<String>()
        val current = StringBuilder()
        for (ch in raw) {
            val accepted = accept(ch)
            if (accepted != null) {
                current.append(accepted)
            } else if (current.isNotEmpty()) {
                segments.add(current.toString())
                current.clear()
            }
        }
        if (current.isNotEmpty()) {
            segments.add(current.toString())
        }
        val exact = segments.filter { it.length == length }
        return exact.firstOrNull { segment -> segment.any { it in '0'..'9' } }
            ?: exact.firstOrNull()
            ?: segments.joinToString("").take(length)
    }

    private fun filter(raw: String): String {
        val out = StringBuilder()
        for (ch in raw) {
            accept(ch)?.let(out::append)
        }
        return out.toString()
    }

    private fun accept(ch: Char): Char? = when {
        ch in '0'..'9' -> ch
        charset == OtpCharset.ALPHANUMERIC && ch in 'a'..'z' -> ch.uppercaseChar()
        charset == OtpCharset.ALPHANUMERIC && ch in 'A'..'Z' -> ch
        else -> null
    }

    private fun commit(next: String): OtpCodeChange? {
        val changed = next != code
        code = next
        val full = next.length == length
        val completed = full && completedCode != next
        completedCode = if (full) next else null
        return if (changed || completed) OtpCodeChange(next, changed, completed) else null
    }

    companion object {
        fun clampLength(value: Int): Int = value.coerceIn(OTP_MIN_LENGTH, OTP_MAX_LENGTH)
    }
}
