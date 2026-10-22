package com.foowrites.kuikly.otp.host.adapter

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import com.foowrites.kuikly.otp.host.KuiklyRenderActivity
import com.tencent.kuikly.core.render.android.adapter.HRImageLoadOption
import com.tencent.kuikly.core.render.android.adapter.IKRImageAdapter
import com.tencent.kuikly.core.render.android.adapter.IKRLogAdapter
import com.tencent.kuikly.core.render.android.adapter.IKRRouterAdapter
import com.tencent.kuikly.core.render.android.adapter.IKRThreadAdapter
import com.tencent.kuikly.core.render.android.adapter.IKRUncaughtExceptionHandlerAdapter
import org.json.JSONObject
import java.util.concurrent.Executors

class KRImageAdapter : IKRImageAdapter {
    override fun fetchDrawable(
        imageLoadOption: HRImageLoadOption,
        callback: (drawable: Drawable?) -> Unit,
    ) {
        callback(null)
    }
}

object KRLogAdapter : IKRLogAdapter {
    override val asyncLogEnable: Boolean = false

    override fun i(tag: String, msg: String) {
        Log.i(tag, msg)
    }

    override fun d(tag: String, msg: String) {
        Log.d(tag, msg)
    }

    override fun e(tag: String, msg: String) {
        Log.e(tag, msg)
    }
}

object KRUncaughtExceptionHandlerAdapter : IKRUncaughtExceptionHandlerAdapter {
    override fun uncaughtException(throwable: Throwable) {
        Log.e("KuiklyOtpHost", throwable.stackTraceToString())
    }
}

object KRRouterAdapter : IKRRouterAdapter {
    override fun openPage(context: Context, pageName: String, pageData: JSONObject) {
        KuiklyRenderActivity.start(context, pageName, pageData)
    }

    override fun closePage(context: Context) {
        (context as? Activity)?.finish()
    }
}

class KRThreadAdapter : IKRThreadAdapter {
    override fun executeOnSubThread(task: () -> Unit) {
        executor.execute(task)
    }

    private companion object {
        val executor = Executors.newFixedThreadPool(2)
    }
}
