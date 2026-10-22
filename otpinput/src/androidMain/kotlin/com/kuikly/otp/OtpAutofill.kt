package com.kuikly.otp

import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.EditText

/**
 * Android host hook. Kuikly 2.4.2 Input does not set autofill hints itself.
 *
 * Call this on the page container after the Kuikly view tree exists. It marks each
 * EditText as an SMS OTP field so the Autofill Framework can insert the full code
 * into the single underlying input.
 */
object OtpAutofill {
    fun applySmsOtpHints(root: View) {
        if (Build.VERSION.SDK_INT < 26) {
            return
        }
        walk(root)
    }

    private fun walk(view: View) {
        if (view is EditText) {
            view.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_YES
            // Same value as View.AUTOFILL_HINT_SMS_OTP. The string keeps the call
            // compiling if the platform stub omits the constant.
            view.setAutofillHints("smsOTPCode")
        }
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                walk(view.getChildAt(index))
            }
        }
    }
}
