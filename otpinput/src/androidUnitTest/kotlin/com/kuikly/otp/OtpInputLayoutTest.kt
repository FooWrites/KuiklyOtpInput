package com.kuikly.otp

import com.tencent.kuikly.core.layout.FlexLayout
import com.tencent.kuikly.core.layout.FlexNode
import com.tencent.kuikly.core.layout.FlexPositionType
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Absolute OTP cells and the transparent Input leave normal flow.
 * The component itself must still occupy a row, with no outer height wrapper.
 */
class OtpInputLayoutTest {
    @Test
    fun reservesRowHeightWhenCellsAreAbsolute() {
        val otp = OtpInputView()
        otp.getViewAttr()

        val overlay = FlexNode()
        overlay.positionType = FlexPositionType.ABSOLUTE
        overlay.setStylePosition(FlexLayout.PositionType.POSITION_TOP, 0f)
        overlay.setStylePosition(FlexLayout.PositionType.POSITION_BOTTOM, 0f)
        overlay.setStylePosition(FlexLayout.PositionType.POSITION_LEFT, 0f)
        overlay.setStylePosition(FlexLayout.PositionType.POSITION_RIGHT, 0f)
        val cell = FlexNode()
        cell.styleWidth = 40f
        cell.styleHeight = 48f
        overlay.addChildAt(cell, 0)
        otp.flexNode.addChildAt(overlay, 0)

        val column = FlexNode()
        column.styleWidth = 360f
        val button = FlexNode()
        button.styleHeight = 40f
        button.styleWidth = 120f
        column.addChildAt(otp.flexNode, 0)
        column.addChildAt(button, 1)
        column.calculateLayout(null)

        assertEquals(48f, otp.flexNode.layoutHeight)
        assertEquals(48f, button.layoutY)
    }
}
