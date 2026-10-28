package com.kuikly.otp.compose

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OtpInputStateTest {
    private val state = OtpInputState()
    private val changes = mutableListOf<String>()
    private val completions = mutableListOf<String>()

    init {
        state.onChange = { changes += it }
        state.onComplete = { completions += it }
        state.applyRules(4, OtpCharset.DIGITS)
    }

    @Test
    fun setCodeFiltersAndCompletes() {
        state.setCode("code: 12-34")

        assertEquals("1234", state.code)
        assertTrue(state.isComplete)
        assertEquals(listOf("1234"), changes)
        assertEquals(listOf("1234"), completions)
    }

    @Test
    fun nativeTextAppendsAndDeletes() {
        state.onNativeText("1")
        state.onNativeText("12")
        state.onNativeText("1")

        assertEquals("1", state.code)
        assertEquals(listOf("1", "12", "1"), changes)
        assertTrue(completions.isEmpty())
    }

    @Test
    fun shrinkingLengthCompletesWithoutChange() {
        val wide = OtpInputState()
        wide.onChange = { changes += it }
        wide.onComplete = { completions += it }
        wide.applyRules(6, OtpCharset.DIGITS)
        wide.setCode("1234")
        changes.clear()

        wide.applyRules(4, OtpCharset.DIGITS)

        assertTrue(changes.isEmpty())
        assertEquals(listOf("1234"), completions)
    }

    @Test
    fun clearResetsCode() {
        state.setCode("1234")
        state.clear()

        assertEquals("", state.code)
        assertFalse(state.isComplete)
        assertEquals(listOf("1234", ""), changes)
    }
}
