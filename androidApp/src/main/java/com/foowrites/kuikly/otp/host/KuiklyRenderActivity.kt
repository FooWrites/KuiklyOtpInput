package com.foowrites.kuikly.otp.host

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.kuikly.otp.OtpAutofill
import com.tencent.kuikly.core.render.android.expand.KuiklyRenderViewBaseDelegator
import com.tencent.kuikly.core.render.android.expand.KuiklyRenderViewBaseDelegatorDelegate
import org.json.JSONObject

class KuiklyRenderActivity : AppCompatActivity() {
    private lateinit var container: ViewGroup
    private lateinit var delegator: KuiklyRenderViewBaseDelegator

    private val pageName: String
        get() = intent.getStringExtra(KEY_PAGE_NAME)?.takeIf { it.isNotEmpty() } ?: DEFAULT_PAGE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.WHITE
        setContentView(R.layout.activity_hr)
        container = findViewById(R.id.hr_container)
        delegator = KuiklyRenderViewBaseDelegator(delegate())
        delegator.onAttach(container, "", pageName, createPageData())
    }

    override fun onResume() {
        super.onResume()
        delegator.onResume()
    }

    override fun onPause() {
        super.onPause()
        delegator.onPause()
    }

    override fun onDestroy() {
        delegator.onDetach()
        super.onDestroy()
    }

    private fun delegate(): KuiklyRenderViewBaseDelegatorDelegate {
        return object : KuiklyRenderViewBaseDelegatorDelegate {
            override fun onPageLoadComplete(
                isSucceed: Boolean,
                errorReason: com.tencent.kuikly.core.render.android.exception.ErrorReason?,
                executeMode: com.tencent.kuikly.core.render.android.context.KuiklyRenderCoreExecuteModeBase,
            ) {
                Log.i(TAG, "onPageLoadComplete succeed=$isSucceed page=$pageName reason=$errorReason")
                applyAutofillHints()
            }
        }
    }

    private fun applyAutofillHints() {
        container.post {
            OtpAutofill.applySmsOtpHints(container)
            container.postDelayed({ OtpAutofill.applySmsOtpHints(container) }, 300)
        }
    }

    private fun createPageData(): Map<String, Any> {
        val pageData = linkedMapOf<String, Any>()
        intent.getStringExtra(KEY_PAGE_DATA)?.let { raw ->
            val json = JSONObject(raw)
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                pageData[key] = json.get(key)
            }
        }
        intent.getStringExtra(KEY_PRESET)?.let { pageData["preset"] = it }
        intent.getStringExtra(KEY_STATE)?.let { pageData["state"] = it }
        Log.i(TAG, "open page=$pageName data=$pageData")
        return pageData
    }

    companion object {
        private const val TAG = "KuiklyOtpHost"
        private const val DEFAULT_PAGE = "otp_input_demo"
        private const val KEY_PAGE_NAME = "pageName"
        private const val KEY_PAGE_DATA = "pageData"
        private const val KEY_PRESET = "preset"
        private const val KEY_STATE = "state"

        fun start(context: Context, pageName: String, pageData: JSONObject) {
            val intent = Intent(context, KuiklyRenderActivity::class.java)
            intent.putExtra(KEY_PAGE_NAME, pageName)
            intent.putExtra(KEY_PAGE_DATA, pageData.toString())
            context.startActivity(intent)
        }
    }
}
