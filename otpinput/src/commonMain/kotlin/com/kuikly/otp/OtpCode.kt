package com.kuikly.otp

internal const val OTP_MIN_LENGTH = 1
internal const val OTP_MAX_LENGTH = 8

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
