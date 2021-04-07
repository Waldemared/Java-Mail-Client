package com.wald.apps.emailclient.configuration


/**
 * @author vkosolapov
 */
enum class StorageProtocol(val protocolName: String, val javaMailType: String) {
    IMAP("IMAP", "imap"),
    POP("POP", "pop3");
}