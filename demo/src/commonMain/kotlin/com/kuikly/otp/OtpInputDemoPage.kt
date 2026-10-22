package com.kuikly.otp

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.ViewRef
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.timer.setTimeout
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

@Page("otp_input_demo")
class OtpInputDemoPage : Pager() {
    private var primaryRef: ViewRef<OtpInputView>? = null
    // Product-facing status; empty by default so nothing overlays the boxes.
    private var hint by observable("")
    private var primaryState by observable(OtpInputState.Normal)
    private var obscure by observable(false)
    private var appliedPreset = false

    override fun pageDidAppear() {
        super.pageDidAppear()
        if (appliedPreset) {
            return
        }
        val preset = pageData.params.optString("preset", "")
        if (preset.isEmpty()) {
            return
        }
        appliedPreset = true
        val visual = pageData.params.optString("state", "")
        setTimeout(50) {
            val nextState = visualState(visual)
            primaryState = nextState
            primaryRef?.view?.setCode(preset)
            // Apply product hint after setCode so onComplete cannot clobber Error copy.
            setTimeout(30) {
                hint = when (nextState) {
                    OtpInputState.Error -> "验证码不正确"
                    OtpInputState.Success -> "已填满"
                    else -> "已填满"
                }
            }
        }
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                flexDirectionColumn()
                backgroundColor(Color.WHITE)
                padding(16f)
            }
            Text {
                attr {
                    text("验证码")
                    fontSize(20f)
                    fontWeightBold()
                    color(Color(0xFF1F2328L))
                }
            }
            Text {
                attr {
                    marginTop(6f)
                    text("6 位。可粘贴整串短信，或点「粘贴整串」。")
                    fontSize(14f)
                    color(Color(0xFF667085L))
                }
            }
            OtpInput {
                ref { ctx.primaryRef = it }
                attr {
                    marginTop(16f)
                    length(6)
                    obscure(ctx.obscure)
                    state(ctx.primaryState)
                }
                event {
                    onChange { code ->
                        // Product copy only — never onChange:/onComplete: debug.
                        if (ctx.primaryState == OtpInputState.Error) {
                            // keep error copy until user clears / switches state
                        } else {
                            ctx.hint = if (code.isEmpty()) "" else "已输入"
                        }
                    }
                    onComplete { _ ->
                        if (ctx.primaryState != OtpInputState.Error) {
                            ctx.hint = "已填满"
                        }
                    }
                }
            }
            View {
                attr {
                    marginTop(16f)
                    flexDirectionRow()
                }
                ctx.actionButton(this, { "粘贴整串" }, true) {
                    ctx.primaryRef?.view?.setCode("短信验证码839201，请勿泄露")
                    ctx.primaryState = OtpInputState.Normal
                    ctx.hint = "已填满"
                }
                ctx.actionButton(this, { "清空" }, false) {
                    ctx.primaryRef?.view?.clear()
                    ctx.primaryState = OtpInputState.Normal
                    ctx.hint = ""
                }
            }
            View {
                attr {
                    marginTop(8f)
                    flexDirectionRow()
                }
                ctx.actionButton(this, { "错误态" }, false) {
                    ctx.primaryState = OtpInputState.Error
                    ctx.hint = "验证码不正确"
                }
                ctx.actionButton(this, { "成功态" }, false) {
                    ctx.primaryState = OtpInputState.Success
                    ctx.hint = "已填满"
                }
                ctx.actionButton(this, { if (ctx.obscure) "明文" else "密文" }, false) {
                    ctx.obscure = !ctx.obscure
                }
            }
            // Status BELOW buttons — far from OTP cells (avoids overlay on absolute cells).
            View {
                attr {
                    marginTop(20f)
                    height(22f)
                    flexDirectionRow()
                    alignItemsCenter()
                }
                Text {
                    attr {
                        text(ctx.hint)
                        fontSize(14f)
                        color(
                            if (ctx.primaryState == OtpInputState.Error) {
                                Color(0xFFD92D20L)
                            } else {
                                Color(0xFF667085L)
                            },
                        )
                    }
                }
            }
            Text {
                attr {
                    marginTop(20f)
                    text("4 位密文")
                    fontSize(14f)
                    color(Color(0xFF667085L))
                }
            }
            OtpInput {
                attr {
                    marginTop(8f)
                    length(4)
                    obscure(true)
                }
                // Secondary OTP must not inject debug into the primary hint.
                event {
                    onComplete { }
                }
            }
            val preset = ctx.pageData.params.optString("preset", "")
            if (preset.isNotEmpty()) {
                ctx.primaryState = visualState(ctx.pageData.params.optString("state", ""))
                ctx.primaryRef?.view?.setCode(preset)
                ctx.hint = when (ctx.primaryState) {
                    OtpInputState.Error -> "验证码不正确"
                    OtpInputState.Success -> "已填满"
                    else -> "已填满"
                }
            }
        }
    }

    private fun actionButton(
        parent: ViewContainer<*, *>,
        label: () -> String,
        primary: Boolean,
        onClick: () -> Unit,
    ) {
        parent.View {
            attr {
                flex(1f)
                height(40f)
                marginRight(8f)
                borderRadius(8f)
                allCenter()
                backgroundColor(if (primary) Color(0xFF1F6FEBL) else Color(0xFFF2F4F7L))
            }
            event {
                click { onClick() }
            }
            Text {
                attr {
                    text(label())
                    fontSize(14f)
                    color(if (primary) Color.WHITE else Color(0xFF1F2328L))
                }
            }
        }
    }
}

internal fun visualState(name: String): OtpInputState {
    return when (name) {
        "error" -> OtpInputState.Error
        "success" -> OtpInputState.Success
        else -> OtpInputState.Normal
    }
}
