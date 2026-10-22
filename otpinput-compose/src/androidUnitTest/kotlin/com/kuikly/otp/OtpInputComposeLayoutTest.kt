package com.kuikly.otp

import com.tencent.kuikly.compose.ComposeContainer
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.core.base.ViewConst
import com.tencent.kuikly.core.manager.BridgeManager
import com.tencent.kuikly.core.manager.KotlinMethod
import com.tencent.kuikly.core.manager.NativeMethod
import com.tencent.kuikly.core.manager.PagerManager
import com.tencent.kuikly.core.nvi.NativeBridge
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Renders the Compose OtpInput in a real ComposeContainer on the JVM and checks the
 * frames Compose sends to the native layer on the first frame.
 */
class OtpInputComposeLayoutTest {
    private val bridge = RecordingBridge()
    private val pagerIds = mutableListOf<String>()

    @AfterTest
    fun tearDown() {
        pagerIds.forEach { BridgeManager.callKotlinMethod(KotlinMethod.DESTROY_INSTANCE, it) }
    }

    private fun render(length: Int): ComposeHostPage {
        val pagerId = "otp-compose-test-${nextId++}"
        val pageName = "otp_compose_test_$pagerId"
        PagerManager.registerPageRouter(pageName) { ComposeHostPage(length) }
        BridgeManager.registerNativeBridge(pagerId, NativeBridge().also { it.delegate = bridge })
        BridgeManager.callKotlinMethod(
            KotlinMethod.CREATE_INSTANCE,
            pagerId,
            pageName,
            """{"rootViewWidth":360,"rootViewHeight":640,"platform":"android","density":1,"param":{}}""",
        )
        pagerIds += pagerId
        return PagerManager.getPager(pagerId) as ComposeHostPage
    }

    private fun rowWidth(length: Int) = length * OtpCellStyle.Width + (length - 1) * OtpCellStyle.Gap

    private fun assertRow(length: Int) {
        val cells = bridge.liveFrames().values
            .filter { it.width == OtpCellStyle.Width && it.height == OtpInputHeight }
        assertEquals(
            (0 until length).map { it * (OtpCellStyle.Width + OtpCellStyle.Gap) },
            cells.map { it.x }.sorted(),
        )
        val input = bridge.liveFrames().getValue(bridge.inputTag())
        assertEquals(rowWidth(length), input.width)
        assertEquals(OtpInputHeight, input.height)
    }

    @Test
    fun cellsAndInputGetFramesFromCompose() {
        render(length = 6)
        assertRow(6)
    }

    @Test
    fun fourDigitRow() {
        render(length = 4)
        assertRow(4)
    }

    @Test
    fun controllerPasteReportsOnceAndPinsCursor() {
        val page = render(length = 6)
        page.controller.setCode("短信验证码839201，请勿泄露")
        page.controller.setCode("839201")

        assertEquals(listOf("839201"), page.changes)
        assertEquals(listOf("839201"), page.completions)
        assertEquals("839201", bridge.viewMethodParams(bridge.inputTag(), "setText").last())
        assertEquals("6", bridge.viewMethodParams(bridge.inputTag(), "setCursorIndex").last())
    }

    private companion object {
        var nextId = 0
    }
}

internal class ComposeHostPage(private val length: Int) : ComposeContainer() {
    val controller = OtpInputController()
    val changes = mutableListOf<String>()
    val completions = mutableListOf<String>()

    override fun willInit() {
        super.willInit()
        setContent {
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                OtpInput(
                    length = length,
                    controller = controller,
                    onChange = { changes += it },
                    onComplete = { completions += it },
                )
            }
        }
    }
}

internal class RecordingBridge : NativeBridge.NativeBridgeDelegate {
    class Frame(val x: Float, val width: Float, val height: Float)

    private class Call(val method: Int, val args: List<Any?>)

    private val calls = mutableListOf<Call>()

    override fun callNative(
        methodId: Int,
        arg0: Any?,
        arg1: Any?,
        arg2: Any?,
        arg3: Any?,
        arg4: Any?,
        arg5: Any?,
    ): Any? {
        calls += Call(methodId, listOf(arg0, arg1, arg2, arg3, arg4, arg5))
        return if (methodId == NativeMethod.CALCULATE_RENDER_VIEW_SIZE) "12|24" else null
    }

    fun inputTag(): Int = calls
        .last { it.method == NativeMethod.CREATE_RENDER_VIEW && it.args[2] == ViewConst.TYPE_TEXT_FIELD }
        .args[1] as Int

    /** Last frame of each render view that has not been removed. */
    fun liveFrames(): Map<Int, Frame> {
        val frames = linkedMapOf<Int, Frame>()
        for (call in calls) {
            val tag = call.args[1] as? Int ?: continue
            when (call.method) {
                NativeMethod.SET_RENDER_VIEW_FRAME -> frames[tag] = Frame(
                    (call.args[2] as Number).toFloat(),
                    (call.args[4] as Number).toFloat(),
                    (call.args[5] as Number).toFloat(),
                )
                NativeMethod.REMOVE_RENDER_VIEW -> frames.remove(tag)
            }
        }
        return frames
    }

    fun viewMethodParams(tag: Int, method: String): List<String?> = calls
        .filter { it.method == NativeMethod.CALL_VIEW_METHOD && it.args[1] == tag && it.args[2] == method }
        .map { it.args[3] as String? }
}
