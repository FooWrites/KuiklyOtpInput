package com.kuikly.otp.h5

import com.tencent.kuikly.core.render.web.ktx.SizeI
import kotlinx.browser.document
import kotlinx.browser.window

fun main() {
    val params = parseQuery(window.location.search)
    val pageName = params["page_name"] ?: "otp_input_demo_home"
    val width = window.innerWidth
    val height = window.innerHeight
    val pageData = mapOf(
        "statusBarHeight" to 0f,
        "activityWidth" to width,
        "activityHeight" to height,
        "param" to params,
    )
    val delegator = KuiklyWebRenderViewDelegator()
    delegator.init("root", pageName, pageData, SizeI(width, height))
    delegator.resume()
    document.addEventListener("visibilitychange", {
        if (document.asDynamic().hidden as Boolean) {
            delegator.pause()
        } else {
            delegator.resume()
        }
    })
}

private fun parseQuery(search: String): Map<String, String> {
    return search
        .removePrefix("?")
        .split("&")
        .mapNotNull { item ->
            val parts = item.split("=", limit = 2)
            if (parts.size == 2 && parts[0].isNotEmpty()) {
                decodeQueryComponent(parts[0]) to decodeQueryComponent(parts[1])
            } else {
                null
            }
        }
        .toMap()
}

private fun decodeQueryComponent(value: String): String {
    return js("decodeURIComponent")(value.replace("+", " ")) as String
}
