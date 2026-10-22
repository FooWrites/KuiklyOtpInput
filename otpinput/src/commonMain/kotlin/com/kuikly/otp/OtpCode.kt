package com.kuikly.otp

internal const val OTP_MIN_LENGTH = 1
internal const val OTP_MAX_LENGTH = 8
internal const val OTP_DEFAULT_LENGTH = 6

/**
 * Keep digits only, capped at [length].
 * Spaces, letters, and SMS boilerplate are dropped so a full-code paste or autofill
 * still fills the boxes.
 */
internal fun normalizeOtp(raw: String, length: Int): String {
    if (length <= 0) {
        return ""
    }
    val out = StringBuilder()
    for (ch in raw) {
        if (ch in '0'..'9') {
            out.append(ch)
            if (out.length == length) {
                break
            }
        }
    }
    return out.toString()
}

internal fun clampOtpLength(length: Int): Int {
    return length.coerceIn(OTP_MIN_LENGTH, OTP_MAX_LENGTH)
}

/**
 * Visual state of the boxes. The caller owns transitions; editing does not clear them.
 */
enum class OtpInputState {
    Normal,
    Error,
    Success,
}

/**
 * One edit that the caller should report. [completed] is true only the first time a
 * given full code is reached, so onComplete does not repeat for the same digits.
 */
class OtpCodeChange(val code: String, val completed: Boolean)

/**
 * Digits and onComplete bookkeeping shared by 自研 DSL and Compose DSL.
 * Pure state: no views and no callbacks.
 */
class OtpCodeState(length: Int = OTP_DEFAULT_LENGTH) {
    var length: Int = clampLength(length)
        private set
    var code: String = ""
        private set
    private var completedFor: String? = null

    /** Keeps digits from [raw], capped at [length]. Returns null when nothing is to be reported. */
    fun accept(raw: String): OtpCodeChange? = commit(normalizeOtp(raw, length))

    /** Clamps to 1..8 and drops digits past the new length. */
    fun setLength(value: Int): OtpCodeChange? {
        val next = clampLength(value)
        if (next == length) {
            return null
        }
        length = next
        return commit(code.take(next))
    }

    private fun commit(next: String): OtpCodeChange? {
        val changed = next != code
        code = next
        val full = next.length == length
        val completed = full && completedFor != next
        completedFor = if (full) next else null
        return if (changed || completed) OtpCodeChange(next, completed) else null
    }

    companion object {
        fun clampLength(length: Int): Int = clampOtpLength(length)
    }
}

class OtpCellColors(val border: Long, val background: Long)

/**
 * Box geometry (dp) and palette (ARGB) drawn by both DSLs.
 */
object OtpCellStyle {
    const val Width = 40f
    const val Gap = 8f
    const val Radius = 8f
    const val BorderWidth = 1.5f
    const val FontSize = 20f
    const val TextColor = 0xFF1F2328L

    fun glyph(index: Int, code: String, obscure: Boolean): String = when {
        index >= code.length -> ""
        obscure -> "•"
        else -> code[index].toString()
    }

    fun colors(
        index: Int,
        code: String,
        length: Int,
        state: OtpInputState,
        focused: Boolean,
    ): OtpCellColors = when (state) {
        OtpInputState.Error -> OtpCellColors(0xFFD92D20L, 0xFFFFF6F6L)
        OtpInputState.Success -> OtpCellColors(0xFF12B76AL, 0xFFF3FCF7L)
        OtpInputState.Normal -> when {
            focused && code.length < length && index == code.length -> OtpCellColors(0xFF1F6FEBL, WHITE)
            index < code.length -> OtpCellColors(0xFF1F2328L, WHITE)
            else -> OtpCellColors(0xFFD0D5DDL, WHITE)
        }
    }

    private const val WHITE = 0xFFFFFFFFL
}
