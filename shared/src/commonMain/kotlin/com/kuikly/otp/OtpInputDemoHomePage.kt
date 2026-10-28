package com.kuikly.otp

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

@Page("otp_input_demo_home")
internal class OtpInputDemoHomePage : Pager() {
    private val routerModule by lazy(LazyThreadSafetyMode.NONE) {
        acquireModule<RouterModule>(RouterModule.MODULE_NAME)
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                flexDirectionColumn()
                backgroundColor(Color.WHITE)
                padding(24f)
                paddingTop(ctx.pagerData.statusBarHeight + 24f)
            }
            Text {
                attr {
                    text("Kuikly OtpInput")
                    fontSize(24f)
                    color(Color(0xFF1F2328L))
                }
            }
            Text {
                attr {
                    marginTop(8f)
                    text("选择 DSL 查看验证码输入框示例")
                    fontSize(14f)
                    color(Color(0xFF667085L))
                }
            }
            ctx.entry(this, 20f, "Kuikly DSL", "OtpInputView", "otp_input_demo")
            ctx.entry(this, 12f, "Compose DSL", "OtpInput", "otp_input_compose_demo")
        }
    }

    private fun entry(container: ViewContainer<*, *>, top: Float, title: String, subtitle: String, page: String) {
        val ctx = this
        container.View {
            attr {
                marginTop(top)
                height(72f)
                padding(16f)
                borderRadius(12f)
                backgroundColor(Color(0xFFF2F4F7L))
                flexDirectionColumn()
            }
            event {
                click {
                    ctx.routerModule.openPage(page)
                }
            }
            Text {
                attr {
                    text(title)
                    fontSize(18f)
                    color(Color(0xFF1F2328L))
                }
            }
            Text {
                attr {
                    marginTop(6f)
                    text(subtitle)
                    fontSize(13f)
                    color(Color(0xFF667085L))
                }
            }
        }
    }
}
