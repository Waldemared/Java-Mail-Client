package com.wald.apps.emailclient.configuration

/**
 * @author vkosolapov
 */
data class AuthConfig(
    var connectionConfig: MailConnectionConfig,
    var username: String,
    var password: String
)