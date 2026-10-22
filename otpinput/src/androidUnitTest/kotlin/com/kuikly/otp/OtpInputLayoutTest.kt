package com.kuikly.otp

import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.views.InputView
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class OtpInputLayoutTest {
    private val host = KuiklyTestHost { OtpHostPager() }
    private val otp: OtpInputView get() = host.pager.otp!!

    @AfterTest
    fun tearDown() {
        host.destroy()
    }

    private fun cells() = (otp.getSubviews()[0] as ViewContainer<*, *>).getSubviews()

    private fun input() = otp.getSubviews()[1] as InputView

    private fun rowWidth(length: Int) = length * OtpCellStyle.Width + (length - 1) * OtpCellStyle.Gap

    private fun assertCellsLaidOut(length: Int) {
        val cells = cells()
        assertEquals(OTP_MAX_LENGTH, cells.size)
        cells.forEachIndexed { index, cell ->
            val frame = cell.flexNode.layoutFrame
            if (index < length) {
                assertEquals(OtpCellStyle.Width, frame.width, "cell $index width")
                assertEquals(OtpInputHeight, frame.height, "cell $index height")
                assertEquals(index * (OtpCellStyle.Width + OtpCellStyle.Gap), frame.x, "cell $index x")
            } else {
                assertEquals(0f, frame.width, "hidden cell $index width")
            }
        }
    }

    @Test
    fun componentTakesCellRowWidthUnderCenteredParent() {
        assertEquals(rowWidth(6), otp.flexNode.layoutWidth)
        assertEquals(OtpInputHeight, otp.flexNode.layoutHeight)
        assertEquals((360f - rowWidth(6)) / 2, otp.flexNode.layoutFrame.x)
        val inputFrame = input().flexNode.layoutFrame
        assertEquals(rowWidth(6), inputFrame.width)
        assertEquals(OtpInputHeight, inputFrame.height)
        assertCellsLaidOut(6)
    }

    @Test
    fun lengthChangeAfterMountKeepsViewsInTree() {
        val mountedCells = cells()
        val mountedInput = input()

        for (length in listOf(4, 8, 1, 6)) {
            host.pager.length = length
            host.layout()

            assertEquals(rowWidth(length), otp.flexNode.layoutWidth, "width for length $length")
            assertCellsLaidOut(length)
            mountedCells.zip(cells()).forEach { (before, after) -> assertSame(before, after) }
            assertSame(mountedInput, input())
            assertEquals(rowWidth(length), input().flexNode.layoutFrame.width)
        }
        val mountedRefs = mountedCells.map { it.nativeRef } + mountedInput.nativeRef
        assertTrue(host.bridge.removedRenderViews().intersect(mountedRefs.toSet()).isEmpty())
    }

    @Test
    fun shorterLengthTrimsCodeAndCompletes() {
        otp.setCode("12345")
        host.pager.length = 4
        host.layout()

        assertEquals(listOf("12345", "1234"), host.pager.changes)
        assertEquals(listOf("1234"), host.pager.completions)
        assertEquals("1234", host.bridge.viewMethodParams(input().nativeRef, "setText").last())
    }

    @Test
    fun textChangeKeepsDigitsAndPinsCursorToEnd() {
        val ref = input().nativeRef

        host.fireViewEvent(ref, "textDidChange", """{"text":"12a3"}""")
        assertEquals(listOf("123"), host.bridge.viewMethodParams(ref, "setText"))
        assertEquals("3", host.bridge.viewMethodParams(ref, "setCursorIndex").last())

        host.fireViewEvent(ref, "textDidChange", """{"text":"1293"}""")
        assertEquals(listOf("123"), host.bridge.viewMethodParams(ref, "setText"))
        assertEquals("4", host.bridge.viewMethodParams(ref, "setCursorIndex").last())

        val pins = host.bridge.viewMethodParams(ref, "setCursorIndex").size
        host.fireViewEvent(ref, "inputFocus", """{"text":"1293"}""")
        assertEquals(pins + 1, host.bridge.viewMethodParams(ref, "setCursorIndex").size)
        assertEquals("4", host.bridge.viewMethodParams(ref, "setCursorIndex").last())

        assertEquals(listOf("123", "1293"), host.pager.changes)
    }

    @Test
    fun pastedSmsCompletesOnce() {
        val ref = input().nativeRef
        host.fireViewEvent(ref, "textDidChange", """{"text":"短信验证码839201，请勿泄露"}""")
        host.fireViewEvent(ref, "textDidChange", """{"text":"839201"}""")

        assertEquals(listOf("839201"), host.pager.changes)
        assertEquals(listOf("839201"), host.pager.completions)
        assertEquals(listOf("839201"), host.bridge.viewMethodParams(ref, "setText"))
    }
}
