package com.foowrites.kuikly.otp.host

import android.app.Application
import com.foowrites.kuikly.otp.host.adapter.KRImageAdapter
import com.foowrites.kuikly.otp.host.adapter.KRLogAdapter
import com.foowrites.kuikly.otp.host.adapter.KRRouterAdapter
import com.foowrites.kuikly.otp.host.adapter.KRThreadAdapter
import com.foowrites.kuikly.otp.host.adapter.KRUncaughtExceptionHandlerAdapter
import com.tencent.kuikly.core.render.android.adapter.KuiklyRenderAdapterManager

class OtpHostApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (KuiklyRenderAdapterManager.krImageAdapter == null) {
            KuiklyRenderAdapterManager.krImageAdapter = KRImageAdapter()
        }
        if (KuiklyRenderAdapterManager.krLogAdapter == null) {
            KuiklyRenderAdapterManager.krLogAdapter = KRLogAdapter
        }
        if (KuiklyRenderAdapterManager.krUncaughtExceptionHandlerAdapter == null) {
            KuiklyRenderAdapterManager.krUncaughtExceptionHandlerAdapter =
                KRUncaughtExceptionHandlerAdapter
        }
        if (KuiklyRenderAdapterManager.krRouterAdapter == null) {
            KuiklyRenderAdapterManager.krRouterAdapter = KRRouterAdapter
        }
        if (KuiklyRenderAdapterManager.krThreadAdapter == null) {
            KuiklyRenderAdapterManager.krThreadAdapter = KRThreadAdapter()
        }
    }
}
