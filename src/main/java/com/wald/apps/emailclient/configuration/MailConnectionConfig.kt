package com.wald.apps.emailclient.configuration

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*


/**
 * @author vkosolapov
 */
class MailConnectionConfig
    @JsonCreator
    constructor(@JsonProperty("name") var name: String,
                @JsonProperty("storageProperties") var storageProperties: MailStorageProperties,
                @JsonProperty("senderProperties") var senderProperties: MailSenderProperties) {

    fun javaMailStoreProtocol(): String {
        var incomingMailProtocol = storageProperties.protocol.javaMailType
        if (storageProperties.secure) incomingMailProtocol += "s"
        return incomingMailProtocol
    }

    fun javaMailSenderProtocol(): String {
        var outgoingMailProtocol = senderProperties.protocol.javaMailType
        if (senderProperties.secure) outgoingMailProtocol += "s"
        return outgoingMailProtocol
    }

    fun javaMailStorageProperties(): Properties {
        val incomingMailProtocol = javaMailStoreProtocol()
        return properties(
            "mail.$incomingMailProtocol.host" to storageProperties.host,
            "mail.$incomingMailProtocol.port" to storageProperties.port.toString(),
            "mail.$incomingMailProtocol.starttls.enable" to storageProperties.secure.toString()
        )
    }

    fun javaMailSenderProperties(user: String, password: String): Properties {
        val outgoingMailProtocol = javaMailSenderProtocol()
        return properties(
            "mail.$outgoingMailProtocol.host" to senderProperties.host,
            "mail.$outgoingMailProtocol.port" to senderProperties.port.toString(),
            "mail.$outgoingMailProtocol.username" to user,
            "mail.$outgoingMailProtocol.password" to password,
            "mail.$outgoingMailProtocol.starttls.enable" to senderProperties.secure.toString(),
            "mail.$outgoingMailProtocol.auth" to true.toString()
        )
    }

    override fun toString(): String = name

    private fun properties(vararg properties: Pair<String, String>): Properties {
        val jProperties = Properties()
        for (property in properties) {
            jProperties[property.first] = property.second
        }
        return jProperties
    }
}