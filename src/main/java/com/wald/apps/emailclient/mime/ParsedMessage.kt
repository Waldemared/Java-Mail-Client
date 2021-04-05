package com.wald.apps.emailclient.mime

import jakarta.activation.DataSource


/**
 * @author vkosolapov
 */
data class ParsedMessage(val plain: String? = null,
                         val html: String? = null,
                         val attachments: List<DataSource>? = null) {
    fun prioritisedHtmlText(): String {
        return html ?: plain ?: ""
    }

    fun prioritisedPlainText(): String {
        return plain ?: html ?: ""
    }

    fun clearHtml(): String? {
        if (html != null) {
            val new = html.replace("text/html; charset=utf-8", "text/html")
            return new
        }

        return html
    }
}