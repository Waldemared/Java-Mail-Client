package com.wald.apps.emailclient.configuration


/**
 * @author vkosolapov
 * @since
 */
enum class SenderProtocol(val protocolName: String, val javaMailType: String) {
    SMTP("SMTP", "smtp")
}