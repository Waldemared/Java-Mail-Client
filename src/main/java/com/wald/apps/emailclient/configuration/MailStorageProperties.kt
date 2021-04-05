package com.wald.apps.emailclient.configuration

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty


/**
 * @author vkosolapov
 */
data class MailStorageProperties
    @JsonCreator
    constructor(@JsonProperty("host") val host: String,
                @JsonProperty("port") val port: Int,
                @JsonProperty("secure") val secure: Boolean,
                @JsonProperty("protocol") val protocol: StorageProtocol)