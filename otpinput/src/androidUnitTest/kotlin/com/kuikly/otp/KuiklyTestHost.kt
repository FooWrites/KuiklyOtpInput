package com.kuikly.otp

import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.manager.BridgeManager
import com.tencent.kuikly.core.manager.KotlinMethod
import com.tencent.kuikly.core.manager.NativeMethod
import com.tencent.kuikly.core.manager.PagerManager
import com.tencent.kuikly.core.nvi.NativeBridge
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.handler.observable

/**
 * Runs a real Kuikly pager on the JVM. Native calls go to [RecordingBridge] instead of a
 * render layer, so tests can check Flex frames and what the native tree was told.
 */
internal class KuiklyTestHost<P : Pager>(createPager: () -> P) {
    val bridge = RecordingBridge()
    val pagerId = "otp-test-${nextId++}"
    val pager: P

    init {
        val pageName = "otp_test_page_$pagerId"
        PagerManager.registerPageRouter(pageName, createPager)
        BridgeManager.registerNativeBridge(pagerId, NativeBridge().also { it.delegate = bridge })
        BridgeManager.callKotlinMethod(
            KotlinMethod.CREATE_INSTANCE,
            pagerId,
            pageName,
            """{"rootViewWidth":360,"rootViewHeight":640,"platform":"android","param":{}}""",
        )
        @Suppress("UNCHECKED_CAST")
        pager = PagerManager.getPager(pagerId) as P
        layout()
    }

    fun layout() {
        BridgeManager.callKotlinMethod(KotlinMethod.LAYOUT_VIEW, pagerId)
    }

    fun fireViewEvent(viewRef: Int, event: String, data: String) {
        BridgeManager.callKotlinMethod(KotlinMethod.FIRE_VIEW_EVENT, pagerId, viewRef, event, data)
    }

    fun destroy() {
        BridgeManager.callKotlinMethod(KotlinMethod.DESTROY_INSTANCE, pagerId)
    }

    private companion object {
        var nextId = 0
    }
}

internal class RecordingBridge : NativeBridge.NativeBridgeDelegate {
    class Call(val method: Int, val args: List<Any?>)

    val calls = mutableListOf<Call>()

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

    /** Params passed to `callViewMethod(tag, method)`, in order. */
    fun viewMethodParams(tag: Int, method: String): List<String?> = calls
        .filter { it.method == NativeMethod.CALL_VIEW_METHOD && it.args[1] == tag && it.args[2] == method }
        .map { it.args[3] as String? }

    fun removedRenderViews(): Set<Int> = calls
        .filter { it.method == NativeMethod.REMOVE_RENDER_VIEW }
        .map { it.args[1] as Int }
        .toSet()
}

internal class OtpHostPager : Pager() {
    var otp: OtpInputView? = null
    var length by observable(6)
    val changes = mutableListOf<String>()
    val completions = mutableListOf<String>()

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                alignItemsCenter()
            }
            OtpInput {
                ref { ctx.otp = it.view }
                attr {
                    length(ctx.length)
                }
                event {
                    onChange { ctx.changes += it }
                    onComplete { ctx.completions += it }
                }
            }
        }
    }
}
