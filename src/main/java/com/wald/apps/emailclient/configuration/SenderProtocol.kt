package com.wald.apps.emailclient.configuration


/**
 * @author vkosolapov
 */
enum class SenderProtocol(val protocolName: String, val javaMailType: String) {
    SMTP("SMTP", "smtp")
}