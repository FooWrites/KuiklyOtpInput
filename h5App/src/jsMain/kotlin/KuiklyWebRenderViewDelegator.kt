package com.kuikly.otp.h5

import com.tencent.kuikly.core.render.web.IKuiklyRenderExport
import com.tencent.kuikly.core.render.web.ktx.SizeI
import com.tencent.kuikly.core.render.web.runtime.web.expand.KuiklyRenderViewDelegator
import com.tencent.kuikly.core.render.web.expand.KuiklyRenderViewDelegatorDelegate

class KuiklyWebRenderViewDelegator : KuiklyRenderViewDelegatorDelegate {
    private val delegate = KuiklyRenderViewDelegator(this)

    fun init(
        containerId: String,
        pageName: String,
        pageData: Map<String, Any>,
        size: SizeI,
    ) {
        delegate.onAttach(containerId, pageName, pageData, size)
    }

    override fun registerViewExternalPropHandler(kuiklyRenderExport: IKuiklyRenderExport) {
        super.registerViewExternalPropHandler(kuiklyRenderExport)
        kuiklyRenderExport.viewPropExternalHandlerExport(KROtpInputPropHandler())
    }

    fun resume() {
        delegate.onResume()
    }

    fun pause() {
        delegate.onPause()
    }
}
