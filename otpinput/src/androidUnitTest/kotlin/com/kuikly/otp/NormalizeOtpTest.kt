package com.kuikly.otp

import kotlin.test.Test
import kotlin.test.assertEquals

class NormalizeOtpTest {
    @Test
    fun keepsDigits() {
        assertEquals("839201", normalizeOtp("839201", 6))
    }

    @Test
    fun pasteFullSmsTextDistributesDigits() {
        assertEquals("839201", normalizeOtp("短信验证码839201，请勿泄露", 6))
    }

    @Test
    fun stripsSeparatorsAndExtraDigits() {
        assertEquals("839201", normalizeOtp("83 92-01 99", 6))
    }

    @Test
    fun supportsFourDigits() {
        assertEquals("1234", normalizeOtp("12ab34xx", 4))
    }

    @Test
    fun emptyWhenNoDigits() {
        assertEquals("", normalizeOtp("abc", 6))
    }

    @Test
    fun clampsLength() {
        assertEquals(4, clampOtpLength(4))
        assertEquals(6, clampOtpLength(6))
        assertEquals(OTP_MIN_LENGTH, clampOtpLength(0))
        assertEquals(OTP_MAX_LENGTH, clampOtpLength(20))
    }
}
