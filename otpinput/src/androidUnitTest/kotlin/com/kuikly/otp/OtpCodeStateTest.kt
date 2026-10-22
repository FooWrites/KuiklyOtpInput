package com.kuikly.otp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OtpCodeStateTest {
    @Test
    fun acceptKeepsDigitsAndSkipsUnchangedText() {
        val state = OtpCodeState(6)
        val change = state.accept("8a3")!!
        assertEquals("83", change.code)
        assertFalse(change.completed)
        assertNull(state.accept("83"))
        assertNull(state.accept("x8 3"))
    }

    @Test
    fun completesOncePerFullCode() {
        val state = OtpCodeState(4)
        assertTrue(state.accept("1234")!!.completed)
        assertNull(state.accept("1234"))
        assertNull(state.accept("12345"))

        val other = state.accept("5678")!!
        assertTrue(other.completed)

        assertFalse(state.accept("567")!!.completed)
        assertTrue(state.accept("5678")!!.completed)
    }

    @Test
    fun shorterLengthTrimsAndCompletes() {
        val state = OtpCodeState(6)
        state.accept("12345")
        val change = state.setLength(4)!!
        assertEquals("1234", change.code)
        assertTrue(change.completed)
        assertNull(state.setLength(4))
    }

    @Test
    fun shorterLengthReachingFullCompletesWithoutTrim() {
        val state = OtpCodeState(6)
        state.accept("1234")
        val change = state.setLength(4)!!
        assertEquals("1234", change.code)
        assertTrue(change.completed)
    }

    @Test
    fun longerLengthReopensCompletion() {
        val state = OtpCodeState(4)
        state.accept("1234")
        assertNull(state.setLength(6))
        assertFalse(state.accept("12345")!!.completed)
        assertTrue(state.accept("123456")!!.completed)
    }

    @Test
    fun lengthIsClamped() {
        assertEquals(OTP_MAX_LENGTH, OtpCodeState(20).length)
        val state = OtpCodeState(6)
        state.setLength(0)
        assertEquals(OTP_MIN_LENGTH, state.length)
    }

    @Test
    fun activeCellFollowsCodeLengthOnlyWhenFocused() {
        val active = OtpCellStyle.colors(2, "12", 6, OtpInputState.Normal, focused = true).border
        assertEquals(0xFF1F6FEBL, active)
        assertTrue(active != OtpCellStyle.colors(2, "12", 6, OtpInputState.Normal, focused = false).border)
        assertTrue(active != OtpCellStyle.colors(1, "12", 6, OtpInputState.Normal, focused = true).border)
        assertEquals("•", OtpCellStyle.glyph(0, "1", obscure = true))
        assertEquals("", OtpCellStyle.glyph(1, "1", obscure = true))
    }
}
