package com.kuikly.otp

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.ViewRef
import com.tencent.kuikly.core.layout.FlexJustifyContent
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

@Page("otp_input_demo")
internal class OtpInputDemoPage : Pager() {
    private var otpRef: ViewRef<OtpInputView>? = null
    private var length by observable(6)
    private var charset by observable(OtpCharset.DIGITS)
    private var cellShape by observable(OtpCellShape.BOX)
    private var alignment by observable(FlexJustifyContent.FLEX_START)
    private var mask by observable(false)
    private var editable by observable(true)
    private var verified by observable<Boolean?>(null)
    private var code by observable("")

    private val expectedCode: String
        get() = DemoOtp.expected(length, charset == OtpCharset.DIGITS)

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                flexDirectionColumn()
                backgroundColor(Color.WHITE)
                padding(16f)
                paddingTop(ctx.pagerData.statusBarHeight + 16f)
            }
            Text {
                attr {
                    text("Kuikly DSL 验证码输入框")
                    fontSize(20f)
                    color(Color(DemoOtp.TEXT))
                }
            }
            Text {
                attr {
                    marginTop(6f)
                    text("输入 ${ctx.expectedCode} 校验通过，其他内容校验失败。")
                    fontSize(14f)
                    color(Color(DemoOtp.SECONDARY))
                }
            }
            OtpInput {
                ref { ctx.otpRef = it }
                attr {
                    marginTop(20f)
                    justifyContent(ctx.alignment)
                    length = ctx.length
                    charset = ctx.charset
                    cellShape = ctx.cellShape
                    mask = ctx.mask
                    editable = ctx.editable
                    status = when (ctx.verified) {
                        true -> OtpInputStatus.SUCCESS
                        false -> OtpInputStatus.ERROR
                        null -> OtpInputStatus.NORMAL
                    }
                    cellWidth = if (ctx.length > 6) 36f else 44f
                    cellSpacing = if (ctx.length > 6) 6f else 8f
                    placeholderChar = "-"
                }
                event {
                    onChange { value ->
                        ctx.code = value
                        ctx.verified = null
                    }
                    onComplete { value ->
                        ctx.verified = value == ctx.expectedCode
                    }
                }
            }
            Text {
                attr {
                    marginTop(12f)
                    text(DemoOtp.hint(ctx.code, ctx.verified))
                    fontSize(14f)
                    color(Color(DemoOtp.hintColor(ctx.verified)))
                }
            }
            View {
                attr {
                    marginTop(16f)
                    flexDirectionRow()
                }
                ctx.button(this, "粘贴短信", true) {
                    ctx.otpRef?.view?.setCode(DemoOtp.sms(ctx.expectedCode))
                }
                ctx.button(this, "清空", false) {
                    ctx.otpRef?.view?.clear()
                }
                ctx.button(this, "聚焦", false) {
                    ctx.otpRef?.view?.focus()
                }
            }
            // 位数和字符集变化后内容可能被截断或重新填满，先清除校验结果，由组件回调重新给出。
            ctx.options(this, "位数", listOf("4" to 4, "6" to 6, "8" to 8), { ctx.length }) {
                ctx.verified = null
                ctx.length = it
            }
            ctx.options(this, "字符", listOf("数字" to OtpCharset.DIGITS, "字母数字" to OtpCharset.ALPHANUMERIC), { ctx.charset }) {
                ctx.verified = null
                ctx.charset = it
            }
            ctx.options(this, "样式", listOf("方框" to OtpCellShape.BOX, "下划线" to OtpCellShape.UNDERLINE), { ctx.cellShape }) {
                ctx.cellShape = it
            }
            ctx.options(
                this,
                "对齐",
                listOf(
                    "靠左" to FlexJustifyContent.FLEX_START,
                    "居中" to FlexJustifyContent.CENTER,
                    "两端" to FlexJustifyContent.SPACE_BETWEEN,
                ),
                { ctx.alignment }
            ) {
                ctx.alignment = it
            }
            ctx.options(this, "遮挡", listOf("关" to false, "开" to true), { ctx.mask }) {
                ctx.mask = it
            }
            ctx.options(this, "可编辑", listOf("是" to true, "否" to false), { ctx.editable }) {
                ctx.editable = it
            }
        }
    }

    private fun button(container: ViewContainer<*, *>, title: String, primary: Boolean, onClick: () -> Unit) {
        container.View {
            attr {
                flex(1f)
                height(44f)
                marginRight(8f)
                borderRadius(8f)
                backgroundColor(Color(if (primary) DemoOtp.PRIMARY else DemoOtp.SURFACE))
                allCenter()
            }
            event {
                click { onClick() }
            }
            Text {
                attr {
                    text(title)
                    fontSize(15f)
                    color(if (primary) Color.WHITE else Color(DemoOtp.TEXT))
                }
            }
        }
    }

    private fun <T> options(
        container: ViewContainer<*, *>,
        label: String,
        values: List<Pair<String, T>>,
        selected: () -> T,
        onSelect: (T) -> Unit,
    ) {
        container.View {
            attr {
                marginTop(14f)
                flexDirectionRow()
                alignItemsCenter()
            }
            Text {
                attr {
                    width(64f)
                    text(label)
                    fontSize(14f)
                    color(Color(DemoOtp.SECONDARY))
                }
            }
            for ((name, value) in values) {
                View {
                    attr {
                        marginRight(8f)
                        padding(6f, 12f, 6f, 12f)
                        borderRadius(14f)
                        backgroundColor(Color(if (selected() == value) DemoOtp.PRIMARY else DemoOtp.SURFACE))
                    }
                    event {
                        click { onSelect(value) }
                    }
                    Text {
                        attr {
                            text(name)
                            fontSize(13f)
                            color(if (selected() == value) Color.WHITE else Color(DemoOtp.TEXT))
                        }
                    }
                }
            }
        }
    }
}
