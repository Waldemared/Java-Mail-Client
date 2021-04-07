package com.wald.apps.emailclient.configuration

/**
 * @author vkosolapov
 */
data class AuthConfig(
    var mailService: MailService,
    var username: String,
    var password: String
)