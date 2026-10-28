package com.kuikly.otp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OtpCodeTest {
    private val otp = OtpCode(length = 4, charset = OtpCharset.DIGITS)

    @Test
    fun typingAppendsUntilFull() {
        assertEquals("1", otp.input("1")?.code)
        assertEquals("12", otp.input("12")?.code)
        assertEquals("123", otp.input("123")?.code)
        val last = assertNotNull(otp.input("1234"))

        assertTrue(last.changed)
        assertTrue(last.completed)
        assertTrue(otp.isComplete)
    }

    @Test
    fun typingBeyondLengthIsIgnored() {
        otp.input("1234")

        assertNull(otp.input("12345"))
        assertEquals("1234", otp.code)
    }

    @Test
    fun deletingRemovesFromEnd() {
        otp.input("1234")

        val change = assertNotNull(otp.input("123"))

        assertEquals("123", change.code)
        assertFalse(change.completed)
        assertEquals("", otp.input("")?.code)
    }

    @Test
    fun filtersCharactersOutsideCharset() {
        assertEquals("12", otp.input("1a2-")?.code)
    }

    @Test
    fun pastedTextExtractsCode() {
        val change = assertNotNull(otp.input("Your code is 8391, valid for 5 min"))

        assertEquals("8391", change.code)
        assertTrue(change.completed)
    }

    @Test
    fun pastedTextPrefersSegmentOfExactLength() {
        val code = OtpCode(length = 6, charset = OtpCharset.DIGITS)

        assertEquals("482913", code.replace("【银行】2026年10月 尾号1234的账户验证码482913")?.code)
        assertEquals("778899", code.input("4829131234 code 778899")?.code)
    }

    @Test
    fun alphanumericPrefersSegmentWithDigits() {
        val code = OtpCode(length = 6, charset = OtpCharset.ALPHANUMERIC)

        assertEquals("A1B2C3", code.replace("【Kuikly】验证码 a1b2c3")?.code)
    }

    @Test
    fun spacedCodeFallsBackToFilteredText() {
        assertEquals("1234", otp.replace("12 34")?.code)
    }

    @Test
    fun fullInsertionReplacesExistingCode() {
        otp.input("12")

        assertEquals("5678", otp.input("125678")?.code)
        assertEquals("1234", otp.replace("1234")?.code)
        assertEquals("9012", otp.input("1234 code: 9012")?.code)
    }

    @Test
    fun partialInsertionAppends() {
        otp.input("1")

        assertEquals("123", otp.input("123")?.code)
    }

    @Test
    fun completionIsReportedOncePerCode() {
        assertTrue(otp.replace("1234")!!.completed)
        assertNull(otp.replace("1234"))

        otp.input("123")
        assertTrue(otp.input("1234")!!.completed)
    }

    @Test
    fun alphanumericUppercasesLetters() {
        val code = OtpCode(length = 6, charset = OtpCharset.ALPHANUMERIC)

        assertEquals("AB12C", code.input("ab-12 c")?.code)
    }

    @Test
    fun switchingCharsetFiltersExistingCode() {
        val code = OtpCode(length = 6, charset = OtpCharset.ALPHANUMERIC)
        code.input("A1B2")

        assertEquals("12", code.updateCharset(OtpCharset.DIGITS)?.code)
        assertNull(code.updateCharset(OtpCharset.DIGITS))
    }

    @Test
    fun shrinkingLengthTrimsAndCompletes() {
        val code = OtpCode(length = 6, charset = OtpCharset.DIGITS)
        code.input("1234")

        val change = assertNotNull(code.updateLength(4))

        assertFalse(change.changed)
        assertTrue(change.completed)
        assertEquals("12", code.updateLength(2)?.code)
    }

    @Test
    fun lengthIsClamped() {
        assertEquals(OTP_MIN_LENGTH, OtpCode(0, OtpCharset.DIGITS).length)
        assertEquals(OTP_MAX_LENGTH, OtpCode(99, OtpCharset.DIGITS).length)
    }
}
